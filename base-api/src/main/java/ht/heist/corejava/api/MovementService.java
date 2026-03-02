package ht.heist.corejava.api;

/**
 * High-level intent-based movement service.
 */
public interface MovementService {
    /**
     * Attempts to walk to the specified world coordinates.
     * 
     * @param target The target world coordinates.
     * @return true if the movement action was successfully initiated.
     */
    boolean walkTo(HeistPoint target);

    /**
     * @return true if the player is currently moving/walking/running.
     */
    boolean isMoving();

    /**
     * Toggles the run state on/off.
     */
    void toggleRun();
}
