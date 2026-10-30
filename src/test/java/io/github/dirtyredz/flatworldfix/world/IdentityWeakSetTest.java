package io.github.dirtyredz.flatworldfix.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityWeakSetTest {
    @Test
    void equalButDistinctValuesDoNotShareMembership() {
        var selected = new String("same");
        var otherWorld = new String("same");
        var set = new IdentityWeakSet<String>();

        set.add(selected);

        assertTrue(set.contains(selected));
        assertFalse(set.contains(otherWorld));
    }

    @Test
    void setCanClearOneIdentityWithoutClearingAnEqualValue() {
        var first = new String("same");
        var second = new String("same");
        var set = new IdentityWeakSet<String>();
        set.add(first);
        set.add(second);

        set.set(first, false);

        assertFalse(set.contains(first));
        assertTrue(set.contains(second));
    }
}
