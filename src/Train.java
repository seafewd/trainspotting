import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;
import static java.lang.Thread.sleep;

public class Train implements Runnable {

    private final int TRAIN_ID;
    private int speed;
    private boolean forwardDirection;
    public TSimInterface tsi = TSimInterface.getInstance(); // Change to private?

    public Train(int TRAIN_ID, int speed, boolean direction) {
        this.TRAIN_ID = TRAIN_ID;
        this.speed = speed;
        this.forwardDirection = direction;
    }


    @Override
    public void run() {
        System.out.println("choo choo");

        while(true) {
            try {
                SensorEvent se = tsi.getSensor(TRAIN_ID);

                //tsi.setSpeed(1,15);
                tsi.setSwitch(17, 7, 2);
                tsi.setSwitch(15, 9, 2);
                tsi.setSwitch(3,11, 2);



                //if (se.getXpos() == 7 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE)


                // North train station
                if (se.getXpos() == 14 && se.getYpos() == 3 && se.getStatus() == SensorEvent.ACTIVE) {
                    // to South
                    if (!forwardDirection) {
                        tsi.setSpeed(TRAIN_ID, 0);
                        sleep(2000);
                        tsi.setSpeed(TRAIN_ID, speed);
                        forwardDirection = !forwardDirection;
                        // to North
                    } else {
                        //fourWay.release();
                    }
                }

                // South train station
                if (se.getXpos() == 14 && se.getYpos() == 13 && se.getStatus() == SensorEvent.ACTIVE) {
                    // to South
                    if (forwardDirection) {
                        tsi.setSpeed(TRAIN_ID, 0);
                        sleep(2000);
                        tsi.setSpeed(TRAIN_ID, -speed);
                        forwardDirection = !forwardDirection;
                        // to North
                    } else {
                        //fourWay.release();
                    }
                }


            } catch (CommandException | InterruptedException ce) {
                ce.printStackTrace();
                System.out.println("CAT-ASTROPHE");
            }





        }
    }
}
