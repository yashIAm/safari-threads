package badqueue;

import java.util.Arrays;

public class BadQueue<E> {
  private E[] data = (E[]) (new Object[10]);
  private int count;

  public void put(E e) throws InterruptedException {
    synchronized (this) {
      while (count >= 10) { // MUST be a loop
        this.wait(); // TRANSACTIONALLY SENSITIVE???
      }
      data[count++] = e;
//      notify();
      notifyAll();
    }
  }

  public E take() throws InterruptedException {
    synchronized (this) {
      while (count <= 0) {
        this.wait();
      }
//      notify(); SAFE!!! but illogical :)
      E r = data[0];
      System.arraycopy(data, 1, data, 0, --count);
//      this.notify();
      this.notifyAll();
      return r;
    }
  }

  public static void main(String[] args) {
    final int COUNT = 100;
    BadQueue<int[]> bi = new BadQueue<>();
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
