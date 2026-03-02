package ht.heist.plugins.hud.overlay;

import ht.heist.plugins.hud.HeistHUDConfig;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayDeque;

public final class CursorTracerOverlay extends Overlay {
    private final Client client;
    private final HeistHUDConfig cfg;

    private static final int MAX_TRAIL = 128;
    private final ArrayDeque<TrailPoint> trail = new ArrayDeque<>(MAX_TRAIL);

    private static final class TrailPoint {
        final int x, y;
        final long ts;

        TrailPoint(int x, int y, long ts) {
            this.x = x;
            this.y = y;
            this.ts = ts;
        }
    }

    @Inject
    public CursorTracerOverlay(Client client, HeistHUDConfig cfg) {
        this.client = client;
        this.cfg = cfg;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
        setMovable(false);
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (!cfg.showCursorTracer())
            return null;

        final long now = System.currentTimeMillis();
        final int ringR = Math.max(2, cfg.tracerRingRadiusPx());
        final int trailMs = Math.max(100, cfg.tracerTrailMs());

        final Point mp = client.getMouseCanvasPosition();
        if (mp != null) {
            trail.addLast(new TrailPoint(mp.getX(), mp.getY(), now));
            while (trail.size() > MAX_TRAIL)
                trail.removeFirst();
        }

        final Composite saved = g.getComposite();
        g.setColor(Color.WHITE);
        for (TrailPoint p : trail) {
            long age = now - p.ts;
            if (age > trailMs)
                continue;
            float alpha = 1f - (float) age / trailMs;
            if (alpha < 0.05f)
                alpha = 0.05f;
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g.fillRect(p.x, p.y, 1, 1);
        }

        if (mp != null) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.9f));
            g.setColor(Color.WHITE);
            g.drawOval(mp.getX() - ringR, mp.getY() - ringR, ringR * 2, ringR * 2);
        }

        g.setComposite(saved);
        return null;
    }
}
