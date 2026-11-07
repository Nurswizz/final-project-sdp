# Final Project SDP
# Topic: Music Streaming Platform
## Students: Nursultan Tumenbay, Abylai Utemis, Nurshakh Sarybek

Patterns: Facade (music library access), Observer (live track updates, lyrics display), Decorator (audio effects on the stream), Factory (creating effects), Strategy (playback order), Command (user actions and undo)

Architecture and folder structure: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

The diagram shows Facade, Observer and Decorator. Factory, Strategy and Command are described in the architecture file

![architecture](docs/architecture.png)

Run:

```
./mvnw -q compile exec:java
./mvnw test
```

Commands:

```
list | search <text>
play <id> | pause | resume | stop | next
queue [<id>] | order sequential|shuffle|repeat
add <effect> | remove <effect> | effects | undo
now | history | exit
effects: volume_boost, fade_in, echo, bass_boost
```
