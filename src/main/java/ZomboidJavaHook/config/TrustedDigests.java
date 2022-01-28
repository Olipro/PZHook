package ZomboidJavaHook.config;

import net.uptheinter.interceptify.util.Util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class TrustedDigests implements Serializable {
    private byte[][] rawHashes;
    private transient Set<ByteBuffer> hashes = new HashSet<>();
    private transient MessageDigest sha256 = SHA256();
    private static final String trustBin = "zhdata.bin";
    public static final Path trustDir = Path.of(System.getProperty("user.home"), "Zomboid");

    private TrustedDigests() {}

    public static <R> R transaction(Function<TrustedDigests, R> func) throws IOException, ClassNotFoundException {
        TrustedDigests inst;
        if (!Files.exists(trustDir) || !Files.exists(trustDir.resolve("zhdata.bin"))) {
            inst = new TrustedDigests();
        } else {
            try (var file = new FileInputStream(trustDir.resolve("zhdata.bin").toFile());
                 var strm = new ObjectInputStream(file)) {
                inst = (TrustedDigests) strm.readObject();
            }
        }
        try {
            return func.apply(inst);
        } finally {
            inst.close();
        }
    }

    private void close() throws IOException {
        if (!Files.exists(trustDir))
            Files.createDirectory(trustDir);
        var file = trustDir.resolve(trustBin).toFile();
        try (var strm = new FileOutputStream(file);
             var objw = new ObjectOutputStream(strm)) {
            objw.writeObject(this);
        }
    }

    private static MessageDigest SHA256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Util.DebugError(e);
        }
        // Should be impossible, but we're in trouble if it happens, so terminate.
        Runtime.getRuntime().halt(1);
        return null;
    }

    public boolean isTrusted(Path folderToCheck) {
        return Util.walk(folderToCheck, 1)
                .distinct()
                .filter(Files::isRegularFile)
                .allMatch(this::wasDigestFound);
    }

    public void addTrusted(Path folderToAdd) {
        // Note: it is entirely possible for someone malicious to
        // change the folder between the user reviewing the list
        // and approving the mods. However, if someone can do that,
        // why bother? they can run code and you're already fucked.
        Util.walk(folderToAdd, 1)
                .filter(Files::isRegularFile)
                .map(this::digestFile)
                .forEach(hash -> hashes.add(hash));
    }

    private static final byte[] throwaway = new byte[4096]; // 1 page of memory

    private ByteBuffer digestFile(Path file) {
        try (var strm = new FileInputStream(file.toFile());
             var digest = new DigestInputStream(strm, sha256)) {
            //noinspection StatementWithEmptyBody
            while(digest.read(throwaway, 0, throwaway.length) == throwaway.length);
            return ByteBuffer.wrap(digest.getMessageDigest().digest());
        } catch (IOException e) {
            Util.DebugError(e);
        }
        return null;
    }

    private boolean wasDigestFound(Path file) {
        return hashes.contains(digestFile(file));
    }

    public void clear() {
        rawHashes = null;
        hashes.clear();
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        rawHashes = new byte[hashes.size()][];
        var i = 0;
        for (var arr : hashes)
            rawHashes[i++] = arr.array();
        return this;
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        hashes = new HashSet<>();
        sha256 = SHA256();
        Arrays.stream(rawHashes)
                .map(ByteBuffer::wrap)
                .forEach(hashes::add);
    }
}
