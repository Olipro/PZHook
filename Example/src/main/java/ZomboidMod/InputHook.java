package ZomboidMod;

import net.uptheinter.interceptify.annotations.InterceptClass;
import net.uptheinter.interceptify.annotations.OverwriteConstructor;
import net.uptheinter.interceptify.annotations.OverwriteMethod;
import zombie.core.input.Input;
import zombie.input.ControllerState;

import java.lang.reflect.Method;

@InterceptClass("zombie.core.input.Input")
public class InputHook extends Input {

    @OverwriteConstructor
    public static void construct(Input input) throws Throwable {
        System.out.println("AFTER constructor");
    }

    @OverwriteMethod("checkConnectDisconnect")
    public static boolean checkConnectDisconnect(Input input, Method parent, ControllerState controllerState) {
        System.out.println("Controller count: " + input.getControllerCount());
        System.out.println("Intercepted check!!!");
        try {
            return (boolean) parent.invoke(input, controllerState);
        } catch (Throwable e) { return false; }
    }
}
