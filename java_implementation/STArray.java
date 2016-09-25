/* 
 * Wrapper for regular Java Arrays to
 * facilitate comparisons with Persistent Arrays.
 *
 */

public class STArray<T> implements ArrayInterface<T> {
  private T[] data;

  public STArray(int size, T initial_value) {
    this.data = (T[]) (new Object[size]);
    for (int i = 0; i < size; i++) {
      this.data[i] = initial_value;
    }
  }

  public int size() {
    return this.data.length;
  }

  public STArray set(int pos, T value) {
    this.data[pos] = value;
    return this;
  }

  public T get(int pos) {
    return this.data[pos];
  }
}
