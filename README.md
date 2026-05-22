# BeatBounce

BeatBounce is a dynamic rhythm game developed in Java. It features procedural level generation based on audio analysis, allowing players to experience their favorite music in a unique, interactive way.

---

## 🚀 Features

- **Procedural Level Generation**: Levels are automatically created by analyzing audio tracks (BPM, onsets, frequency bands).
- **Audius API Integration**: Search and stream/download music directly from the [Audius](https://audius.co/) platform.
- **Advanced Audio Analysis**: Utilizes DSP (Digital Signal Processing) with TarsosDSP for accurate beat detection.
- **High Performance Rendering**: Optimized Java Swing GUI with support for High DPI and hardware acceleration (OpenGL/Direct3D).
- **Customizable Experience**: Various tile types (Normal, Moving, Breakable, Long), difficulty profiles, and visual settings.
- **Achievement System**: Track your progress and unlock achievements as you play.
- **Local Music Support**: Play your own MP3, OGG, or FLAC files.

---

## 🛠 Tech Stack

- **Language**: Java 25
- **Build System**: Maven
- **GUI Framework**: Java Swing (Custom components)
- **Audio Processing**: TarsosDSP, MP3SPI, VorbisSPI, JFLAC
- **JSON Processing**: Jackson Databind
- **Testing**: JUnit 5, Mockito

---

## 🏗 Architecture & Data Flow

BeatBounce relies on complex data pipelines to synchronize visual gameplay with audio processing.

### 🎼 Audio Analysis Pipeline

This sequence captures the multi-stage DSP pipeline used to transform raw PCM samples into a procedurally generated level.

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

### 🎮 Game Execution

The following diagram illustrates the high-precision update loop executed for every frame to maintain synchronization and handle physics.

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

## 📋 Prerequisites

- **Java Development Kit (JDK) 25** or higher.
- **Maven** for building the project.

---

## ⚙️ Installation & Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/BeatBounce.git
   cd BeatBounce
   ```

2. **Build the project:**
   ```bash
   mvn clean package
   ```

3. **Run the application:**
   ```bash
   java -jar target/cz.matysekxx.beatbounce-1.0-SNAPSHOT.jar
   ```

---

## 🎮 How to Play

1. **Select a Song**: Choose a song from the Audius library or load a local file.
2. **Analysis**: The game will analyze the song and generate a level.
3. **Gameplay**: Control the sphere to bounce on tiles in sync with the beat. Use your mouse or keyboard (configurable) to navigate.
4. **Collect Orbs**: Pick up orbs for extra points.

---
