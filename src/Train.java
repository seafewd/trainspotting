import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Train class
 * Runs concurrently with other Trains
 */

public class Train implements Runnable {
    private final int TRAIN_ID;
    private int speed;
    private TSimInterface tsi;
    private HashMap<String, Semaphore> semaphores;

    public Train(int TRAIN_ID, int speed, HashMap<String, Semaphore> semaphores) {
        this.TRAIN_ID = TRAIN_ID;
        this.speed = speed;
        this.semaphores = semaphores;
        this.tsi = TSimInterface.getInstance();
    }

    @Override
    public void run() {
        try {
            while(true) {
                System.out.println("choo choo");
                tsi.setSpeed(TRAIN_ID, speed);
                // get sensor event
                SensorEvent sensorEvent = tsi.getSensor(TRAIN_ID);
                // get sensor position if the sensor is active
                if (sensorEvent.getStatus() == SensorEvent.ACTIVE) {
                    int xPos = sensorEvent.getXpos();
                    int yPos = sensorEvent.getYpos();
                    //String sensorPos = getSensorPos(xPos, yPos);
                    //onSensorEvent(sensorPos);
                }

            }
        } catch(CommandException | InterruptedException ce) {
            ce.printStackTrace();
            System.exit(1);
        }
    }
}
