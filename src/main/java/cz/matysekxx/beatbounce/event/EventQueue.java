package cz.matysekxx.beatbounce.event;

import cz.matysekxx.beatbounce.DIContainer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {
    static {
        DIContainer.register(EventQueue.class, new EventQueue());
    }
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    public void enqueue(Event event) {
        eventQueue.add(event);
    }

    public Event take() throws InterruptedException {
        return eventQueue.take();
    }
}
