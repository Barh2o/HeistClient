package ht.heist.api.widgets;

import ht.heist.Static;
import ht.heist.api.game.ClientScriptAPI;
import ht.heist.data.Tab;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.VarClientID;

import java.util.Arrays;

/**
 * API for interacting with game tabs.
 */
public class TabsAPI
{
    /**
     * Opens the specified tab.
     *
     * @param tab The tab to open.
     */
    public static void open(Tab tab)
    {
        Client client = Static.getClient();
        if (client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.LOADING)
        {
            return;
        }

        ClientScriptAPI.switchTabs(tab);
    }

    /**
     * Checks if the specified tab is currently open.
     *
     * @param tab The tab to check.
     * @return True if the tab is open, false otherwise.
     */
    public static boolean isOpen(Tab tab) {
        Client client = Static.getClient();
        return client.getVarcIntValue(VarClientID.TOPLEVEL_PANEL) == tab.getTabVarbit();
    }
}
