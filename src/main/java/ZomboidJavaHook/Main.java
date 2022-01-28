package ZomboidJavaHook;

import ZomboidJavaHook.ui.UIManager;
import net.uptheinter.interceptify.EntryPoint;

import java.lang.instrument.Instrumentation;

public class Main {
    public static void main(String[] args) throws Exception {
        UIManager.main(args);
    }

    public static void premain(String args, Instrumentation instr) {
        EntryPoint.premain(args, instr);
    }
}
