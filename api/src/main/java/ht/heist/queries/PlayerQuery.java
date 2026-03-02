package ht.heist.queries;

import ht.heist.data.wrappers.PlayerEx;
import ht.heist.queries.abstractions.AbstractActorQuery;
import ht.heist.services.GameManager;
import net.runelite.api.Player;

/**
 * A query class for filtering and retrieving Player entities in the game.
 */
public class PlayerQuery extends AbstractActorQuery<PlayerEx, PlayerQuery>
{
    /**
     * Constructs a new PlayerQuery instance.
     * Initializes the query with the list of all players from the GameManager.
     */
    public PlayerQuery() {
        super(GameManager.playerList());
    }
}
