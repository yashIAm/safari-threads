package badqueue;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GoodQueue<E> {
  private ReentrantLock lock = new ReentrantLock();
  private Condition notFull = lock.newCondition();
  private Condition notEmpty = lock.newCondition();

  private E[] data = (E[]) (new Object[10]);
  private int count;

  public void put(E e) throws InterruptedException {
    lock.lock();
    try {
      while (count >= 10) { // MUST be a loop
        notFull.await(); // TRANSACTIONALLY SENSITIVE???
      }
      data[count++] = e;
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  public E take() throws InterruptedException {
    lock.lock();
    try {
      while (count <= 0) {
        notEmpty.await();
      }
//      notify(); SAFE!!! but illogical :)
      E r = data[0];
      System.arraycopy(data, 1, data, 0, --count);
      notFull.signal();
      return r;
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) {
    final int COUNT = 100;
    GoodQueue<int[]> bi = new GoodQueue<>();
    Runnable producer = () -> {
      try {
        for (int i = 0; i < COUNT; i++) {
          int[] data = {0, i}; // "transactionally unsound" -- should be equal values :)
          if (i < 50) Thread.sleep(1);
          data[0] = i;
          if (i == COUNT / 2) {
            data[0] = -99; // Verify test by breaking the data :)
          }
          System.out.println("put data " + Arrays.toString(data));
          bi.put(data);
          data = null;
        }
        System.out.println("Producer finished...");
      } catch (InterruptedException ie) {
        System.out.println("Should never happen!!!");
      }
    };
    Runnable consumer = () -> {
      try {
        for (int i = 0; i < COUNT; i++) {
          int[] data = bi.take();
          if (i < COUNT - 50) Thread.sleep(1);
          if (data[0] != data[1] || data[0] != i) {
            System.out.println("**** ERROR: index " + i + " data " + Arrays.toString(data));
          }
          System.out.println(Arrays.toString(data));
        }
        System.out.println("Consumer finished...");
      } catch (InterruptedException ie) {
        System.out.println("Should never happen!!!");
      }
    };
    new Thread(producer).start();
    new Thread(consumer).start();
    System.out.println("Started...");
  }
}
