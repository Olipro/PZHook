package ZomboidJavaHook.installer;

import ZomboidJavaHook.Main;
import ZomboidJavaHook.mac.Plist;
import ZomboidJavaHook.mac.PlistString;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import net.uptheinter.interceptify.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class HookInstaller {
    private static final File self;
    private static final boolean isWindows = System.getProperty("os.name").contains("Windows");
    private static final boolean isMacOs = System.getProperty("os.name").toLowerCase().contains("mac");

    private final Path dest;
    private final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    static {
        Path tmp = null;
        try {
            tmp = Path.of(HookInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            Util.DebugError(e);
        }
        self = tmp != null ? tmp.toFile() : null;
    }

    public HookInstaller(Path path) {
        dest = path;
    }

    private void patchJson(File file) throws IOException {
        if (!Files.isRegularFile(file.toPath()))
            return;
        ZomboidJson json;
        try (var strm = new FileInputStream(file)) {
            json = jsonb.fromJson(strm, ZomboidJson.class);
            json.mainClass = Main.class.getName().replace('.', '/');
            json.classpath = json.classpath.stream().filter(s -> !s.contains(self.getName())).collect(Collectors.toList());
            json.vmArgs = json.vmArgs.stream().filter(s -> !s.contains("-javaagent:")).collect(Collectors.toList());
            json.vmArgs.add("-javaagent:" + self.getName());
            json.classpath.add(self.getName());
        }
        var dir = file.toPath().getParent();
        var newName = file.getName().replace(".json", ".site.json");
        try (var strm = new FileOutputStream(dir.resolve(newName).toFile())) {
            jsonb.toJson(json, strm);
        }
    }

    private static final byte[] dll = new byte[]{'U','S','E','R','3','2'};
    @SuppressWarnings("SameParameterValue")
    private int findSeq(byte[] haystack, byte[] needle) {
        for (var i = 0; i < haystack.length; ++i)
            for (var j = 0; j < needle.length && i + j < haystack.length; ++j) {
                if (haystack[i + j] != needle[j])
                    break;
                if (j == needle.length - 1)
                    return i + j;
            }
        return -1;
    }

    private void patchEXE(Path file, byte newVal) throws IOException {
        if (!isWindows)
            return;
        var bytes = Files.readAllBytes(file);
        var idx = findSeq(bytes, dll);
        if (idx != -1) {
            bytes[idx] = newVal;
            Files.write(file, bytes);
        }
    }

    private void writeStubs(String... names) {
        if (!isWindows)
            return;
        Arrays.stream(names).forEach(name -> {
            try (var in = getClass().getResourceAsStream(name);
                 var out = new FileOutputStream(dest.resolve(name).toFile())) {
                assert in != null;
                in.transferTo(out);
            } catch (IOException e) {
                Util.DebugError(e);
            }
        });
    }

    private void patchPlist() throws Exception {
        if (!isMacOs)
            return;
        var path = dest.getParent().getParent().resolve("Info.plist");
        if (!Files.isRegularFile(path))
            return;
        Plist plist;
        try (var in = new FileInputStream(path.toFile())) {
            plist = Plist.deserialise(in);
        }
        var opts = plist.getDict().get("JVMOptions").getArray();
        if (opts.stream().anyMatch(v -> v.getString().contains("-javaagent:")))
            return;
        opts.add(new PlistString("-javaagent:" + self.getName()));
        try (var out = new FileOutputStream(path.toFile())) {
            Plist.serialise(plist, out);
        }
    }

    public void install() {
        try (var from = new FileInputStream(self);
             var to = new FileOutputStream(dest.resolve(self.getName()).toFile())) {
            from.transferTo(to);
            writeStubs("User34.dll", "User30.dll");
            patchEXE(dest.resolve("ProjectZomboid32.exe"), (byte) '0');
            patchJson(dest.resolve("ProjectZomboid32.json").toFile());
            patchEXE(dest.resolve("ProjectZomboid64.exe"), (byte) '4');
            patchJson(dest.resolve("ProjectZomboid64.json").toFile());
            patchPlist();
        } catch (Exception e) {
            Util.DebugError(e);
        }
    }
}
