package cz.matysekxx.beatbounce.util;

import java.util.Objects;
import java.util.function.Supplier;

/// A thread-safe container for a value that is computed lazily.
///
/// This implementation ensures that the [Supplier] is invoked at most once,
/// even when accessed from multiple threads simultaneously, using the
/// double-checked locking pattern.
///
/// ### Example Usage:
///
/// ```java
/// // Define a heavy resource that won't be created immediately
/// Lazy<String> lazyDatabaseUrl = Lazy.of(() -> {
///     System.out.println("Fetching URL from config...");
///     return "jdbc:mysql://localhost:3306/db";
/// });
///
/// // Later in the code...
/// if (!lazyDatabaseUrl.wasInitialized()) {
///     System.out.println("URL is still not loaded.");
/// }
///
/// // The first call to get() triggers the supplier
/// String url = lazyDatabaseUrl.get();
///
/// // Subsequent calls return the cached value instantly
/// String cachedUrl = lazyDatabaseUrl.get();
/// ```
///
/// @param <T> The type of the value.
public class Lazy<T> {
    private final Supplier<T> supplier;
    private volatile T value;

    /// Internal constructor. Use [of] for instance creation.
    ///
    /// @param supplier The function providing the value
    public Lazy(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    /// Creates a new lazy instance.
    ///
    /// @param <T>      The type of the value.
    /// @param supplier The provider function.
    /// @return A new [Lazy] container.
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /// Returns the value, computing it if necessary.
    ///
    /// This method uses double-checked locking to ensure thread safety
    /// while maintaining high performance for subsequent reads.
    ///
    /// @return The initialized value.
    public T get() {
        T result = value;
        if (result == null) {
            synchronized (this) {
                result = value;
                if (result == null) {
                    value = result = supplier.get();
                }
            }
        }
        return result;
    }

    /// Returns `true` if the value has already been initialized.
    ///
    /// @return The initialization status.
    public boolean wasInitialized() {
        return value != null;
    }

    /// Force initialization of the value without needing to handle the return type.
    ///
    /// Useful for pre-warming caches or ensuring a value is ready before it is needed.
    public void initialize() {
        get();
    }
}