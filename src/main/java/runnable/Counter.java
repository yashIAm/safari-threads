package runnable;

public class Counter {
  static volatile long counter = 0;

  public static void main(String[] args) throws Throwable {
    Runnable r = () -> {
      for (int i = 0; i < 2_000_000; i++) {
        counter++;
      }
    };

//    r.run();
//    r.run();
    Thread t1 = new Thread(r);
    t1.start();
    Thread t2 = new Thread(r);
    t2.start();
    t1.join();
    t2.join();
    System.out.println("Value of counter is " + counter);
  }
}
