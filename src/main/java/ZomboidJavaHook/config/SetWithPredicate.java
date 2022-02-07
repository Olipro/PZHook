package ZomboidJavaHook.config;

import java.util.HashSet;
import java.util.function.Predicate;

public class SetWithPredicate<T> extends HashSet<T> {
    private final Predicate<T> func;
    private final Class<T> type;

    public SetWithPredicate(Predicate<T> func, Class<T> type) {
        this.func = func;
        this.type = type;
    }

    @Override
    public boolean contains(Object o) {
        if (o.getClass().isAssignableFrom(type))
            return func.test((T) o) || super.contains(o);
        return super.contains(o);
    }
}
