
import java.util.Random;

public class ArraySpeedTest {

  public static long seq_read_test(ArrayInterface<Integer> A, int cnt) {
    long startTime = System.currentTimeMillis();
    int size = A.size();
    for (int i = 0; i < cnt; i++) {
      int x = A.get(cnt % size);
    }
    return System.currentTimeMillis() - startTime;
  }

  public static long st_seq_read_test(int size, int cnt) {
    STArray A = new STArray<Integer>(size, 0);
    return seq_read_test(A, cnt);
  }

  public static long persistent_seq_read_test(int size, int cnt) {
    PersistentArray A = new PersistentArray<Integer>(size, 0);
    return seq_read_test(A, cnt);
  }

  public static long seq_write_test(ArrayInterface<Integer> A, int cnt) {
    long startTime = System.currentTimeMillis();
    int size = A.size();
    for (int i = 0; i < cnt; i++) {
      A = A.set(cnt % size, i);
    }
    return System.currentTimeMillis() - startTime;
  }

  public static long st_seq_write_test(int size, int cnt) {
    STArray A = new STArray<Integer>(size, 0);
    return seq_write_test(A, cnt);
  }

  public static long persistent_seq_write_test(int size, int cnt) {
    PersistentArray A = new PersistentArray<Integer>(size, 0);
    return seq_write_test(A, cnt);
  }

  public static long rnd_read_test(ArrayInterface<Integer> A, int cnt) {
    Random R = new Random();
    long startTime = System.currentTimeMillis();
    int size = A.size();
    for (int i = 0; i < cnt; i++) {
      int x = A.get(R.nextInt(size));
    }
    return System.currentTimeMillis() - startTime;
  }

  public static long st_rnd_read_test(int size, int cnt) {
    STArray A = new STArray<Integer>(size, 0);
    return rnd_read_test(A, cnt);
  }

  public static long persistent_rnd_read_test(int size, int cnt) {
    PersistentArray A = new PersistentArray<Integer>(size, 0);
    return rnd_read_test(A, cnt);
  }

  public static long rnd_write_test(ArrayInterface<Integer> A, int cnt) {
    Random R = new Random();
    long startTime = System.currentTimeMillis();
    int size = A.size();
    for (int i = 0; i < cnt; i++) {
      A = A.set(R.nextInt(size), i);
    }
    return System.currentTimeMillis() - startTime;
  }

  public static long st_rnd_write_test(int size, int cnt) {
    STArray A = new STArray<Integer>(size, 0);
    return rnd_write_test(A, cnt);
  }

  public static long persistent_rnd_write_test(int size, int cnt) {
    PersistentArray A = new PersistentArray<Integer>(size, 0);
    return rnd_write_test(A, cnt);
  }

