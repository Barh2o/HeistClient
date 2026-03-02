package ht.heist.model;

import ht.heist.model.ui.components.VPluginPanel;
import ht.heist.util.ReflectBuilder;
import javax.swing.*;
import java.awt.*;

public class PluginPanelConverter
{
    public static Object toPluginPanel(VPluginPanel panel)
    {
        if (panel == null) {
            return null;
        }

        JPanel pluginPanel = ReflectBuilder.newInstance(
                "net.runelite.proxies.PluginPanelProxy",
                new Class[]{boolean.class},
                new Object[]{false}
        ).get();

        pluginPanel.setLayout(new BorderLayout());
        pluginPanel.add(panel, BorderLayout.CENTER);

        pluginPanel.repaint();
        pluginPanel.revalidate();
        return pluginPanel;
    }
}
