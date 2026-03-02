package ht.heist.runelite;

import ht.heist.Logger;
import ht.heist.Static;
import ht.heist.services.proxy.ProxyManager;
import ht.heist.heistclient.Main;
import ht.heist.model.NavButton;
import ht.heist.model.ui.HeistClientInfoPanel;
import ht.heist.model.ui.HeistClientOptionsPanel;
import ht.heist.util.ReflectBuilder;
import ht.heist.util.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ClientUIUpdater {
    private static JPanel wrapper;
    private static JScrollPane scrollPane;

    public static void inject() {
        if (Static.getCliArgs().isIncognito())
            return;

        addNavigation();

        Object clientUI = ReflectBuilder.runelite()
                .staticField("rlInstance")
                .field("clientUI")
                .get();

        JFrame frame = ReflectBuilder.of(clientUI)
                .field("frame")
                .get();

        JPanel originalContent = ReflectBuilder.of(clientUI)
                .field("content")
                .get();

        BufferedImage icon = ResourceUtil.getImage(Main.class, "icon.png");
        frame.setIconImage(icon);
        if (ProxyManager.getProxy() != null) {
            frame.setTitle("HeistClient (Proxy: " +
                    ProxyManager.getProxy().getHost() + ":" +
                    ProxyManager.getProxy().getPort() + ")");
        } else {
            frame.setTitle("HeistClient");
        }

        wrapper = new JPanel(new BorderLayout());
        JTextPane console = Logger.getConsole();
        scrollPane = new JScrollPane(console);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30), 5));
        Logger.setLoggerComponent(scrollPane);
        Logger.initLoggerUI(scrollPane, wrapper, frame);

        wrapper.add(originalContent, BorderLayout.CENTER);
        wrapper.add(scrollPane, BorderLayout.SOUTH);

        frame.setContentPane(wrapper);
        frame.revalidate();
        frame.repaint();
    }

    private static void addNavigation() {
        BufferedImage info = ResourceUtil.getImage(Main.class, "info.png");
        NavButton.builder()
                .icon(info)
                .priority(0)
                .tooltip("HeistClient Info")
                .panel(new HeistClientInfoPanel())
                .addToNavigation();

        BufferedImage settings = ResourceUtil.getImage(Main.class, "settings.png");
        NavButton.builder()
                .icon(settings)
                .priority(0)
                .tooltip("HeistClient Options")
                .panel(HeistClientOptionsPanel.getInstance())
                .addToNavigation();
    }

    /**
     * Patches the splash screen of the client. don't remove, call is injected.
     * 
     * @param jFrame SplashScreen jframe
     */
    public static void patchSplashScreen(JFrame jFrame) {
        jFrame.setTitle("HeistClient Launcher");
        jFrame.setIconImage(ResourceUtil.getImage(Main.class, "icon.png"));
        jFrame.setBackground(Color.BLACK);
    }
}
