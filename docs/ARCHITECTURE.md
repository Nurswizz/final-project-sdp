# Music Streaming Platform: architecture

Final Project SDP. Students: Nursultan Tumenbay, Abylai Utemis, Nurshakh Sarybek

Java 17, Maven, JUnit 5. No external libraries: sound is played with `javax.sound.sampled`, which is part of the JDK

## 1. Idea

A small music player. The user searches the library, plays a track, hears it with live audio effects (echo, bass boost and so on) and sees the lyrics line change in time with the music

## 2. Patterns

| Pattern | Where | What it does in the project | Owner (proposal) |
|---|---|---|---|
| Facade | `facade` | `MusicStreamingFacade` gives the UI one simple API over the catalog, lyrics, audio library and player | Nursultan Tumenbay |
| Observer | `events` | the player publishes playback events, listeners (lyrics display, now playing bar, history) react to them | Abylai Utemis |
| Decorator | `audio.effects` | effects wrap an `AudioStream` and change its samples, they can be stacked in any order at runtime | Nurshakh Sarybek |

The owners are only a proposal, the team can swap them

## 3. Folder structure

```
music-streaming-sdp/
├── pom.xml, mvnw, mvnw.cmd, .mvn/
├── README.md
├── docs/
│   ├── ARCHITECTURE.md            this file
│   ├── architecture.png           overview diagram
│   ├── uml/                       class diagrams for the report
│   ├── screenshots/               screenshots of the running app for the report
│   └── report/                    final report pdf
├── src/main/java/music/
│   ├── app/                       MusicApp (main, composition root), ConsoleUi
│   ├── model/                     Track, LyricLine, Lyrics                              (shared, done)
│   ├── facade/                    MusicStreamingFacade                                  FACADE
│   ├── library/                   TrackCatalog, LyricsRepository, LrcParser, AudioLibrary  (facade subsystems)
│   ├── playback/                  Player                                                 (facade subsystem, event source)
│   ├── audio/                     AudioStream, AudioOutput                              (shared, done)
│   │                              ToneTrackStream, SpeakerOutput, SilentOutput
│   │   └── effects/               EffectType (done), AudioEffect, VolumeBoostEffect,     DECORATOR
│   │                              FadeInEffect, EchoEffect, BassBoostEffect, EffectChain
│   └── events/                    PlaybackEvent, PlaybackEventType, PlaybackListener,   OBSERVER
│       │                          PlaybackSubject (done), PlaybackEventPublisher
│       └── listeners/             LyricsDisplay, NowPlayingBar, ListeningHistory
├── src/main/resources/
│   ├── tracks/                    track list (tracks are generated in code, no copyright problems)
│   └── lyrics/                    .lrc files written by us
└── src/test/java/music/           tests, same packages as main
```

Files marked "done" already exist and compile. They are the shared contracts, everybody writes code against them, so the three parts fit together without waiting for each other

## 4. How the patterns work together

```
ConsoleUi ──> MusicStreamingFacade ──> TrackCatalog        (search)
                     │            ──> LyricsRepository    (lyrics of a track)
                     │            ──> AudioLibrary        (raw AudioStream of a track)
                     │            ──> EffectChain         (wraps the stream in effects)   DECORATOR
                     │            ──> Player              (plays the wrapped stream)
                     │                   │
                     │                   └──> PlaybackEventPublisher ──> LyricsDisplay    OBSERVER
                     │                                               ──> NowPlayingBar
                     │                                               ──> ListeningHistory
                     └──> subscribe(type, listener)  (the UI adds listeners through the facade)
```

Play a track, step by step:

1. `ConsoleUi` calls `facade.play("t1")`
2. the facade gets the raw stream from `AudioLibrary`: `ToneTrackStream`
3. the facade asks `EffectChain` to wrap it with the active effects, for example `new EchoEffect(new BassBoostEffect(raw))`
4. the facade gives the wrapped stream to `Player`. The player only knows `AudioStream`, it does not know which effects are inside
5. the player reads samples in a loop on its own thread and writes them to `AudioOutput` (speakers or silent in tests)
6. while playing, the player publishes `TRACK_STARTED`, `POSITION_CHANGED` (about every 100 ms), `TRACK_FINISHED` and so on
7. `LyricsDisplay` listens to `POSITION_CHANGED` and prints the lyric line whose time has come

