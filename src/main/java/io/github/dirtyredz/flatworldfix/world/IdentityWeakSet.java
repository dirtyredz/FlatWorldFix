package io.github.dirtyredz.flatworldfix.world;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** Small synchronized weak set whose membership uses object identity rather than equals(). */
final class IdentityWeakSet<T> {
    private final List<WeakReference<T>> entries = new ArrayList<>();

    synchronized boolean contains(T value) {
        boolean found = false;
        for (var iterator = entries.iterator(); iterator.hasNext();) {
            T candidate = iterator.next().get();
            if (candidate == null) iterator.remove();
            else if (candidate == value) found = true;
        }
        return found;
    }

    synchronized void add(T value) {
        if (!contains(value)) entries.add(new WeakReference<>(value));
    }

    synchronized void set(T value, boolean present) {
        entries.removeIf(reference -> {
            T candidate = reference.get();
            return candidate == null || candidate == value;
        });
        if (present) entries.add(new WeakReference<>(value));
    }
}
