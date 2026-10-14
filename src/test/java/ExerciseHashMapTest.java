import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
        int[] keys = {1, 101, 201};
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
        int first = 1;
        int middle = 101;
        int last = 201;
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
    void collisionsFromDifferentNaturalIndicesRemainSearchable() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(1, 11);
        map.put(2, 22);
        map.put(101, 1_011);

        assertEquals(OptionalInt.of(11), map.get(1));
        assertEquals(OptionalInt.of(22), map.get(2));
        assertEquals(OptionalInt.of(1_011), map.get(101));
        assertEquals(3, map.size());
    }

    @Test
    void deletingAnEarlierEntryDoesNotShiftAnotherKeyBeforeItsNaturalIndex() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(1, 11);   // natural index 1
        map.put(2, 22);   // natural index 2
        map.put(101, 1_011); // collides at index 1

        assertTrue(map.remove(1));

        assertEquals(OptionalInt.of(22), map.get(2));
        assertEquals(OptionalInt.of(1_011), map.get(101));
        assertEquals(2, map.size());
    }

    @Test
    void insertionAfterDeletionKeepsTheRemainingAndNewEntriesAccessible() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(1, 10);
        map.put(101, 20);
        map.put(201, 30);

        assertTrue(map.remove(101));
        map.put(301, 40);

        assertEquals(OptionalInt.of(10), map.get(1));
        assertEquals(OptionalInt.empty(), map.get(101));
        assertEquals(OptionalInt.of(30), map.get(201));
        assertEquals(OptionalInt.of(40), map.get(301));
        assertEquals(3, map.size());
    }

    @Test
    void repeatedDeletionAndReinsertionKeepsCollisionChainEntriesCorrect() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int[] keys = {1, 101, 201, 301};
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

            int replacement = (round + 10) * 100 + 1;
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
    void crossingTheInitialLoadThresholdPreservesAllEntries() {
        ExerciseHashMap map = new LinearProbingHashMap();

        for (int key = 0; key < 50; key++) {
            map.put(key, key + 100);
        }

        assertEquals(50, map.size());
        for (int key = 0; key < 50; key++) {
            assertEquals(OptionalInt.of(key + 100), map.get(key));
        }

        map.put(50, 150);

        assertEquals(51, map.size());
        for (int key = 0; key <= 50; key++) {
            assertEquals(OptionalInt.of(key + 100), map.get(key));
        }
    }

    @Test
    void collidingEntriesRemainAvailableAfterGrowth() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 2_000;
        int[] keys = new int[entryCount];

        for (int index = 0; index < entryCount; index++) {
            keys[index] = index * 100;
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
        int[] keys = {99, 199, 299};

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            for (int index = 0; index < keys.length; index++) {
                map.put(keys[index], index + 1);
            }

            for (int index = 0; index < keys.length; index++) {
                assertEquals(OptionalInt.of(index + 1), map.get(keys[index]));
            }
        });
    }

    @Test
    void removingANonexistentKeyAtTheArrayBoundaryReturnsFalseWithoutChangingTheMap() {
        ExerciseHashMap map = new LinearProbingHashMap();
        map.put(99, 9_900);

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> assertFalse(map.remove(199)));

        assertEquals(1, map.size());
        assertEquals(OptionalInt.of(9_900), map.get(99));
    }

    @Test
    void unsuccessfulSearchesTerminateAfterHeavyCollisionsAndDeletions() {
        ExerciseHashMap map = new LinearProbingHashMap();
        int entryCount = 2_000;
        int[] keys = new int[entryCount];
        for (int index = 0; index < entryCount; index++) {
            keys[index] = index * 100;
            map.put(keys[index], index);
        }
        for (int index = 0; index < entryCount; index += 2) {
            assertTrue(map.remove(keys[index]));
        }

        int absent = entryCount * 100;
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertEquals(OptionalInt.empty(), map.get(absent));
            assertFalse(map.containsKey(absent));
            assertFalse(map.remove(absent));
        });
    }

    @Test
    void repeatedInsertUpdateAndDeleteOperationsMatchAReferenceMap() {
        ExerciseHashMap actual = new LinearProbingHashMap();
        Map<Integer, Integer> expected = new HashMap<>();
        int[] keys = {1, 2, 101, 201, 301};

        for (int step = 0; step < 60; step++) {
            int key = keys[(step * 3) % keys.length];
            if (step % 4 == 3) {
                boolean expectedRemoved = expected.containsKey(key);
                assertEquals(expectedRemoved, actual.remove(key));
                expected.remove(key);
            } else {
                int value = step % 2 == 0 ? -step : step;
                actual.put(key, value);
                expected.put(key, value);
            }

            assertMatchesReference(actual, expected, keys);
        }
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

    private static void assertMatchesReference(
            ExerciseHashMap actual,
            Map<Integer, Integer> expected,
            int[] keys) {
        assertEquals(expected.size(), actual.size());
        for (int key : keys) {
            assertEquals(expected.containsKey(key), actual.containsKey(key));
            Integer expectedValue = expected.get(key);
            OptionalInt expectedResult = expectedValue == null
                    ? OptionalInt.empty()
                    : OptionalInt.of(expectedValue);
            assertEquals(expectedResult, actual.get(key));
        }
    }
}
