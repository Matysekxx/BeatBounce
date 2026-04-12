package cz.matysekxx.beatbounce;

import java.util.HashMap;
import java.util.Map;

@Deprecated
//Currently not in use due safety reasons
public final class DIContainer {
    private final static Map<Class<?>, Object> components = new HashMap<>();

    private DIContainer() {}

    public static void init() {
        //register(LevelGenerator.class, new LevelGenerator());
        //register(ScreenManager.class, new ScreenManager());
    }

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

    public static <T> T getComponent(Class<T> type) {
        System.out.println(components);
        if (!components.containsKey(type)) {
            System.err.println("Component not found: " + type.getName());
            return null;
        }
        @SuppressWarnings("unchecked")
        final T c = (T) components.get(type);
        if (c == null) {
            System.err.println("Component is null: " + type.getName());
        }
        return c;
    }
}
