package ht.heist.plugins;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import ht.heist.util.ResourceUtil;
import javax.inject.Inject;
import java.awt.image.BufferedImage;

@PluginDescriptor(name = "Heist HUD", description = "Heist Identity & HUD Features", tags = { "heist", "hud", "logo" })
public class HeistHUDPlugin extends Plugin {
    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        final BufferedImage icon = ResourceUtil.getImage(HeistHUDPlugin.class, "heist_hud_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Heist HUD")
                .icon(icon)
                .priority(5)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
    }
}
