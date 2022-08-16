package ZomboidJavaHook.zomboid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

import static net.uptheinter.interceptify.util.Util.DebugError;

public class SteamWorkshop implements AutoCloseable {
    private final String[] installedItemFolders;
    private final Path workshopFolder;
    private static final String successString = "PZHook.Success";

    public static void main(String[] args) throws Throwable {
        var out = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));
        var classLoader = new URLClassLoader(new URL[]{Paths.get("").toFile().toURI().toURL()});
        Class.forName("zombie.core.Rand", false, classLoader).getMethod("init").invoke(null);
        var sUtils = Class.forName("zombie.core.znet.SteamUtils", false, classLoader);
        var steamUtilsShutdown = sUtils.getMethod("shutdown");
        var sWorkshop = Class.forName("zombie.core.znet.SteamWorkshop", false, classLoader);
        var steamWorkshopInst = sWorkshop.getField("instance").get(null);
        sUtils.getMethod("init").invoke(null);
        sWorkshop.getMethod("init").invoke(null);
        var installedItems = (String[])sWorkshop.getMethod("GetInstalledItemFolders").invoke(steamWorkshopInst);
        var workshopFolder = (String)sWorkshop.getMethod("getWorkshopFolder").invoke(steamWorkshopInst);
        steamUtilsShutdown.invoke(null);
        out.println(successString);
        Arrays.stream(installedItems).forEach(out::println);
        out.println(workshopFolder);
    }

    private static String getJRE() {
        var home = Path.of(System.getProperty("java.home"), "bin");
        if (Files.isExecutable(home.resolve("java")))
            return home.resolve("java").toFile().getAbsolutePath();
        if (Files.isExecutable(home.resolve("java.exe")))
            return home.resolve("java.exe").toFile().getAbsolutePath();
        return null;
    }

    private AbstractMap.SimpleEntry<String[], Path> getWorkshopPaths() throws IOException {
        var jre = getJRE();
        var runtime = ManagementFactory.getRuntimeMXBean();
        var args = runtime.getClassPath();
        var libs = runtime.getLibraryPath();
        var wdir = Path.of("").toAbsolutePath().toFile();
        var proc = new ProcessBuilder()
            .command(jre, "-Dzomboid.steam=1", "-Djava.library.path=" + libs, "-cp", args, "ZomboidJavaHook.zomboid.SteamWorkshop")
            .directory(wdir)
                .start();
        try (var strm = new InputStreamReader(proc.getInputStream());
             var buf = new BufferedReader(strm)) {
            var wasOK = buf.readLine();
            if (!successString.equals(wasOK)) {
                System.err.println(wasOK);
                buf.lines().forEach(System.out::println);
                new BufferedReader(new InputStreamReader(proc.getErrorStream())).lines().forEach(System.err::println);
                throw new IOException("Did not get success string from spawned process");
            }
            var lines = new ArrayList<String>(30);
            buf.lines().forEach(lines::add);
            var workshopFolder = Path.of(lines.remove(lines.size() - 1));
            var items = lines.toArray(String[]::new);
            return new AbstractMap.SimpleEntry<>(items, workshopFolder);
        }
    }

    public SteamWorkshop() throws Exception {
        var paths= getWorkshopPaths();
        installedItemFolders = paths.getKey();
        workshopFolder = paths.getValue();
    }

    public String[] getInstalledItemFolders() {
        return installedItemFolders;
    }

    public Path getWorkshopFolder() {
        return workshopFolder;
    }

    @Override
    public void close() {
    }
}
