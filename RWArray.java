/* 
 * Array that supports concurrent access by one writer 
 * and multiple readers (with restricted functionality).
 * 
 * The readers can call .size() and .get(). The reader 
 * is guaranteed that all elements between 0 and .size()-1 
 * can be accessed using .get().
 * The writer can call .get, .push_back, and .size.
 *
 * @author Ananya Kumar
 *
 */

public class RWArray<T> {
  private volatile int capacity;
  private volatile int size;
  private volatile T[] data;

  public RWArray() {
    this.capacity = 10;
    this.size = 0;
    this.data = (T[]) (new Object[this.capacity]);
  }

  void push_back(T e) {
    if (this.capacity == this.size) {
      this.capacity = this.capacity * 2;
      T[] new_array = (T[]) (new Object[this.capacity]);
      for (int i = 0; i < this.size; i++) { // In parallel
        new_array[i] = this.data[i];
      }
      this.data = new_array;
    }
    // It's very important that we set the element
    // before we increase the size. Otherwise there
    // could be a race where readers think that
    // they can access this.data[this.size] but
    // this.data[this.size] has not been set.
    this.data[this.size] = e;
    this.size = this.size + 1;
  }

  T get(int pos) {
    return this.data[pos];
  }

  int size() {
    return this.size;
  }
}
