package ht.heist.api.entities;

import ht.heist.Static;
import ht.heist.api.TClient;
import ht.heist.data.wrappers.PlayerEx;
import ht.heist.queries.PlayerQuery;
import ht.heist.services.ClickManager;
import ht.heist.services.ClickPacket.ClickType;
import net.runelite.api.Client;
import net.runelite.api.Player;

/**
 * Player API
 */
public class PlayerAPI extends ActorAPI
{
    /**
     * Creates an instance of PlayerQuery
     * @return PlayerQuery
     */
    public static PlayerQuery search()
    {
        return new PlayerQuery();
    }

    /**
     * interact with a player by option number
     * @param player player
     * @param option option number
     */
    public static void interact(PlayerEx player, int option)
    {
        interact(player.getIndex(), option);
    }

    /**
     * interacts with a player by first matching action
     * @param player player
     * @param actions actions list
     */
    public static void interact(PlayerEx player, String... actions)
    {
        String[] playerActions = player.getActions();
        for (String action : actions)
        {
            for(int i = 0; i < playerActions.length; i++)
            {
                if(playerActions[i] != null && playerActions[i].toLowerCase().contains(action.toLowerCase()))
                {
                    interact(player, i);
                    return;
                }
            }
        }
    }

    /**
     * interact with a player by option number
     * @param index player index
     * @param option option number
     */
    public static void interact(int index, int option)
    {
        TClient client = Static.getClient();
        Static.invoke(() ->
        {
            ClickManager.click(ClickType.ACTOR);
            client.getPacketWriter().playerActionPacket(option, index, false);
        });
    }

    /**
     * @return The current player
     */
    @Deprecated
    public static PlayerEx getLocal()
    {
        return PlayerEx.getLocal();
    }

    /**
     * check if the local player is idle
     * @return true if idle
     */
    @Deprecated
    public static boolean isIdle()
    {
        return PlayerEx.getLocal().isIdle();
    }
}
