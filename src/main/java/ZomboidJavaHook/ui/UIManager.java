package ZomboidJavaHook.ui;

import ZomboidJavaHook.config.HookConfig;
import ZomboidJavaHook.config.ModData;
import ZomboidJavaHook.config.TrustedDigests;
import ZomboidJavaHook.dev.GameCodePubliciser;
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
import java.util.Set;
import java.util.function.Consumer;

public class UIManager extends Application {
    private static HookConfig cfg;
    private static final String skipCheck = ".skipJavaModsPrompt";
    private static final URL modsDialog = UIManager.class.getResource("ModsDialog.fxml");
    private static final URL gameLocatorDialog = UIManager.class.getResource("GameLocatorDialog.fxml");

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        var listArgs = Arrays.asList(args);
        if (listArgs.contains("--help") || listArgs.contains("-h")) {
            System.out.println(
"""
Project Zomboid Java Hook:
--i-trust-all-mods : loads any Java it finds and runs completely silently.
                     Intended for servers and the foolhardy. You CANNOT use
                     this option while also using the --mods option; --mods
                     will take precedence.
--mods mod1;mod2   : Specifies a list of mod names to automatically trust. This
                     offers no real benefit over the previous option unless you
                     are worried about someone slipping some java into other
                     mods on the server - which you are still not safe from if
                     the attacker knows any mods named in this argument.
                     You MUST encase the mod list in quotes in your shell script
                     if any of them have a space in their name.
--make-public      : Modifies all classes, interfaces and enums in the game
                     to have public, non-final visibility, dumps them into
                     the file zombie.jar and terminates - This is intended for
                     use in your own project, if you need it. The hook modifies
                     all classes at runtime to be public so it will match what
                     you code against.
--server           : Instead of executing the usual main function for the
                     client version of the game, it will instead execute
                     zombie.network.gameServer/main.
--main my.Main     : Allows you to specify any java class to execute as the
                     real main function after code hooking has completed.
                     Useful if you intend to call your own main before the
                     game's and saves you from having to hook zomboid's main.
                     This argument supersedes --server.
--help             : prints this help message and terminates""");
            return;
        }
        if (listArgs.contains("--make-public")) {
            try (var gcp = new GameCodePubliciser()) {
                gcp.dumpCode();
            }
            return;
        }
        try {
            cfg = new HookConfig(listArgs.contains("--main") ? listArgs.get(listArgs.indexOf("--main") + 1)
                                                             : listArgs.contains("--server") ? HookConfig.serverMain
                                                                                             : HookConfig.clientMain);
        } catch (IOException e) {
            // not running from game dir, no problem.
        }
        if (cfg != null) {
            var modArg = listArgs.indexOf("--mods");
            if (modArg++ != -1) {
                var toEnable = Set.of(listArgs.get(modArg).split(";"));
                cfg.getMods().stream()
                        .filter(mod -> toEnable.contains(mod.getModName()))
                        .forEach(mod -> mod.setEnabled(true));
            } else if (listArgs.contains("--i-trust-all-mods") ||
                    (Files.exists(TrustedDigests.trustDir.resolve(skipCheck)) &&
                            TrustedDigests.transaction(td -> cfg.getMods().stream()
                                    .map(ModData::getJarDir)
                                    .allMatch(td::isTrusted))
                    )) {
                cfg.getMods().forEach(mod -> mod.setEnabled(true));
            } else
                launch();
        } else
            launch();
        if (cfg != null && !cfg.isUserCancelled())
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
