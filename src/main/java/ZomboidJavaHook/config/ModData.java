package ZomboidJavaHook.config;

import java.nio.file.Path;
import java.util.Objects;

public class ModData {
    private final String modName;
    private final Path jarDir;
    private boolean isEnabled = false;

    public ModData(String modName, Path jarDir) {
        if (jarDir.startsWith(Path.of(System.getProperty("user.home")).resolve("Zomboid").resolve("Workshop")))
            modName += " ----- (NOT FROM WORKSHOP)";
        this.modName = modName;
        this.jarDir = jarDir;
    }

    public String getModName() {
        return modName;
    }

    public Path getJarDir() {
        return jarDir;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ModData)
            return modName.equals(((ModData)other).modName);
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modName);
    }
}
