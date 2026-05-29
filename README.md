<div align="center">
  <img src="src/main/resources/icon.png" alt="BeatBounce Icon" width="128">
  <h1>BeatBounce</h1>
  <p>A dynamic, procedurally-generated rhythm game built in Java.</p>

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)](#)
</div>

---

BeatBounce is a high-performance rhythm game developed in Java. By combining real-time Digital Signal Processing (DSP)
with procedural generation, it transforms any audio track into a unique interactive experience. Players navigate a
sphere through a 3D-projected environment, timing their movements to the detected beats and frequency shifts of the
music.

<br>
<div align="center">
  <img src="src/main/resources/gameplay.png" alt="BeatBounce Gameplay" width="90%" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.5);">
  <br>
</div>

<br>

---

## 🚀 Key Features

- **🎹 Intelligent Level Generation**: Automatically maps level geometry to BPM, spectral flux, and detected song
  sections (Intro, Chorus, etc.).
- **🎵 Audius Integration**: Stream and analyze millions of tracks directly from the
  decentralized [Audius](https://audius.co/) network.
- **⚡ High-Performance Rendering**: Custom-built Swing-based engine with hardware acceleration (OpenGL/Direct3D) and
  High DPI support.
- **🏆 Progression & Achievements**: Dynamic scoring system with combo multipliers, global high scores, and unlockable
  achievements.
- **🎧 Broad Format Support**: Native support for MP3, OGG, and FLAC via specialized SPI providers.

---

## 🛠 Tech Stack

| Category         | Technology                              |
|:-----------------|:----------------------------------------|
| **Language**     | Java 25                                 |
| **Build Tool**   | Maven 3.9+                              |
| **Audio Engine** | TarsosDSP, MP3SPI, VorbisSPI, JFLAC     |
| **Graphics**     | Java Swing (Custom 2D-to-3D projection) |
| **Data**         | Jackson Databind (JSON)                 |
| **Testing**      | JUnit 5, Mockito                        |

---

## 🏗 Architecture & Data Flow

BeatBounce utilizes a modular architecture to decouple high-latency audio analysis from the low-latency game loop.

### 🎼 Audio Analysis Pipeline

The analysis occurs in a separate thread pool to prevent UI blocking, using a multi-pass approach to identify musical
structures.

```mermaid
sequenceDiagram
    autonumber
    participant LG as LevelGenerator
    participant AA as AudioAnalyzer
    participant BPM as BpmDetector
    participant AP as AudioProcessor (DSP)
    participant SD as SectionDetector
    participant GC as GenerationContext

    LG->>AA: analyze()
    activate AA
    AA->>BPM: detectTempo(samples)
    Note right of BPM: FFT + Autocorrelation
    BPM-->>AA: TempoMap (BPM Grid)
    
    loop Sliding Window (Buffer + Overlap)
        AA->>AP: processChunk(chunk)
        AP->>AP: Compute Spectral Flux
        AP-->>AA: BeatEvent (Onset, Energy, Band)
    end
    
    AA->>SD: detectSections(events)
    Note right of SD: Segmenting Intro, Verse, Chorus
    SD-->>AA: Section Changes
    
    AA->>AA: Post-process & Sort Events
    AA-->>LG: Refined Event List
    deactivate AA
    
    LG->>GC: generate(Events, TempoMap)
    activate GC
    GC->>GC: Apply Difficulty Profile
    GC->>GC: Place Tiles (Normal, Long, Moving)
    GC-->>LG: Level Object (JSON serializable)
    deactivate GC
```

### 🎮 Game Execution (Activity Loop)

The game engine synchronizes visual updates with the audio timestamp using `System.nanoTime()` for micro-second
precision.

```mermaid
flowchart TD
    Start([Frame Update Event]) --> Sync[Sync Smoothed Audio Time]
    Sync --> Interp[Interpolate Audio Time via System.nanoTime]
    Interp --> State{Game State?}

    State -- COUNTDOWN --> UpdateCD[Update Countdown Timer]
    UpdateCD --> CD_Zero{Timer <= 0?}
    CD_Zero -- Yes --> StartMusic[Start Audio Clip & Set Playing]
    CD_Zero -- No --> EndFrame

    State -- PLAYING --> UpdateZ[Advance Game Z-Progress]
    UpdateZ --> TileMotion[Update Moving/Breakable Tiles]
    TileMotion --> Collision{Check Collision Engine}
    
    Collision -- On Tile --> CalcJump[Calculate Parabolic Path to Next Tile]
    CalcJump --> IncScore[Increment Score & Combo]
    IncScore --> WinCheck{Is Last Tile?}
    WinCheck -- Yes --> EndAnim[Transition to LEVEL_END_ANIMATION]
    WinCheck -- No --> EndFrame
    
    Collision -- Missed / Y < 0 --> Fall[Transition to FALLING State]
    Fall --> StopMusic[Stop Audio Clip]
    
    State -- FALLING --> ApplyGravity[Apply Downward Velocity to Sphere]
    ApplyGravity --> ReviveEligible{Can Revive?}
    ReviveEligible -- Yes --> Prompt[Show Revive UI]
    ReviveEligible -- No --> GameOver[Show Game Over Screen]
    
    StartMusic --> EndFrame
    EndAnim --> EndFrame
    Prompt --> EndFrame
    GameOver --> EndFrame
    EndFrame([End of Frame])
```

---

## 📂 Project Structure

```text
src
+---main
|   +---java
|   |   \---cz.matysekxx.beatbounce
|   |       +---achievements  # Logic for unlockable milestones
|   |       +---api           # Audius Discovery Provider integration
|   |       +---configuration # App-wide settings and hardware tweaks
|   |       +---controller    # Input handling (Keyboard/Mouse)
|   |       +---gui           # Custom-rendered Swing screens and components
|   |       +---model         # Domain logic: Audio DSP, Entities, Game Engine
|   |       \---util          # Mathematical helpers and scaling utilities
|   \---resources             # Static assets: MP3s, icons, level metadata
\---test                      # Comprehensive unit tests (JUnit 5)
```

---

## ⚙️ Installation & Running

### Prerequisites

- **JDK 25+** (Required for the latest language features)
- **Maven 3.9+**

### Steps

1. **Build:** `mvn clean package`
2. **Run:** `java -jar target/cz.matysekxx.beatbounce-1.0-SNAPSHOT.jar`

---
