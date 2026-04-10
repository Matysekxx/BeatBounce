package cz.matysekxx.beatbounce;

import java.util.HashMap;
import java.util.Map;

public final class DIContainer {
    private final static Map<Class<?>, Object> components = new HashMap<>();

    private DIContainer() {}

    public static void register(Object component) {
        if (components.put(component.getClass(), component) != null) {
            System.err.println("Component has been overwritten: " + component.getClass().getName());
        }
    }

    public static <T> T getComponent(Class<T> component) {
        @SuppressWarnings("unchecked")
        final T c = (T) components.get(component);
        return c;
    }
}
