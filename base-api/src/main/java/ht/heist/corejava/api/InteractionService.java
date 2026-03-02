package ht.heist.corejava.api;

public interface InteractionService {
    /**
     * Interacts with a scene object (e.g. "Climb-down" Staircase, "Open" Door)
     * 
     * @param objectName The precise name of the object.
     * @param action     The action to perform.
     * @return true if the object was found and clicked.
     */
    boolean interactObject(String objectName, String action);

    /**
     * Interacts with an NPC.
     * 
     * @param npcName The name of the NPC.
     * @param action  The action to perform.
     * @return true if the NPC was found and clicked.
     */
    boolean interactNPC(String npcName, String action);

    /**
     * Interacts with an item on the ground.
     * 
     * @param itemName The name of the item.
     * @param action   The action (e.g., "Take").
     * @return true if the item was found and clicked.
     */
    boolean interactGroundItem(String itemName, String action);

    /**
     * Handles NPC/Player chat and options dialogues.
     * 
     * @param optionToSelect The specific text to select (e.g., "Climb up").
     *                       If null or empty, it will click "Click here to
     *                       continue".
     * @return true if a dialogue was found and processed.
     */
    boolean handleDialogue(String optionToSelect);
}
