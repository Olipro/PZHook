package ZomboidJavaHook.ui;

import ZomboidJavaHook.config.HookConfig;
import ZomboidJavaHook.config.ModData;
import ZomboidJavaHook.config.TrustedDigests;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class ModsDialogController extends DialogControllerBase {
    private HookConfig cfg;
    private TrustedDigests trustData;
    private int uncheckedCount;

    @FXML
    private VBox detectedMods;
    @FXML
    private CheckBox rememberChoices;
    @FXML
    private Tooltip toolTip;

    @FXML
    public void initialize() {
        detectedMods.setSpacing(3);
    }

    @Override
    protected Window getWindow() {
        return detectedMods.getScene().getWindow();
    }

    public void setConfig(HookConfig cfg, TrustedDigests trustData) {
        var items = detectedMods.getChildren();
        cfg.getMods()
            .forEach(mod -> {
                var checkBox = new ModCheckBox(mod, trustData);
                checkBox.setOnAction(this::onCheckedChange);
                checkBox.getStyleClass().add("item-checkbox");
                if (!checkBox.isSelected())
                    ++uncheckedCount;
                items.add(checkBox);
            });
        //for (int i = 0; i < 50; ++i) items.add(new CheckBox("testing"));
        rememberChoices.setDisable(uncheckedCount != 0);
        toolTip.setOpacity(uncheckedCount != 0 ? 1 : 0);
        this.cfg = cfg;
        this.trustData = trustData;
    }

    private void onCheckedChange(ActionEvent actionEvent) {
        uncheckedCount += ((ModCheckBox)actionEvent.getSource()).isSelected() ? -1 : 1;
        var shouldDisable = uncheckedCount != 0;
        if (shouldDisable && rememberChoices.isSelected())
            rememberChoices.fire();
        rememberChoices.setDisable(shouldDisable);
        toolTip.setOpacity(shouldDisable ? 1 : 0);
    }

    @FXML
    private void onOK(@SuppressWarnings("unused") ActionEvent actionEvent) {
        trustData.clear();
        cfg.getMods()
                .stream()
                .filter(ModData::isEnabled)
                .map(ModData::getJarDir)
                .forEach(trustData::addTrusted);
        if (rememberChoices.isSelected())
            cfg.setRememberNextTime();
        detectedMods.getScene().getWindow().hide();
    }

    @FXML
    private void onCancel(@SuppressWarnings("unused") ActionEvent actionEvent) {
        cfg.setUserCancelled();
        detectedMods.getScene().getWindow().hide();
    }
}
