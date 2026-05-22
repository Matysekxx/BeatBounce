package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link SectionDetector}.
 */
public class SectionDetectorTest {

    @Test
    void testDetectSectionsWithEmptyEvents() {
        SectionDetector detector = new SectionDetector();
        List<SectionDetector.SongSection> sections = detector.detectSections(new ArrayList<>(), 100.0);

        assertEquals(1, sections.size());
        assertEquals(SectionDetector.SectionType.VERSE, sections.getFirst().type());
        assertEquals(0.0, sections.getFirst().startTime());
        assertEquals(100.0, sections.getFirst().endTime());
    }

    @Test
    void testDetectSectionsWithEvents() {
        SectionDetector detector = new SectionDetector();
        List<BeatEvent> events = new ArrayList<>();

        for (double t = 0; t < 50; t += 0.5) {
            events.add(BeatEvent.ofClassified(t, EventType.BEAT_HIHAT, 0.2, "HIGH"));
        }
        for (double t = 50; t < 100; t += 0.5) {
            events.add(BeatEvent.ofClassified(t, EventType.BEAT_KICK, 0.9, "BASS"));
        }

        List<SectionDetector.SongSection> sections = detector.detectSections(events, 100.0);

        assertFalse(sections.isEmpty());
        boolean hasIntro = sections.stream().anyMatch(s -> s.type() == SectionDetector.SectionType.INTRO);
        boolean hasChorus = sections.stream().anyMatch(s -> s.type() == SectionDetector.SectionType.CHORUS);

        assertTrue(hasIntro, "Should detect intro at the beginning");
        assertTrue(hasChorus, "Should detect chorus for high energy segments");
    }

    @Test
    void testMergeSimilarSections() {
        SectionDetector detector = new SectionDetector();
        List<BeatEvent> events = new ArrayList<>();
        for (double t = 0; t < 20; t += 0.5) {
            events.add(BeatEvent.ofClassified(t, EventType.BEAT_KICK, 0.5, "BASS"));
        }

        List<SectionDetector.SongSection> sections = detector.detectSections(events, 20.0);

        long verseCount = sections.stream().filter(s -> s.type() == SectionDetector.SectionType.VERSE).count();
        assertTrue(verseCount <= 1, "Consecutive similar sections should be merged");
    }
}
