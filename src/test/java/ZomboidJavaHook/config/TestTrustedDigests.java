package ZomboidJavaHook.config;

import ZomboidJavaHook.config.TrustedDigests;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TestTrustedDigests {
    byte[] randBuf = new byte[256];
    Random rng = new Random();

    @Test
    void testCorrectFileTrust() throws Exception {
        var constructor = TrustedDigests.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var td = constructor.newInstance();
        var tmp = Files.createTempDirectory("tmp");
        tmp.toFile().deleteOnExit();
        for (var i = 0; i < 10; ++i) {
            rng.nextBytes(randBuf);
            Files.write(tmp.resolve(i + ".txt"), randBuf);
        }
        td.addTrusted(tmp);
        assertTrue(td.isTrusted(tmp));
        rng.nextBytes(randBuf);
        Files.write(tmp.resolve("untrusted.txt"), randBuf);
        assertFalse(td.isTrusted(tmp));
        var bytes = new ByteArrayOutputStream();
        try (var serial = new ObjectOutputStream(bytes)) {
            serial.writeObject(td);
        }
        var deSerial = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        td = (TrustedDigests) deSerial.readObject();
        assertFalse(td.isTrusted(tmp));
        Files.delete(tmp.resolve("untrusted.txt"));
        assertTrue(td.isTrusted(tmp));
    }
}