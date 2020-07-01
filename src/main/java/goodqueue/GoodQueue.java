package goodqueue;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GoodQueue {

  public static void main(String[] args) {
    final int COUNT = 100;
    BlockingQueue<int[]> bi = new ArrayBlockingQueue<>(10);
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
