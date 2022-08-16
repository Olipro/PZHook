package ZomboidJavaHook;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class PublicClassRegistry {
    private static final Set<Predicate<String>> callbacks = new HashSet<>();

    public static void add(Predicate<String> predicate) {
        callbacks.add(predicate);
    }

    public static void remove(Predicate<String> predicate) {
        callbacks.remove(predicate);
    }

    public static boolean anyMatch(String input) {
        return callbacks.stream().anyMatch(pred -> pred.test(input));
    }
}
