package ZomboidJavaHook.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.nio.file.Files;
import java.nio.file.Path;

public class GameLocatorDialogController extends DialogControllerBase {

    @FXML
    private DirTextField gamePath;

    @Override
    protected Window getWindow() {
        return gamePath.getScene().getWindow();
    }

    @FXML
    void onBrowse(ActionEvent event) {
        var choose = new DirectoryChooser();
        choose.setTitle("Select Project Zomboid Path");
        var path = choose.showDialog(gamePath.getScene().getWindow());
        if (path != null)
            gamePath.setPath(path);
    }

    @FXML
    void onCancel(ActionEvent event) {
        gamePath.setPath(null);
        gamePath.getScene().getWindow().hide();
    }

    @FXML
    void onOK(ActionEvent event) {
        var path = gamePath.getPath();
        var linux = path.toPath().resolve("projectzomboid").resolve("zombie");
        var mac = path.toPath().resolve("Contents").resolve("Java");
        if (Files.isDirectory(linux))
            path = linux.getParent().toFile();
        else if (Files.isDirectory(mac))
            path = mac.toFile();
        if (!Files.isDirectory(path.toPath().resolve("zombie"))) {
            var alert = new Alert(Alert.AlertType.ERROR,
                    "The folder you selected doesn't appear to be the " +
                    "correct game directory. Try again, or, if you are sure " +
                    "that it's correct, this mod might need updating", ButtonType.OK);
            alert.initStyle(StageStyle.UNIFIED);
            alert.show();
        } else {
            var alert = new Alert(Alert.AlertType.INFORMATION,
                    "The mod will now be installed to the game directory." +
                              "you can close this window and launch the game as usual!");
            alert.initStyle(StageStyle.UNIFIED);
            alert.showAndWait();
            gamePath.getScene().getWindow().hide();
        }
    }

    public Path getGamePath() {
        return gamePath.getPath() == null ? null : gamePath.getPath().toPath();
    }

    public void setInitialPath(Path path) {
        gamePath.setPath(path.toFile());
    }
}
