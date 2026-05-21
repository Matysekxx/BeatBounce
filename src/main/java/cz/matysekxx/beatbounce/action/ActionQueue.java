package cz.matysekxx.beatbounce.action;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ActionQueue {
    private static final ActionQueue instance = new ActionQueue();
    private final ConcurrentLinkedQueue<Runnable> actionQueue = new ConcurrentLinkedQueue<>();

    private ActionQueue() {}

    public static ActionQueue getSingleton() {
        return instance;
    }

    public void add(Runnable action) {
        actionQueue.add(action);
    }

    public void processActions() {
        while (!actionQueue.isEmpty()) {
            actionQueue.poll().run();
        }
    }

    public void clear() {
        actionQueue.clear();
    }
}