  public static long rnd_old_read_test(int size, int numWrites, int numReads) {
    PersistentArray<Integer> A = new PersistentArray<Integer>(size, 0);
    PersistentArray<Integer> B = A;

    // Write random values to the array
    Random R = new Random();
    for (int i = 0; i < numWrites; i++) {
      B = B.set(R.nextInt(size), i);
    }

    // Read random values from the old array
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < numReads; i++) {
      int x = A.get(R.nextInt(size));
    }
    return System.currentTimeMillis() - startTime;
  }

  public static long rnd_new_read_test(int size, int numWrites, int numReads) {
    PersistentArray<Integer> A = new PersistentArray<Integer>(size, 0);

    // Write random values to the array
    Random R = new Random();
    for (int i = 0; i < numWrites; i++) {
      A = A.set(R.nextInt(size), i);
    }

    // Read random values from the old array
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < numReads; i++) {
      int x = A.get(R.nextInt(size));
    }
    return System.currentTimeMillis() - startTime;
  }

  private static class RandomReadTest implements Runnable {
    ArrayInterface<Integer> A;
    int cnt;
    public RandomReadTest(ArrayInterface<Integer> A, int cnt) {
      this.A = A;
      this.cnt = cnt;
    }
    public void run() {
      Random R = new Random();
      int size = A.size();
      for (int i = 0; i < cnt; i++) {
        int x = A.get(R.nextInt(size));
      }
    }
  }

  public static long threaded_rnd_read_test(int size, int cnt, int num_threads) {
    PersistentArray<Integer> A = new PersistentArray<Integer>(size, 0);
    // Write random stuff, make array A an "old" array
    PersistentArray<Integer> B = A;
    Random R = new Random();
    for (int i = 0; i < 9*size; i++) {
      B = B.set(R.nextInt(size), i);
    }
    // Launch a bunch of threads that read from A simultaneously
    Thread[] threads = new Thread[num_threads];
    for (int i = 0; i < num_threads; i++) {
      threads[i] = new Thread(new RandomReadTest(A, cnt));
    }
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < num_threads; i++) {
      threads[i].start();
    }
    try {
      for (int i = 0; i < num_threads; i++) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      return -1;
    }
    return System.currentTimeMillis() - startTime;
  }

  private static class RandomDrillTest implements Runnable {
    ArrayInterface<Integer> A;
    int cnt;
    public RandomDrillTest(ArrayInterface<Integer> A, int cnt) {
      this.A = A;
      this.cnt = cnt;
    }
    public void run() {
      for (int i = 0; i < cnt; i++) {
        int x = A.get(0);
      }
    }
  }

  public static long threaded_read_drill_test(int size, int cnt, int num_threads) {
    PersistentArray<Integer> A = new PersistentArray<Integer>(size, 0);
    // Write lots of value to element 0
    PersistentArray<Integer> B = A;
    for (int i = 0; i < 9*size; i++) {
      B = B.set(0, i);
    }
    // Launch a bunch of threads that read from A simultaneously
    Thread[] threads = new Thread[num_threads];
    for (int i = 0; i < num_threads; i++) {
      threads[i] = new Thread(new RandomReadTest(A, cnt));
    }
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < num_threads; i++) {
      threads[i].start();
    }
    try {
      for (int i = 0; i < num_threads; i++) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      return -1;
    }
    return System.currentTimeMillis() - startTime;
  }

  public static void main (String[] args) {
    int num_trials = 20;

    // Tests comparing persistent arrays with regular arrays.
    System.out.println("\nseq_read_test");
    for (int i = 0; i < num_trials; i++) {
      long st_time = st_seq_read_test(3000000, 15000000);
      long persistent_time = persistent_seq_read_test(3000000, 15000000);
      System.out.println(st_time + "," + persistent_time);
    }

    System.out.println("\nrnd_read_test");
    for (int i = 0; i < num_trials; i++) {
      long st_time = st_rnd_read_test(3000000, 15000000);
      long persistent_time = persistent_rnd_read_test(3000000, 15000000);
      System.out.println(st_time + "," + persistent_time);
    }

    System.out.println("\nseq_write_test");
    for (int i = 0; i < num_trials; i++) {
      long st_time = st_seq_write_test(3000000, 5000000);
      long persistent_time = persistent_seq_write_test(3000000, 5000000);
      System.out.println(st_time + "," + persistent_time);
    }

    System.out.println("\nrnd_write_test");
    for (int i = 0; i < num_trials; i++) {
      long st_time = st_rnd_write_test(3000000, 5000000);
      long persistent_time = persistent_rnd_write_test(3000000, 5000000);
      System.out.println(st_time + "," + persistent_time);
    }

    // Tests comparing accessing "old" elements with "new" elements in
    // persistent arrays.
    System.out.println("\nrnd_read_test (old vs new)");
    for (int i = 0; i < num_trials; i++) {
      long old_time = rnd_old_read_test(2100000, 20000000, 5000000);
      long new_time = rnd_new_read_test(2100000, 20000000, 5000000);
      System.out.println(old_time + "," + new_time);
    }

    System.out.println("\n threaded_rnd_read_test");
    // Ignore results of 3, 4 threads if you don't want to deal
    // with mysterious effects of Intel hyperthreading
    for (int num_threads = 1; num_threads < 3; num_threads++) {
      System.out.println("numthreads = " + num_threads);
      for (int j = 0; j < num_trials; j++) {
        System.out.println(threaded_rnd_read_test(100000, 1000000, num_threads));
      }
    }

    System.out.println("\n threaded_read_drill_test");
    // Ignore results of 3, 4 threads if you don't want to deal
    // with mysterious effects of Intel hyperthreading
    for (int num_threads = 1; num_threads < 3; num_threads++) {
      System.out.println("numthreads = " + num_threads);
      for (int j = 0; j < num_trials; j++) {
        System.out.println(threaded_read_drill_test(100000, 1000000, num_threads));
      }
    }
  }
}
