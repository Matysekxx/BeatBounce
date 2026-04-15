package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AudioAnalyzer {
    private final AudioData audioData;
    private final float speedMultiplier;

    public AudioAnalyzer(AudioData audioData, float speedMultiplier) {
        this.audioData = audioData;
        this.speedMultiplier = speedMultiplier;
    }

    public List<BeatEvent> analyze() {
        final List<BeatEvent> beatEvents = Collections.synchronizedList(new ArrayList<>());

        final AudioProcessor processor = new AudioProcessor(
                audioData.format(),
                speedMultiplier,
                beatEvents::add
        );

        final short[] samples = audioData.samples();
        final int chunkSize = 1024;
        for (int i = 0; i < samples.length; i += chunkSize) {
            final short[] chunk = new short[chunkSize];
            final int end = Math.min(i + chunkSize, samples.length);
            System.arraycopy(samples, i, chunk, 0, end - i);
            processor.processChunk(chunk);
        }

        beatEvents.sort(Comparator.comparingDouble(BeatEvent::timestamp));
        System.out.println("Analyzing done");
        ;
        return beatEvents;
    }
}