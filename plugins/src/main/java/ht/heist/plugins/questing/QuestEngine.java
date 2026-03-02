package ht.heist.plugins.questing;

import ht.heist.Logger;
import ht.heist.api.widgets.InventoryAPI;
import ht.heist.api.game.MovementAPI;
import ht.heist.api.game.SceneAPI;
import ht.heist.services.pathfinder.Walker;
import ht.heist.data.wrappers.PlayerEx;
import ht.heist.data.wrappers.TileItemEx;
import ht.heist.queries.TileItemQuery;
import net.runelite.api.coords.WorldPoint;
import java.util.function.BooleanSupplier;

/**
 * QuestEngine handles the high-level state machine for questing.
 * It integrates Walker, Inventory, and Scene APIs.
 */
public class QuestEngine {

    public enum State {
        GATHERING,
        INTERACTING,
        WALKING,
        IDLE,
        ERROR
    }

    private State currentState = State.IDLE;

    /**
     * Walk to a destination but stop if a condition is met (e.g. item appeared).
     */
    public boolean walkConditional(WorldPoint target, BooleanSupplier stopCondition) {
        currentState = State.WALKING;
        Logger.info("Walking to quest objective: " + target);
        return Walker.walkTo(target, stopCondition);
    }

    /**
     * Gather an item from the ground if it's nearby.
     */
    public boolean gatherItem(int itemId, int radius) {
        currentState = State.GATHERING;
        TileItemEx groundItem = new TileItemQuery().withId(itemId).nearest();
        if (groundItem != null
                && groundItem.getWorldPoint().distanceTo(PlayerEx.getLocal().getWorldPoint()) <= radius) {
            Logger.info("Gathering quest item: " + itemId);
            groundItem.interact("Take");
            return true;
        }
        return false;
    }

    public State getState() {
        return currentState;
    }
}
