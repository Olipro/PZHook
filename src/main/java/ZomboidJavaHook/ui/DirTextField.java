package ZomboidJavaHook.ui;

import javafx.scene.control.TextField;

import java.io.File;

public class DirTextField extends TextField {
    private File path;

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
        if (path != null)
            setText(path.getAbsolutePath());
    }
}
