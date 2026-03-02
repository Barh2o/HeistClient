package ht.heist.plugins;

import com.google.inject.Provides;
import ht.heist.corejava.api.input.TapBus;
import ht.heist.corejava.api.input.TapEvent;
import ht.heist.corejava.api.input.TapEvent.Button;
import ht.heist.corejava.api.input.TapEvent.Type;
import ht.heist.corejava.logging.ClickLogger;
import ht.heist.corejava.logging.MoveFeatures;
import ht.heist.corejava.logging.TapResult;
import ht.heist.plugins.hud.HeistHUDConfig;
import ht.heist.plugins.hud.export.HeatmapExporter;
import ht.heist.plugins.hud.overlay.ClickDotsOverlay;
import ht.heist.plugins.hud.overlay.CursorTracerOverlay;
import ht.heist.plugins.hud.service.ClickStore;
import ht.heist.plugins.hud.ui.HeistHUDPanel;
import ht.heist.util.ResourceUtil;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(name = "Heist HUD", description = "Persistent click dots + heatmap export + on-disk click logs + Phase-A features.", tags = {
        "heist", "hud", "logo", "clicks", "heatmap" })
public final class HeistHUDPlugin extends Plugin {
    // ---- Injected services --------------------------------------------------
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private MouseManager mouseManager;
    @Inject
    private Client client;

    @Inject
    private HeistHUDConfig cfg;
    @Inject
    private ClickStore store;
    @Inject
    private HeatmapExporter exporter;
    @Inject
    private ClickDotsOverlay dotsOverlay;
    @Inject
    private CursorTracerOverlay cursorTracerOverlay;
    @Inject
    private HeistHUDPanel panel;

    // ---- UI handle ----------------------------------------------------------
    private NavigationButton navButton;

    // ---- Listeners ----------------------------------------------------------
    private TapBus.Listener busListener;
    private MouseAdapter rlMouseAdapter;

    // ---- On-disk logging ----------------------------------------------------
    private ClickLogger logger;
    private String sessionId;

    // ---- Phase-A: gesture correlation & dwell tracking ----------------------
    private static final int SIG_MOVE_RADIUS_PX = 2;
    private static final long MATCH_WINDOW_MS = 450;
    private static final long FALSE_GRACE_MS = 180;
    private volatile long lastMoveTs = 0L;
    private volatile int lastMoveX = Integer.MIN_VALUE;
    private volatile int lastMoveY = Integer.MIN_VALUE;

    private final Map<Integer, Pending> pendingByButton = new HashMap<>();
    private ScheduledExecutorService scheduler;

    private static final class Pending {
        final String eventId;
        final long downTs;
        final int downX, downY;
        final Integer dwellMs;
        final String opt, tgt;
        final Integer opcode;
        volatile boolean processed;
        volatile ScheduledFuture<?> graceFuture;

        Pending(String eventId, long downTs, int downX, int downY,
                Integer dwellMs, String opt, String tgt, Integer opcode) {
            this.eventId = eventId;
            this.downTs = downTs;
            this.downX = downX;
            this.downY = downY;
            this.dwellMs = dwellMs;
            this.opt = opt;
            this.tgt = tgt;
            this.opcode = opcode;
        }
    }

    @Provides
    HeistHUDConfig provideConfig(ConfigManager cm) {
        return cm.getConfig(HeistHUDConfig.class);
    }

    @Override
    protected void startUp() {
        overlayManager.add(dotsOverlay);
        overlayManager.add(cursorTracerOverlay);

        final BufferedImage icon = ResourceUtil.getImage(HeistHUDPlugin.class, "heisticon.png");
        navButton = NavigationButton.builder()
                .tooltip("Heist HUD")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);

        busListener = store::add;
        TapBus.addListener(busListener);

        rlMouseAdapter = new MouseAdapter() {
            @Override
            public MouseEvent mouseMoved(MouseEvent e) {
                onMouseMoved(e);
                return e;
            }

            @Override
            public MouseEvent mousePressed(MouseEvent e) {
                onMousePressed(e);
                return e;
            }

            @Override
            public MouseEvent mouseReleased(MouseEvent e) {
                onMouseReleased(e);
                return e;
            }

            @Override
            public MouseEvent mouseClicked(MouseEvent e) {
                postFromAwt(e, Type.CLICK, null, null, null, null);
                return e;
            }
        };
        mouseManager.registerMouseListener(rlMouseAdapter);

        sessionId = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File outDir = expandOutputDir(cfg.outputDir());
        logger = new ClickLogger(sessionId, outDir, cfg.writeCsv());
        try {
            logger.start();
        } catch (Exception ignored) {
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HeistHUD-ResultGrace");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(dotsOverlay);
        overlayManager.remove(cursorTracerOverlay);
        if (navButton != null) {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }

        if (busListener != null) {
            TapBus.removeListener(busListener);
            busListener = null;
        }
        if (rlMouseAdapter != null) {
            mouseManager.unregisterMouseListener(rlMouseAdapter);
            rlMouseAdapter = null;
        }

        for (Pending p : pendingByButton.values()) {
            ScheduledFuture<?> f = p.graceFuture;
            if (f != null)
                f.cancel(true);
        }
        pendingByButton.clear();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        if (logger != null) {
            try {
                logger.stop();
            } catch (Exception ignored) {
            }
            logger = null;
        }

        store.clear();
    }

