package ZomboidJavaHook.dev;

import ZomboidJavaHook.config.HookConfig;
import net.uptheinter.interceptify.EntryPoint;
import net.uptheinter.interceptify.internal.RuntimeHook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class GameCodePubliciser implements AutoCloseable {
    private final Path codeDir;
    private final FileOutputStream file;
    private final JarOutputStream jar;
    private final ClassFileTransformer transformer;

    public GameCodePubliciser() throws IOException {
        codeDir = Path.of("", "zombie");
        if (!Files.exists(codeDir)) {
            System.err.println("ERROR: can't find 'zombie' folder. Did you run this from the game dir?");
            throw new NotDirectoryException("Can't find zombie folder");
        }
        file = new FileOutputStream(Path.of("", "zombie.jar").toFile());
        jar = new JarOutputStream(file);
        var fakeInstr = new FakeInstrumentation();
        EntryPoint.premain("", fakeInstr);
        RuntimeHook.init(new HookConfig(true));
        transformer = fakeInstr.getTransformer();
    }

    public void dumpCode() throws IOException {
        Files.walk(codeDir)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".class"))
                .forEach(this::addClassFile);
        System.out.println("Done. you can find the file in the game directory with the name zombie.jar");
    }

    private void addClassFile(File file) {
        var path = file.getPath().replace('\\', '/');
        System.out.println(path);
        try {
            var bytes = transformer.transform(ClassLoader.getSystemClassLoader(),
                    path.substring(0, path.length() - 6).replace('/', '.'),
                    null, null, Files.readAllBytes(file.toPath()));
            jar.putNextEntry(new JarEntry(path));
            jar.write(bytes);
        } catch (Throwable e) {
            if (e.getMessage().contains("org.junit"))
                return;
            System.err.println("Failed to write " + file.getPath() + " due to:");
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        jar.closeEntry();
        jar.close();
        file.close();
    }
}
