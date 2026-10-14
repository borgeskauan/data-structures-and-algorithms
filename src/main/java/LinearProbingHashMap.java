import java.util.OptionalInt;

public class LinearProbingHashMap implements ExerciseHashMap {

    Entry[] mainArray = new Entry[100];
    int size = 0;

    double targetLoadFactor = 0.5;


    @Override
    public void put(int key, int value) {
        int index = hashFunction(key);

        while (index < mainArray.length) {
            var entry = mainArray[index];
            if (entry == null) {
                size++;
                break;
            }

            if (entry.key == key) {
                break;
            }

            index++;
        }

        if (index == mainArray.length) {
            throw new IllegalStateException("No free slots were found");
        }

        var entry = new Entry(key, value);
        mainArray[index] = entry;

        if (size / (double) mainArray.length > targetLoadFactor) {
            resize();
        }
    }

    private void resize() {
        var oldArray = mainArray.clone();
        int initialSize = size;

        mainArray = new Entry[oldArray.length * 2];

        for (var entry : oldArray) {
            if (entry == null) {
                continue;
            }

            put(entry.key, entry.value);
        }

        size = initialSize;
    }

    @Override
    public OptionalInt get(int key) {
        int index = hashFunction(key);

        while (index < mainArray.length) {
            var entry = mainArray[index];
            if (entry == null) {
                return OptionalInt.empty();
            }

            if (entry.key == key) {
                return OptionalInt.of(entry.value);
            }

            index++;
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

        while (index < mainArray.length) {
            var entry = mainArray[index];
            if (entry == null) {
                return false;
            }

            if (entry.key == key) {
                break;
            }

            index++;
        }

        mainArray[index] = null;
        size--;

        int nextEmptyIndex = index + 1;

        while (index < mainArray.length) {
            if (mainArray[nextEmptyIndex] == null) {
                break;
            }

            nextEmptyIndex++;
        }

        var tempArray = new Entry[nextEmptyIndex - index];
        int tempArrayIndex = 0;

        nextEmptyIndex = index + 1;
        while (index < mainArray.length) {
            if (mainArray[nextEmptyIndex] == null) {
                break;
            }

            tempArray[tempArrayIndex] = mainArray[nextEmptyIndex];
            mainArray[nextEmptyIndex] = null;
            nextEmptyIndex++;
            tempArrayIndex++;
        }

        int originalSize = size;

        for (var entry : tempArray) {
            if (entry == null) {
                continue;
            }

            put(entry.key, entry.value);
        }

        size = originalSize;

        return true;
    }

    @Override
    public int size() {
        return size;
    }

    private int hashFunction(int key) {
        return Math.abs(key % mainArray.length);
    }

    private class Entry {
        private int key;
        private int value;

        public Entry(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}
