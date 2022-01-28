package ZomboidJavaHook.ui;

import ZomboidJavaHook.config.ModData;
import ZomboidJavaHook.config.TrustedDigests;
import javafx.scene.control.CheckBox;

public class ModCheckBox extends CheckBox {
    private final ModData mod;

    public ModCheckBox(ModData mod, TrustedDigests trustData) {
        super(mod.getModName());
        setFocusTraversable(true);
        var enabled = trustData.isTrusted(mod.getJarDir());
        setSelected(enabled);
        mod.setEnabled(enabled);
        this.mod = mod;
    }

    @Override
    public void fire() {
        super.fire();
        mod.setEnabled(isSelected());
    }
}
