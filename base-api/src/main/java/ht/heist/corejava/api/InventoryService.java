package ht.heist.corejava.api;

/**
 * Inventory query and interaction service.
 */
public interface InventoryService {
    /**
     * Checks if the inventory contains at least one of the specified item ID.
     * 
     * @param itemId The RuneLite ItemID.
     * @return true if the item exists in the inventory.
     */
    boolean hasItem(int itemId);

    /**
     * @return The number of empty slots in the inventory.
     */
    int getEmptySlots();

    /**
     * Attempts to interact with a specific item using a given menu action (e.g.
     * "Drop", "Eat").
     * 
     * @param itemId The RuneLite ItemID.
     * @param action The string action to match against the item options.
     * @return true if the interaction was successfully initiated.
     */
    boolean interactWithItem(int itemId, String action);
}
