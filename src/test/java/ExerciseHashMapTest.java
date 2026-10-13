import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

/** Behavioral tests for the int-to-int ExerciseHashMap contract. */
class ExerciseHashMapTest {

    @Test
    void insertsOneKeyAndRetrievesItsValue() {
        ExerciseHashMap map = new LinearProbingHashMap();

        map.put(42, 7);

        assertEquals(OptionalInt.of(7), map.get(42));
        assertTrue(map.containsKey(42));
        assertEquals(1, map.size());
    }

    @Test
    void returnsEmptyForANonexistentKey() {
        ExerciseHashMap map = new LinearProbingHashMap();

        assertEquals(OptionalInt.empty(), map.get(999));
        assertFalse(map.containsKey(999));
        assertEquals(0, map.size());
    }

    @Test
    void updatingAnExistingKeyReplacesItsValueWithoutChangingSize() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(42, 41);

        map.put(42, 7);

        assertEquals(OptionalInt.of(7), map.get(42));
        assertEquals(1, map.size());
    }

    @Test
    void removesAnExistingKey() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(42, 7);

        assertTrue(map.remove(42));

        assertEquals(OptionalInt.empty(), map.get(42));
        assertFalse(map.containsKey(42));
        assertEquals(0, map.size());
    }

    @Test
    void removingANonexistentKeyReturnsFalseAndLeavesMapUnchanged() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(5, 70);

        assertFalse(map.remove(999));

        assertEquals(1, map.size());
        assertEquals(OptionalInt.of(70), map.get(5));
    }

    @Test
    void retrievesKeysThatCollideInTheInitialTable() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int[] keys = {0, 10, 20};
        int[] values = {10, 20, 30};

        for (int index = 0; index < keys.length; index++) {
            map.put(keys[index], values[index]);
        }

        for (int index = 0; index < keys.length; index++) {
            assertEquals(OptionalInt.of(values[index]), map.get(keys[index]));
        }
        assertEquals(keys.length, map.size());
    }

    @Test
    void deletingFromTheMiddleOfACollisionChainKeepsOtherEntriesReachable() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int first = 0;
        int middle = 10;
        int last = 20;
        map.put(first, 10);
        map.put(middle, 20);
        map.put(last, 30);

        assertTrue(map.remove(middle));

        assertEquals(OptionalInt.of(10), map.get(first));
        assertEquals(OptionalInt.empty(), map.get(middle));
        assertEquals(OptionalInt.of(30), map.get(last));
        assertEquals(2, map.size());
    }

    @Test
    void repeatedDeletionAndReinsertionKeepsCollisionChainEntriesCorrect() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int[] keys = {0, 10, 20, 30};
        int[] values = {10, 20, 30, 40};
        for (int index = 0; index < keys.length; index++) {
            map.put(keys[index], values[index]);
        }

        for (int round = 0; round < 8; round++) {
            int replaceIndex = (round + 1) % keys.length;
            int removed = keys[replaceIndex];
            assertTrue(map.remove(removed));
            assertFalse(map.containsKey(removed));
            assertEquals(OptionalInt.empty(), map.get(removed));

            int replacement = (round + 10) * 10;
            int replacementValue = 100 + round;
            map.put(replacement, replacementValue);
            keys[replaceIndex] = replacement;
            values[replaceIndex] = replacementValue;

            assertEquals(keys.length, map.size());
            for (int index = 0; index < keys.length; index++) {
                assertTrue(map.containsKey(keys[index]));
                assertEquals(OptionalInt.of(values[index]), map.get(keys[index]));
            }
        }
    }

    @Test
    void entriesRemainAvailableAfterInsertingManyKeys() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 10_000;

        for (int key = 0; key < entryCount; key++) {
            map.put(key, key * 3);
        }

        assertEquals(entryCount, map.size());
        for (int key = 0; key < entryCount; key++) {
            assertEquals(OptionalInt.of(key * 3), map.get(key));
        }
    }

    @Test
    void collidingEntriesRemainAvailableAfterGrowth() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 2_000;
        int[] keys = new int[entryCount];

        for (int index = 0; index < entryCount; index++) {
            keys[index] = index * 10;
            map.put(keys[index], index - 1_000);
        }

        assertEquals(entryCount, map.size());
        for (int index = 0; index < entryCount; index++) {
            assertTrue(map.containsKey(keys[index]));
            assertEquals(OptionalInt.of(index - 1_000), map.get(keys[index]));
        }
    }

    @Test
    void handlesNegativeKeysIncludingIntegerMinimumValue() {
        ExerciseHashMap map = new LinearProbingHashMap();

        map.put(-37, Integer.MIN_VALUE);
        map.put(Integer.MIN_VALUE, -1);

        assertEquals(OptionalInt.of(Integer.MIN_VALUE), map.get(-37));
        assertEquals(OptionalInt.of(-1), map.get(Integer.MIN_VALUE));
        assertTrue(map.containsKey(Integer.MIN_VALUE));
    }

    @Test
    void probesAcrossTheArrayBoundaryForKeysHashingToTheLastBucket() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int[] keys = {-1, 9, 19};

        for (int index = 0; index < keys.length; index++) {
            map.put(keys[index], index + 1);
        }

        for (int index = 0; index < keys.length; index++) {
            assertEquals(OptionalInt.of(index + 1), map.get(keys[index]));
        }
    }

    @Test
    void unsuccessfulSearchesTerminateAfterHeavyCollisionsAndDeletions() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 2_000;
        int[] keys = new int[entryCount];
        for (int index = 0; index < entryCount; index++) {
            keys[index] = index * 10;
            map.put(keys[index], index);
        }
        for (int index = 0; index < entryCount; index += 2) {
            assertTrue(map.remove(keys[index]));
        }

        int absent = entryCount * 10;
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertEquals(OptionalInt.empty(), map.get(absent));
            assertFalse(map.containsKey(absent));
            assertFalse(map.remove(absent));
        });
    }

    @Test
    void optionalResultDistinguishesAbsenceFromEveryIntValue() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int[] values = {Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE};

        for (int index = 0; index < values.length; index++) {
            map.put(index, values[index]);
        }

        for (int index = 0; index < values.length; index++) {
            assertEquals(OptionalInt.of(values[index]), map.get(index));
        }
        assertEquals(OptionalInt.empty(), map.get(99));
    }
}
