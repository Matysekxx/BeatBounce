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

    public static <T> void register(Class<T> type, T component) {
        if (components.put(type, component) != null) {
            System.err.println("Component has been overwritten: " + type.getName());
        }
    }

    @SuppressWarnings("unchecked") public static <T> T getComponent(Class<T> component) {
        return (T) components.get(component);
    }
}
