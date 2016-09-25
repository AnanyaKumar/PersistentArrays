/* 
 * Simple interface for arrays.
 * Set returns the array because we are simulating 
 * functional programming semantics.
 *
 */

public interface ArrayInterface<T> {
  public int size();
  public ArrayInterface<T> set(int pos, T value);
  public T get(int pos);
}