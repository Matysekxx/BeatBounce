package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;

/**
 * A reactive subscriber implementation of the Java Flow API that collects
 * {@link BeatEvent}s emitted by the {@link AudioProcessor}.
 * <p>
 * This class acts as a bridge between the asynchronous event stream produced by the
 * processing engine and the synchronous orchestration in {@link AudioAnalyzer}. It utilizes
 * a {@link CountDownLatch} to signal the completion or failure of the audio stream,
 * allowing the analyzer thread to safely block until all chunks are processed.
 * </p>
 *
 * @author Matysekxx
 */
public class AudioSubscriber implements Flow.Subscriber<BeatEvent> {

    /**
     * The thread-safe list where all intercepted beat and intensity events are collected.
     */
    private final List<BeatEvent> beatEvents;

    /**
     * A synchronization aid used to unblock the managing thread once the stream terminates.
     */
    private final CountDownLatch latch;

    /**
     * Constructs a new AudioSubscriber with a target collection list and a synchronization latch.
     *
     * @param beatEvents a thread-safe list to store the detected events
     * @param latch      the latch used to synchronize the completion of the reactive stream
     */
    public AudioSubscriber(List<BeatEvent> beatEvents, CountDownLatch latch) {
        this.beatEvents = beatEvents;
        this.latch = latch;
    }

    /**
     * Invoked when the subscriber is successfully linked to the publisher.
     * Immediately requests an unbounded number of items, effectively bypassing backpressure
     * since the audio file is processed locally and sequentially.
     *
     * @param subscription the subscription representing the link between publisher and subscriber
     */
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    /**
     * Invoked whenever the publisher emits a newly detected and classified {@link BeatEvent}.
     * Appends the received event to the shared event list.
     *
     * @param item the classified beat or intensity event emitted by the processor
     */
    @Override
    public void onNext(BeatEvent item) {
        beatEvents.add(item);
    }

    /**
     * Invoked when an unrecoverable error occurs during audio processing.
     * Counts down the latch to ensure the main analysis thread does not hang indefinitely.
     *
     * @param throwable the exception that caused the stream to fail
     */
    @Override
    public void onError(Throwable throwable) {
        latch.countDown();
    }

    /**
     * Invoked when the publisher has finished processing all audio chunks and closes the stream.
     * Counts down the latch to zero, releasing the waiting thread in {@link AudioAnalyzer}.
     */
    @Override
    public void onComplete() {
        latch.countDown();
    }
}