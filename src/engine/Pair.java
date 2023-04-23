package sekelsta.engine;

import java.util.Objects;

public class Pair<K,V> {
    K key;
    V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair other = (Pair)o;
        return ((this.key == null && other.key == null) || (this.key != null && this.key.equals(other.key)))
            && ((this.value == null && other.value == null) || (this.value != null && this.value.equals(other.value)));
    }

    @Override
    public String toString() {
        return "<%s, %s>".format(key.toString(), value.toString());
    }
}
