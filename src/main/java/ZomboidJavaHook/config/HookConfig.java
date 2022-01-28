package ZomboidJavaHook.config;

import ZomboidJavaHook.zomboid.SteamWorkshop;
import net.uptheinter.interceptify.interfaces.StartupConfig;
import net.uptheinter.interceptify.internal.RuntimeHook;
import net.uptheinter.interceptify.util.JarFiles;
import net.uptheinter.interceptify.util.Util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class HookConfig implements StartupConfig {
    private final Set<ModData> mods = initMods();
    private boolean userCancelled;
    private boolean rememberNextTime;

    public HookConfig() throws ClassNotFoundException {}

    public boolean isUserCancelled() {
        return userCancelled;
    }

    @Override
    public Consumer<String[]> getRealMain() {
        try {
            var main = Class.forName("zombie.gameStates.MainScreenState")
                    .getDeclaredMethod("main", String[].class);
            return args -> {
                try {
                    main.invoke(null, (Object) args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("Couldn't find the main game class!");
        }
    }

    @Override
    public List<URL> getClasspaths() {
        try {
            var allJarFiles = StartupConfig.super.getClasspaths();
            Files.walk(Path.of(""), 1)
                    .filter(f -> f.toFile().getName().endsWith(".jar"))
                    .map(Path::toAbsolutePath)
                    .map(Util::toURL)
                    .forEach(allJarFiles::add);
            allJarFiles.add(Util.toURL(Paths.get("").toAbsolutePath()));
            allJarFiles.add(RuntimeHook.class.getProtectionDomain().getCodeSource().getLocation());
            return allJarFiles;
        } catch (IOException e) {
            Util.DebugError(e);
        }
        return new ArrayList<>();
    }

    private static ModData tryAddModInfo(Path javaDir) {
        var modInfo = javaDir.getParent().resolve("mod.info");
        if (!Files.isRegularFile(modInfo))
            return null;
        List<String> lines;
        try {
            lines = Files.readAllLines(modInfo);
        } catch (IOException e) {
            Util.DebugError(e);
            return null;
        }
        var name = lines.stream()
                .filter(line -> line.startsWith("name"))
                .map(line -> line.substring(line.indexOf('=') + 1))
                .findAny().orElse(null);
        if (name == null)
            return null;
        return new ModData(name, javaDir);
    }

    private Set<ModData> initMods() {
        final var mods = new HashSet<ModData>();
        try (final var steamWorkshop = new SteamWorkshop()) {
            Arrays.stream(steamWorkshop.getInstalledItemFolders())
                    .map(Path::of)
                    .map(path -> path.resolve("mods"))
                    .filter(Files::isDirectory)
                    .flatMap(p -> Util.walk(p, 1)
                            .filter(o -> !o.equals(p)))
                    .map(p -> p.resolve("java"))
                    .filter(Files::isDirectory)
                    .map(HookConfig::tryAddModInfo)
                    .filter(Objects::nonNull)
                    .forEach(mods::add);
            final var localMods = steamWorkshop.getWorkshopFolder();
            Files.walk(localMods, 1)
                    .filter(p -> !p.equals(localMods))
                    .map(p -> p.resolve("Contents").resolve("mods"))
                    .filter(Files::isDirectory)
                    .flatMap(p -> Util.walk(p, 1)
                            .filter(Files::isDirectory)
                            .filter(o -> !o.equals(p)))
                    .flatMap(p -> Util.walk(p, 1)
                            .filter(Files::isDirectory)
                            .filter(o -> !o.equals(p)))
                    .filter(p -> p.endsWith("java"))
                    .map(HookConfig::tryAddModInfo)
                    .filter(Objects::nonNull)
                    .forEach(mods::add);
        } catch (Exception e) {
            Util.DebugError(e);
        }
        return mods;
    }

    public Set<ModData> getMods() {
        return mods;
    }

    @Override
    public JarFiles getJarFilesToInject() {
        var allFiles = new JarFiles();
        mods.stream()
            .filter(ModData::isEnabled)
            .map(ModData::getJarDir)
            .forEach(allFiles::addFromDirectory);
        return allFiles;
    }

    public void setUserCancelled() {
        userCancelled = true;
    }

    public void setRememberNextTime() {
        rememberNextTime = true;
    }

    public boolean getRememberNextTime() {
        return rememberNextTime;
    }
}
