package ht.heist.corejava.api;

/**
 * Manages the active QuestState and handles state transitions.
 */
public class QuestEngine {
    private QuestState currentState;

    public void start(QuestState initialState) {
        this.currentState = initialState;
    }

    public void stop() {
        this.currentState = null;
    }

    public void tick(QuestContext context) {
        if (currentState == null) {
            return;
        }

        // Evaluate if we should transition to a new state
        QuestState next = currentState.nextState(context);
        if (next != currentState) {
            currentState = next;
        }

        // Execute the current state (might be the new one if we just transitioned)
        if (currentState != null) {
            currentState.execute(context);
        }
    }

    public QuestState getCurrentState() {
        return currentState;
    }
}
