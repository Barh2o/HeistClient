package ht.heist.corejava.api;

/**
 * Interface representing a single logical step in a quest flow.
 */
public interface QuestState {
    /**
     * Executes the logic for this state on the current game tick.
     * 
     * @param context The generic game context.
     */
    void execute(QuestContext context);

    /**
     * Evaluates whether this state is complete and returns the next state.
     * 
     * @param context The generic game context.
     * @return The next state to transition to, or this state if not yet complete.
     */
    QuestState nextState(QuestContext context);
}
