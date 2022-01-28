package ZomboidJavaHook.ui;

import ZomboidJavaHook.config.HookConfig;
import ZomboidJavaHook.config.ModData;
import ZomboidJavaHook.config.TrustedDigests;
import ZomboidJavaHook.installer.HookInstaller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.uptheinter.interceptify.EntryPoint;
import net.uptheinter.interceptify.util.Util;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

public class UIManager extends Application {
    private static HookConfig cfg;
    private static final String skipCheck = ".skipJavaModsPrompt";
    private static final URL modsDialog = UIManager.class.getResource("ModsDialog.fxml");
    private static final URL gameLocatorDialog = UIManager.class.getResource("GameLocatorDialog.fxml");

    static {
        try {
            cfg = new HookConfig();
        } catch (ClassNotFoundException e) {
            // not running from game dir, no problem.
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        var listArgs = Arrays.asList(args);
        if (listArgs.contains("--help")) {
            System.out.println(
"""
Project Zomboid Java Hook:
--i-trust-all-mods : loads any Java it finds and runs completely silently.
                     Intended for servers and the foolhardy.
--help             : prints this help message and terminates""");
            return;
        }
        if (cfg != null && (listArgs.contains("--i-trust-all-mods") ||
                (Files.exists(TrustedDigests.trustDir.resolve(skipCheck)) &&
                        TrustedDigests.transaction(td -> cfg.getMods().stream()
                                .map(ModData::getJarDir)
                                .allMatch(td::isTrusted))
                )
        )) {
            cfg.getMods().forEach(mod -> mod.setEnabled(true));
        } else
            launch();
        if (!cfg.isUserCancelled())
            EntryPoint.entryPoint(cfg, args);
    }

    private static final String[] searchDirs = new String[]{
            "C:/Program Files (x86)/Steam/steamapps/common/ProjectZomboid/",
            "D:/Program Files (x86)/Steam/steamapps/common/ProjectZomboid/",
            "C:/Program Files/Steam/steamapps/common/ProjectZomboid/",
            "D:/Program Files/Steam/steamapps/common/ProjectZomboid/",
            System.getProperty("user.home") + "/.local/share/steam/steamapps/common/ProjectZomboid/projectzomboid",
            System.getProperty("user.home") + "/Library/Application Support/Steam/steamapps/common/Project Zomboid.app/Contents/Java",
    };

    private boolean initedGamePath() {
        if(Files.isDirectory(Paths.get("", "zombie")))
            return false;
        var path = Arrays.stream(searchDirs)
                .map(Paths::get)
                .filter(Files::isDirectory)
                .findAny().orElse(null);
        path = showGameLocatorDialog(path);
        installHook(path);
        return true;
    }

    private void installHook(Path path) {
        if (path == null)
            return;
        new HookInstaller(path).install();
    }

    private <T> T showStage(URL dialog, Consumer<T> optionalFunc) {
        try {
            var root = new FXMLLoader(dialog);
            var scene = new Scene(root.load());
            scene.setFill(Color.TRANSPARENT);
            var subStage = new Stage(StageStyle.TRANSPARENT);
            //noinspection ConstantConditions
            subStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
            subStage.setScene(scene);
            var controller = root.<T>getController();
            if (optionalFunc != null)
                optionalFunc.accept(controller);
            subStage.showAndWait();
            return controller;
        } catch (IOException e) {
            Util.DebugError(e);
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    private <T> T showStage(URL dialog) {
        return showStage(dialog, null);
    }

    private Path showGameLocatorDialog(Path path) {
        return this.<GameLocatorDialogController>showStage(gameLocatorDialog,
                gld -> gld.setInitialPath(path)).getGamePath();
    }

    private void showModsDialog() {
        try {
            TrustedDigests.transaction(trustData -> {
                this.<ModsDialogController>showStage(modsDialog, controller -> controller.setConfig(cfg, trustData));
                return 0;
            });
            if (cfg.getRememberNextTime())
                Files.createFile(TrustedDigests.trustDir.resolve(skipCheck));
        } catch (Exception e) {
            Util.DebugError(e);
        }
    }

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.TRANSPARENT);
        if (!initedGamePath())
            showModsDialog();
        else
            cfg.setUserCancelled(); // don't run the game, we're installing.
        Platform.exit();
    }
}
