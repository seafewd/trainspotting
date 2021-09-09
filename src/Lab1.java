import TSim.*;

public class Lab1 {

  public Lab1(int speed1, int speed2) {

    Train train1 = new Train(1, speed1, true);
    Train train2 = new Train(2, speed2, false);

    Thread thread1 = new Thread(train1);
    Thread thread2 = new Thread(train2);

    thread1.start();
    thread2.start();
  }
}
