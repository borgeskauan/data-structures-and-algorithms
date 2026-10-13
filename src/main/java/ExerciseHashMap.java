import java.util.OptionalInt;

/** A map from integer keys to integer values. */
public interface ExerciseHashMap {
    void put(int key, int value);

    OptionalInt get(int key);

    boolean containsKey(int key);

    boolean remove(int key);

    int size();
}
