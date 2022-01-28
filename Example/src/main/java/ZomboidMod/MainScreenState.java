package ZomboidMod;

import net.uptheinter.interceptify.annotations.InterceptClass;
import net.uptheinter.interceptify.annotations.OverwriteMethod;

import java.lang.reflect.Method;

@InterceptClass("zombie.gameStates.MainScreenState")
public class MainScreenState {

    @OverwriteMethod("main")
    public static void main(Method parent, String[] args) {
        System.out.println("Intercepted main!");
        try {
            parent.invoke(null, (Object) args);
        } catch (Exception e){}
    }
}
