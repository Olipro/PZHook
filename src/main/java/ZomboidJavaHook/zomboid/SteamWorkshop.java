package ZomboidJavaHook.zomboid;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.uptheinter.interceptify.util.Util.DebugError;

public class SteamWorkshop implements AutoCloseable {
    private final ClassLoader classLoader;
    private final Method steamUtilsShutdown;
    private final Method getInstalledItemFolders;
    private final Method getWorkshopFolder;
    private final Object steamWorkshopInst;

    public SteamWorkshop() throws Exception {
        classLoader = new URLClassLoader(new URL[]{Paths.get("").toFile().toURI().toURL()}, getClass().getClassLoader());
        var sUtils = Class.forName("zombie.core.znet.SteamUtils", false, classLoader);
        steamUtilsShutdown = sUtils.getMethod("shutdown");
        var sWorkshop = Class.forName("zombie.core.znet.SteamWorkshop", false, classLoader);
        getInstalledItemFolders = sWorkshop.getMethod("GetInstalledItemFolders");
        getWorkshopFolder = sWorkshop.getMethod("getWorkshopFolder");
        sUtils.getMethod("init").invoke(null);
        sWorkshop.getMethod("init").invoke(null);
        steamWorkshopInst = sWorkshop.getField("instance").get(null);
    }

    public String[] getInstalledItemFolders() {
        try {
            return (String[])getInstalledItemFolders.invoke(steamWorkshopInst);
        } catch (Exception e) {
            DebugError(e);
        }
        return null;
    }

    public Path getWorkshopFolder() {
        try {
            return Path.of((String)getWorkshopFolder.invoke(steamWorkshopInst));
        } catch (Exception e) {
            DebugError(e);
        }
        return null;
    }

    @Override
    public void close() {
        try {
            steamUtilsShutdown.invoke(null);
        } catch (Exception e) {
            DebugError(e);
        }
    }
}