Change an effect while the music plays: `facade.addEffect(EffectType.ECHO)` makes `EffectChain` build a new wrapped stream, the player switches to it and publishes `EFFECTS_CHANGED`

## 5. Contracts

### Facade (Nursultan): `facade.MusicStreamingFacade`

```java
List<Track> search(String query)
void play(String trackId)
void pause()
void resume()
void stop()
void addEffect(EffectType effect)
void removeEffect(EffectType effect)
List<EffectType> activeEffects()
Optional<Lyrics> lyricsOf(String trackId)
void subscribe(PlaybackEventType type, PlaybackListener listener)
void unsubscribe(PlaybackEventType type, PlaybackListener listener)
```

Subsystems behind it (the UI never touches them directly):

- `TrackCatalog`: list of tracks, search by title or artist
- `LyricsRepository` + `LrcParser`: reads `resources/lyrics/<trackId>.lrc` (lines like `[00:12.50] text`)
- `AudioLibrary`: opens the raw `AudioStream` of a track
- `Player`: plays a stream, keeps the position, publishes events

### Decorator (Nurshakh): `audio`, `audio.effects`

- `AudioStream` (component, done): `int sampleRate()`, `int read(float[] buffer)` returns how many samples were read or -1 at the end
- `ToneTrackStream` (concrete component): generates the music of a track from notes (sine waves), so we need no audio files
- `AudioEffect` (base decorator): implements `AudioStream`, keeps the wrapped stream, `sampleRate()` goes to it
- concrete decorators, each changes the samples after reading them from the inner stream:
  - `VolumeBoostEffect`: multiplies every sample, clips to -1..1
  - `FadeInEffect`: volume grows from 0 to 1 in the first seconds
  - `EchoEffect`: adds the sound from some milliseconds ago, quieter
  - `BassBoostEffect`: simple low pass filter mixed back in
- `EffectChain`: keeps the list of active `EffectType`s and wraps a raw stream in them
- `AudioOutput` (done): `SpeakerOutput` plays through `javax.sound.sampled`, `SilentOutput` is for tests

### Observer (Abylai): `events`, `events.listeners`

- `PlaybackSubject` (subject, done): `subscribe` and `unsubscribe` per `PlaybackEventType`
- `PlaybackListener` (observer, done): `onEvent(PlaybackEvent)`
- `PlaybackEvent` (done): immutable record with type, track and position (push model, the event carries all the data)
- `PlaybackEventPublisher`: implements `PlaybackSubject`, thread safe (`ConcurrentHashMap` + `CopyOnWriteArrayList`), the player calls `publish(event)`
- listeners:
  - `LyricsDisplay`: shows the current lyric line
  - `NowPlayingBar`: shows title, artist and a progress bar `[#####-----] 1:12 / 3:05`
  - `ListeningHistory`: remembers finished tracks

## 6. Rules

- `app` knows only `facade`, `model`, `events` (to create listeners) and `audio.effects.EffectType`
- `audio` and `events` do not know about each other, only the facade and the player connect them
- `library` does not know about `playback`, `audio.effects` or `events`
- one composition root: `MusicApp.main` creates everything and passes objects through constructors
- the player plays on its own thread, so listeners must not block for a long time

## 7. Plan

1. everybody: read this file, agree on the owners
2. each owner: write the classes of the pattern and tests in their package
3. Nursultan: facade and console UI once the parts are ready, the facade can be written earlier with fake subsystems
4. all: run together, take screenshots into `docs/screenshots`, draw class diagrams into `docs/uml`
5. report (pdf): introduction, body (one section per pattern with code, uml and screenshots), conclusion, further work

Every student commits their own pattern from their own GitHub account, so the history shows who did what
