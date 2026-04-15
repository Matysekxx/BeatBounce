package cz.matysekxx.beatbounce.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {

    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    private EventQueue() {
    }

    public void enqueue(Event event) {
        eventQueue.add(event);
    }

    public Event take() throws InterruptedException {
        return eventQueue.take();
    }
}
