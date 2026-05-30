package cz.matysekxx.beatbounce.action;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A thread-safe singleton queue for managing and deferred execution of UI and game actions.
 * This class ensures that actions requested from various threads (e.g., UI events)
 * are processed in a synchronized manner within the main game loop.
 *
 * @author Matysekxx
 */
public class ActionQueue {
    /**
     * The singleton instance of the ActionQueue.
     */
    private static final ActionQueue instance = new ActionQueue();

    /**
     * The underlying thread-safe queue containing actions to be executed.
     */
    private final ConcurrentLinkedQueue<Runnable> actionQueue = new ConcurrentLinkedQueue<>();

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private ActionQueue() {
    }

    /**
     * Retrieves the singleton instance of the {@code ActionQueue}.
     *
     * @return the {@code ActionQueue} instance
     */
    public static ActionQueue getSingleton() {
        return instance;
    }

    /**
     * Adds a new action to the queue for deferred execution.
     *
     * @param action the {@link Runnable} to be executed
     */
    public void add(Runnable action) {
        actionQueue.add(action);
    }

    /**
     * Processes and executes all queued actions in the order they were added.
     * This should typically be called from the main game loop.
     */
    public void processActions() {
        while (!actionQueue.isEmpty()) {
            actionQueue.poll().run();
        }
    }

    /**
     * Clears all pending actions from the queue.
     */
    public void clear() {
        actionQueue.clear();
    }
}
