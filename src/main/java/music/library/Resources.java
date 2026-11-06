package music.library;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

final class Resources {

    private Resources() {
    }

    static Optional<List<String>> lines(String path) {
        try (InputStream in = Resources.class.getResourceAsStream(path)) {
            if (in == null) {
                return Optional.empty();
            }
            return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
