package badqueue;

public class BadQueue<E> {
  private E[] data = (E[]) (new Object[10]);
  private int count;

  public void put(E e) throws InterruptedException {
    synchronized (this) {
      while (count >= 10) { // MUST be a loop
        this.wait(); // TRANSACTIONALLY SENSITIVE???
      }
      data[count++] = e;
      notify();
    }
  }

  public E take() throws InterruptedException {
    synchronized (this) {
      while (count <= 0) {
        this.wait(10);
      }
//      notify(); SAFE!!! but illogical :)
      E r = data[0];
      System.arraycopy(data, 1, data, 0, --count);
      this.notify();
      return r;
    }
  }

  public static void main(String[] args) {
    BadQueue<Integer> bi = new BadQueue<>();
//    bi.put(1);
//    bi.put(2);
//    bi.put(3);
//    bi.put(4);
//
//    System.out.println(bi.take());
//    System.out.println(bi.take());
//    System.out.println(bi.take());
//    System.out.println(bi.take());
  }
}
