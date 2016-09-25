/* 
 * Concurrent, wait-free, persistent arrays.
 *  
 * Supports .set and .get on the most recent
 * version in constant time, .get on old versions
 * in O(log size) time, and .set on old versions
 * in O(size) time. 
 *  
 * Complexities do not depend on the number of 
 * threads using the structure because of the 
 * wait-free approach.
 * 
 * Note that we avoid memory barriers in the
 * implementation of PersistentArray by using
 * the fact that RWArray method calls act as
 * memory barriers (on top of performing their
 * intended functionality).
 *
 */

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistentArray<T> implements ArrayInterface<T> {
  private class UndoLog {
    int version_before_change;
    T old_value;
    public UndoLog(int version_before_change, T old_value) {
      this.version_before_change = version_before_change;
      this.old_value = old_value;
    }
  }

  private class ArrayData {
    int size;
    T[] values;
    ArrayList< RWArray<UndoLog> > undo_lists;
    AtomicInteger latest_version;
    public ArrayData(int size, int version) {
      this.size = size;
      this.latest_version = new AtomicInteger(version);
      this.values = (T[]) (new Object[size]);
      this.undo_lists = new ArrayList< RWArray<UndoLog> >(size);
      for (int i = 0; i < size; i++) {
        this.undo_lists.add(new RWArray<UndoLog>());
      }
    }
  }

  ArrayData data;
  int version;
  int size;

  // Should not be used by consumers of this class
  public PersistentArray(ArrayData data, int version, int size) {
    this.data = data;
    this.version = version;
    this.size = size;
  }

  // Main constructor
  public PersistentArray(int size, T initial_value) {
    this.size = size;
    this.version = 0;
    this.data = new ArrayData(this.size, this.version);
    for (int i = 0; i < size; i++) { // In parallel
      this.data.values[i] = initial_value;
    }
  }

  public int size() {
    return this.size;
  }

  public PersistentArray<T> set(int pos, T elem) {
    PersistentArray<T> new_array = new PersistentArray<T>(this.data, 
      this.version + 1, this.size);

    // If old version, or if array data is holding too many versions
    if (this.data.latest_version.get() != this.version ||
        !this.data.latest_version.compareAndSet(this.version, new_array.version)
        || new_array.version % (10 * this.size) == 0) {
      new_array.data = new ArrayData(new_array.size, new_array.version);
      for (int i = 0; i < size; i++) { // In parallel
        new_array.data.values[i] = this.get(i);
      }
    } else {
      // Very important to update the undo list before we
      // update the actual value, so that when we .get
      // we can first get the value, and then check
      // the undolist to see if we have the latest value.
      T old_value = new_array.data.values[pos];
      new_array.data.undo_lists.get(pos).push_back(
        new UndoLog(this.version, old_value));
      // There's an implicit memory barrier here from push_back
      // in RWArray, which ensures that pushing the undo log
      // happens before setting new_array.data.values[pos]
    }

    // We don't need to push an undo log if we created a
    // new ArrayData because nobody will try to access
    // older versions in the new ArrayData.
    new_array.data.values[pos] = elem;
    return new_array;
  }

  public T get(int pos) {
    T guess_value = this.data.values[pos];
    // We check the version after getting the value to avoid
    // a time of check, time of use issue.
    // latest_version is an AtomicInteger, so there is an implicit
    // memory barrier here that ensures we loaded guess_value before
    // we loaded latest_version. this.version is immutable, so the
    // order in which we load it does not matter.
    if (this.data.latest_version.get() == this.version) {
      return guess_value;
    }

    // If we don't have the latest version, binary search 
    // undo_list until we find undolog with the smallest 
    // version >= our version. We want the corresponding value.
    RWArray<UndoLog> undo_list = this.data.undo_lists.get(pos);
    int lower = 0;
    int upper = undo_list.size() - 1;
    if (upper == -1 || undo_list.get(upper).version_before_change < this.version) {
      return guess_value;
    }
    while (lower < upper) {
      int mid = (lower + upper) / 2;
      UndoLog undo_log = undo_list.get(mid);
      if (undo_log.version_before_change >= this.version) {
        upper = mid;
      } else {
        lower = mid + 1;
      }
    }

    UndoLog undo_log = undo_list.get(lower);
    return undo_log.old_value;
  }
}
