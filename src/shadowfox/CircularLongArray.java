package shadowfox;

public class CircularLongArray {
    private long[] array;
    private int startingIndex;

    public CircularLongArray(int capacity) {
        array = new long[capacity];
    }

    public void add(long element) {
        array[startingIndex] = element;
        startingIndex += 1;
        startingIndex %= array.length;
    }

    public long sum() {
        long total = 0;
        for (int i = 0; i < array.length; ++i) {
            total += array[i];
        }
        return total;
    }

    public int size() {
        return array.length;
    }
}
