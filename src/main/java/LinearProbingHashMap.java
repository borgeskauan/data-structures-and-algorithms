import java.util.OptionalInt;

public class LinearProbingHashMap implements ExerciseHashMap {

    CircularArray mainArray = new CircularArray(100);
    int size = 0;

    double targetLoadFactor = 0.5;

    @Override
    public void put(int key, int value) {
        if (insert(key, value)) {
            size++;
        }

        if (size / (double) mainArray.capacity > targetLoadFactor) {
            resize();
        }
    }

    private boolean insert(int key, int value) {
        int index = hashFunction(key);

        mainArray.startReadingAt(index);

        boolean hasInsertedNew = false;

        while (!mainArray.hasWrapped()) {
            var entry = mainArray.nextEntry();

            if (entry == null) {
                hasInsertedNew = true;
                break;
            }

            if (entry.key == key) {
                break;
            }
        }

        var newEntry = new Entry(key, value);
        mainArray.set(newEntry);

        return hasInsertedNew;
    }

    private void resize() {
        var oldArray = mainArray;

        mainArray = new CircularArray(oldArray.capacity * 2);

        oldArray.startReadingAt(0);

        while (!oldArray.hasWrapped()) {
            var entry = oldArray.nextEntry();
            if (entry == null) {
                continue;
            }

            insert(entry.key, entry.value);
        }
    }

    @Override
    public OptionalInt get(int key) {
        int index = hashFunction(key);

        mainArray.startReadingAt(index);

        while (!mainArray.hasWrapped()) {
            var entry = mainArray.nextEntry();
            if (entry == null) {
                return OptionalInt.empty();
            }

            if (entry.key == key) {
                return OptionalInt.of(entry.value);
            }
        }

        return OptionalInt.empty();
    }

    @Override
    public boolean containsKey(int key) {
        return get(key).isPresent();
    }

    @Override
    public boolean remove(int key) {
        int index = hashFunction(key);

        mainArray.startReadingAt(index);

        while (!mainArray.hasWrapped()) {
            var entry = mainArray.nextEntry();
            if (entry == null) {
                return false;
            }

            if (entry.key == key) {
                break;
            }
        }

        mainArray.set(null);
        size--;

        int deletedAtIndex = mainArray.getLastReadCursor();

        int countEntriesToDelete = 0;

        while (!mainArray.hasWrapped()) {
            var entry = mainArray.nextEntry();
            if (entry == null) {
                break;
            }

            countEntriesToDelete++;
        }

        var tempArray = new Entry[countEntriesToDelete];
        int tempArrayIndex = 0;

        mainArray.startReadingAt(deletedAtIndex + 1);

        while (!mainArray.hasWrapped()) {
            var entry = mainArray.nextEntry();
            if (entry == null) {
                break;
            }

            tempArray[tempArrayIndex] = entry;
            mainArray.set(null);

            tempArrayIndex++;
        }

        for (var entry : tempArray) {
            if (entry == null) {
                continue;
            }

            insert(entry.key, entry.value);
        }

        return true;
    }

    @Override
    public int size() {
        return size;
    }

    private int hashFunction(int key) {
        return Math.abs(key % mainArray.capacity);
    }

    private class Entry {
        private int key;
        private int value;

        public Entry(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private class CircularArray {
        private Entry[] entries;

        private int lastReadCursor;
        private int capacity;

        private int counted;

        public CircularArray(int initialCapacity) {
            entries = new Entry[initialCapacity];
            capacity = initialCapacity;
        }

        public boolean hasWrapped() {
            return counted > capacity;
        }

        public void startReadingAt(int index) {
            lastReadCursor = (index - 1) % entries.length;
            counted = 0;
        }

        public int getLastReadCursor() {
            return lastReadCursor;
        }

        public Entry nextEntry() {
            if (hasWrapped()) {
                throw new IllegalStateException("The implementation has probably entered an infinite loop.");
            }

            var nextToRead = (lastReadCursor + 1) % entries.length;

            var entry = entries[nextToRead];

            lastReadCursor = nextToRead;
            counted++;

            return entry;
        }

        public void set(Entry entry) {
            entries[lastReadCursor] = entry;
        }
    }
}
