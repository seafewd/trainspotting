import TSim.*;

import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;
import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

import java.util.concurrent.Semaphore;

public class Lab1 {

  public Lab1(int speed1, int speed2) {

    Train train1 = new Train(1, speed1, true);
    Train train2 = new Train(2, speed2, false);

    train1.initSpeed(speed1);
    train2.initSpeed(speed2);

    Thread thread1 = new Thread(train1);
    Thread thread2 = new Thread(train2);
    thread1.start();
    thread2.start();

  }
}
