package ht.heist.api.entities;

import ht.heist.Static;
import ht.heist.api.game.CombatAPI;
import ht.heist.data.wrappers.ActorEx;
import ht.heist.queries.NpcQuery;
import ht.heist.queries.PlayerQuery;
import net.runelite.api.*;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Actor API
 */
public class ActorAPI
{
    /**
     * find the actor currently in combat with the local player
     * @return the actor, or null if none found
     */
    @Deprecated
    public static ActorEx<?> getInCombatWith()
    {
        Client client = Static.getClient();
        return ActorEx.fromActor(client.getLocalPlayer()).getInCombatWith();
    }
}