    // ---- Manual dwell estimation --------------------------------------------
    private void onMouseMoved(MouseEvent e) {
        final int x = e.getX(), y = e.getY();
        if (lastMoveX == Integer.MIN_VALUE) {
            lastMoveX = x;
            lastMoveY = y;
            lastMoveTs = System.currentTimeMillis();
            return;
        }
        if (Math.abs(x - lastMoveX) >= SIG_MOVE_RADIUS_PX || Math.abs(y - lastMoveY) >= SIG_MOVE_RADIUS_PX) {
            lastMoveX = x;
            lastMoveY = y;
            lastMoveTs = System.currentTimeMillis();
        }
    }

    // ---- PRESS --------------------------------------------------------------
    private void onMousePressed(MouseEvent e) {
        final long now = System.currentTimeMillis();
        final String eventId = UUID.randomUUID().toString();

        Integer dwellMs = null;
        if (lastMoveTs > 0) {
            long d = now - lastMoveTs;
            if (d >= 0 && d <= 30_000)
                dwellMs = (int) d;
        }

        String opt = null, tgt = null;
        Integer opcode = null;
        final MenuEntry[] entries = client.getMenuEntries();
        if (entries != null && entries.length > 0) {
            final MenuEntry top = entries[entries.length - 1];
            opt = top.getOption();
            tgt = top.getTarget();
            try {
                opcode = top.getType().getId();
            } catch (Throwable ignore) {
            }
        }

        pendingByButton.put(e.getButton(), new Pending(eventId, now, e.getX(), e.getY(), dwellMs, opt, tgt, opcode));
        postFromAwt(e, Type.DOWN, eventId, opt, tgt, opcode);
    }

    // ---- RELEASE ------------------------------------------------------------
    private void onMouseReleased(MouseEvent e) {
        final long now = System.currentTimeMillis();
        final Pending p = pendingByButton.get(e.getButton());
        final String eventId = (p != null ? p.eventId : null);

        postFromAwt(e, Type.UP, eventId, null, null, null);

        if (p != null && logger != null) {
            final int holdMs = (int) Math.max(0, now - p.downTs);
            final MoveFeatures mf = new MoveFeatures(
                    sessionId, p.eventId, "manual",
                    p.dwellMs, null, holdMs,
                    null, null, null, null, null,
                    null, null, null, null);
            try {
                logger.writeMoveFeatures(mf);
            } catch (Exception ignored) {
            }

            if (scheduler != null) {
                p.graceFuture = scheduler.schedule(() -> {
                    if (!p.processed && logger != null) {
                        final TapResult tr = new TapResult(sessionId, p.eventId, false, System.currentTimeMillis(),
                                p.opt, p.tgt, p.opcode);
                        try {
                            logger.writeTapResult(tr);
                        } catch (Exception ignored) {
                        }
                    }
                    pendingByButton.remove(e.getButton(), p);
                }, FALSE_GRACE_MS, TimeUnit.MILLISECONDS);
            }
        }
    }

    // ---- MenuOptionClicked --------------------------------------------------
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked ev) {
        if (pendingByButton.isEmpty())
            return;
        final long now = System.currentTimeMillis();
        final String opt = ev.getMenuOption(), tgt = ev.getMenuTarget();
        Integer opcode = null;
        try {
            opcode = ev.getMenuAction().getId();
        } catch (Throwable ignore) {
        }

        for (Map.Entry<Integer, Pending> entry : pendingByButton.entrySet()) {
            final Pending p = entry.getValue();
            if (now - p.downTs > MATCH_WINDOW_MS)
                continue;
            if (safeEq(p.opt, opt) && safeEq(p.tgt, tgt) &&
                    (p.opcode == null || opcode == null || p.opcode.equals(opcode))) {
                p.processed = true;
                final ScheduledFuture<?> f = p.graceFuture;
                if (f != null)
                    f.cancel(true);
                if (logger != null) {
                    final TapResult tr = new TapResult(sessionId, p.eventId, true, now, p.opt, p.tgt, p.opcode);
                    try {
                        logger.writeTapResult(tr);
                    } catch (Exception ignored) {
                    }
                }
                break;
            }
        }
    }

    private static boolean safeEq(String a, String b) {
        if (a == null && b == null)
            return true;
        if (a == null || b == null)
            return false;
        return a.equals(b);
    }

    // ---- Post TapEvent to TapBus -------------------------------------------
    private void postFromAwt(MouseEvent e, Type type, String eventId, String opt, String tgt, Integer opcode) {
        Button b;
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                b = Button.LEFT;
                break;
            case MouseEvent.BUTTON2:
                b = Button.MIDDLE;
                break;
            case MouseEvent.BUTTON3:
                b = Button.RIGHT;
                break;
            default:
                b = Button.UNKNOWN;
        }
        boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
        boolean alt = (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0;
        TapBus.post(new TapEvent(
                System.currentTimeMillis(), e.getX(), e.getY(),
                type, b, shift, ctrl, alt, sessionId, eventId,
                null, null, null, null, null, null, opt, tgt, opcode, null));
    }

    private static File expandOutputDir(String raw) {
        return new File(raw.replace("%USERPROFILE%", System.getProperty("user.home")));
    }
}
