import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** Behavioral tests for the int-to-int ExerciseHashMap contract. */
class ExerciseHashMapTest {

    @Test
    void insertsAndUpdatesAKeyWithoutIncreasingSize() {
        ExerciseHashMap map = new LinearProbingHashMap();

        map.put(42, 7);
        assertEquals(OptionalInt.of(7), map.get(42));
        assertTrue(map.containsKey(42));
        assertEquals(1, map.size());

        map.put(42, -9);
        assertEquals(OptionalInt.of(-9), map.get(42));
        assertEquals(1, map.size());
    }

    @Test
    void unsuccessfulLookupAndRemovalLeaveTheMapUnchanged() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(5, 70);

        assertEquals(OptionalInt.empty(), map.get(999));
        assertFalse(map.containsKey(999));
        assertFalse(map.remove(999));
        assertEquals(1, map.size());
        assertEquals(OptionalInt.of(70), map.get(5));
    }

    @Test
    void removingAnExistingKeyReturnsTrueAndDecreasesSize() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(42, 7);

        assertTrue(map.remove(42));
        assertEquals(OptionalInt.empty(), map.get(42));
        assertFalse(map.containsKey(42));
        assertEquals(0, map.size());
    }

    @Test
    void resolvesCollisionsForKeysWithTheSameNaturalIndex() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int[] keys = {1, 101, 201};
        int[] values = {10, 20, 30};

        for (int index = 0; index < keys.length; index++) {
            map.put(keys[index], values[index]);
        }

        assertEquals(3, map.size());
        for (int index = 0; index < keys.length; index++) {
            assertEquals(OptionalInt.of(values[index]), map.get(keys[index]));
        }
    }

    @Test
    void deletingFromTheMiddleOfACollisionChainPreservesFollowingEntries() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(1, 10);
        map.put(101, 20);
        map.put(201, 30);

        assertTrue(map.remove(101));

        assertEquals(OptionalInt.of(10), map.get(1));
        assertEquals(OptionalInt.empty(), map.get(101));
        assertEquals(OptionalInt.of(30), map.get(201));
        assertEquals(2, map.size());
    }

    @Test
    void deletionDoesNotMoveAKeyBeforeItsNaturalIndex() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(1, 11);   // natural index 1
        map.put(2, 22);   // natural index 2
        map.put(101, 1_011); // collides at index 1

        assertTrue(map.remove(1));

        assertEquals(OptionalInt.empty(), map.get(1));
        assertEquals(OptionalInt.of(22), map.get(2));
        assertEquals(OptionalInt.of(1_011), map.get(101));
        assertEquals(2, map.size());
    }

    @Test
    void wrappedChainDeletionsAndReinsertionsPreserveAllEntriesAndValues() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 45;
        int[] candidateKeys = new int[entryCount + 3];
        Map<Integer, Integer> expected = new HashMap<>();

        for (int index = 0; index < entryCount; index++) {
            int key = 99 + index * 100;
            int value = 30_000 + index;
            candidateKeys[index] = key;
            map.put(key, value);
            expected.put(key, value);
        }

        // This chain runs through slot 99 and wraps to slot 0. The first
        // deletion is near its end, with another occupied entry following it.
        int[] removedIndices = {43, 20, 7};
        for (int round = 0; round < removedIndices.length; round++) {
            int removedKey = candidateKeys[removedIndices[round]];
            assertTrue(map.remove(removedKey));
            expected.remove(removedKey);

            int replacementKey = 99 + (entryCount + round) * 100;
            int replacementValue = -40_000 - round;
            candidateKeys[entryCount + round] = replacementKey;
            map.put(replacementKey, replacementValue);
            expected.put(replacementKey, replacementValue);

            assertMatchesReference(map, expected, candidateKeys);
        }
    }

    @Test
    void growsAndRehashesCollidingEntriesThenMaintainsSizeAfterRemoval() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 2_000;
        int[] keys = new int[entryCount];

        for (int index = 0; index < entryCount; index++) {
            keys[index] = index * 100;
            map.put(keys[index], index - 1_000);
        }

        assertEquals(entryCount, map.size());
        for (int index = 0; index < entryCount; index++) {
            assertEquals(OptionalInt.of(index - 1_000), map.get(keys[index]));
        }

        for (int index = 0; index < entryCount; index += 2) {
            assertTrue(map.remove(keys[index]));
        }
        assertEquals(entryCount / 2, map.size());
        for (int index = 0; index < entryCount; index++) {
            if (index % 2 == 0) {
                assertEquals(OptionalInt.empty(), map.get(keys[index]));
            } else {
                assertEquals(OptionalInt.of(index - 1_000), map.get(keys[index]));
            }
        }
        assertFalse(map.remove(entryCount * 100));
        assertEquals(entryCount / 2, map.size());
    }

    @Test
    void handlesNegativeKeysAndIntegerBoundaryKeysAndValues() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int[] keys = {-37, Integer.MIN_VALUE, Integer.MAX_VALUE, 0};
        int[] values = {Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE};

        for (int index = 0; index < keys.length; index++) {
            map.put(keys[index], values[index]);
        }

        assertEquals(keys.length, map.size());
        for (int index = 0; index < keys.length; index++) {
            assertEquals(OptionalInt.of(values[index]), map.get(keys[index]));
            assertTrue(map.containsKey(keys[index]));
        }
        assertEquals(OptionalInt.empty(), map.get(99));
    }

    @Test
    void wrapsAtTheArrayEndAndTerminatesAtTheLongestReachableInitialCluster() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 50;
        int[] keys = new int[entryCount];

        // At capacity 100 these keys occupy slots 99, 0, ..., 48. The final
        // key takes 50 probes. A full-table probe is unreachable via the API:
        // inserting the 51st distinct key grows the table at load factor 0.5.
        for (int index = 0; index < entryCount; index++) {
            keys[index] = 99 + index * 100;
            map.put(keys[index], index * 17 - 200);
        }

        int missingKey = 99 + entryCount * 100;
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            assertEquals(OptionalInt.of((entryCount - 1) * 17 - 200), map.get(keys[entryCount - 1]));
            assertTrue(map.containsKey(keys[entryCount - 1]));
            assertEquals(OptionalInt.empty(), map.get(missingKey));
            assertFalse(map.containsKey(missingKey));
            assertFalse(map.remove(missingKey));
        });
        assertEquals(entryCount, map.size());
    }

    @Test
    void deterministicOperationSequenceMatchesJavaHashMap() {
        ExerciseHashMap actual = new LinearProbingHashMap();
        Map<Integer, Integer> expected = new HashMap<>();
        int[] keys = {1, 2, 99, 101, 199, 201, -1, Integer.MIN_VALUE, Integer.MAX_VALUE};
        Random random = new Random(0x5EED);

        for (int step = 0; step < 300; step++) {
            int key = keys[random.nextInt(keys.length)];
            if (random.nextInt(4) == 0) {
                assertEquals(expected.containsKey(key), actual.remove(key));
                expected.remove(key);
            } else {
                int value = random.nextInt();
                actual.put(key, value);
                expected.put(key, value);
            }

            assertMatchesReference(actual, expected, keys);
        }
    }

    private static void assertMatchesReference(
            ExerciseHashMap actual,
            Map<Integer, Integer> expected,
            int[] candidateKeys) {
        assertEquals(expected.size(), actual.size());
        for (int key : candidateKeys) {
            assertEquals(expected.containsKey(key), actual.containsKey(key));
            Integer value = expected.get(key);
            OptionalInt expectedValue = value == null ? OptionalInt.empty() : OptionalInt.of(value);
            assertEquals(expectedValue, actual.get(key));
        }
    }
}
