/*
 * Simple unit test for PersistentArray.
 *
 * @author Ananya Kumar
 *
 */

import java.util.*;
import java.lang.System;

public class ArrayUnitTest {
  // boundary case test (use array of size 1)
  public static void small_get_test() {
    int size = 1;
    PersistentArray<Integer> A = new PersistentArray<Integer>(size, 2);
    assert A.get(0) == 2;
  }

  // tests array creation and get
  public static void init_get_test() {
    int size = 15;
    PersistentArray<Integer> A = new PersistentArray<Integer>(size, 1);
    for (int i = 0; i < size; i++) {
        assert A.get(i) == 1;
    }
  }

  // single threaded set and get test (doesn't test persistence)
  public static void single_thread_cycle_test() {
    int size = 10;
    PersistentArray<Integer> A = new PersistentArray<Integer>(size, -1);
    int num_cycles = 200;
    for (int i = 0; i < num_cycles; i++) {
        for (int j = 0; j < size; j++) {
            assert A.get(j) == i-1;
            A = A.set(j, i);
            assert A.get(j) == i;
        }
    }
  }

  // Partial persistence test
  public static void partial_persistence_drill() {
    int drill_size = 20000;
    int size = 7;
    ArrayList< PersistentArray<Integer> > A = new 
        ArrayList< PersistentArray<Integer> >(drill_size);
    A.add(new PersistentArray<Integer>(size, 0));
    for (int i = 1; i < drill_size; i++) {
        A.add(A.get(i-1).set(0, i));
    }
    for (int i = 0; i < drill_size; i++) {
        assert A.get(i).get(0) == i;
    }
  }

  private static PersistentArray<Integer> drill(PersistentArray<Integer> A,
    int pos, int start, int end) {
    int old_value = A.get(pos);
    PersistentArray<Integer> B = A;
    for (int i = start; i <= end; i++) {
        B = B.set(pos, i);
    }
    assert A.get(pos) == old_value;
    assert B.get(pos) == end;
    return B;
  }

  private static PersistentArray<Integer> set_and_test(
    PersistentArray<Integer> A, int pos, int value) {
    PersistentArray<Integer> B = A;
    int old_value = A.get(pos);
    B = A.set(pos, value);
    assert A.get(pos) == old_value;
    assert B.get(pos) == value;
    return B;
  }

  // reasonably thorough test of persistence
  public static void persistence_test() {
    int size = 20;
    assert size >= 3;
    PersistentArray<Integer> main = new PersistentArray<Integer>(size, 0);
    PersistentArray<Integer> branch1 = set_and_test(main, size - 1, 5);
    main = drill(main, size - 1, 10, 50);
    main = drill(main, 0, 10, 15);
    branch1 = drill(branch1, 0, 30, 60);
    branch1 = drill(branch1, size - 1, 100, 160);
    assert main.get(0) == 15;
    assert main.get(size - 1) == 50;

    PersistentArray<Integer> branch11 = drill(branch1, 0, 200, 250);
    PersistentArray<Integer> branch12 = drill(branch1, 0, 300, 350);
    assert branch1.get(0) == 60;
    assert branch11.get(0) == 250;
    assert branch12.get(0) == 350;

    PersistentArray<Integer> branch2 = set_and_test(main, size - 1, 5);
    main = drill(main, size - 1, 10, 50);
    main = drill(main, 0, 10, 15);
    branch2 = drill(branch2, 0, 30, 60);
    branch2 = drill(branch2, size - 1, 100, 160);
    assert main.get(0) == 15;
    assert main.get(size - 1) == 50;
  }

  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();
    small_get_test();
    init_get_test();
    single_thread_cycle_test();
    partial_persistence_drill();
    persistence_test();
    long elapsedTime = System.currentTimeMillis() - startTime;
    System.out.println("UNIT TESTS PASSED in " + elapsedTime + "ms");
  }
}
