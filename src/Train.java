import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

public class Train implements Runnable {

    private final int TRAIN_ID;
    private int speed;
    private boolean direction;
    public TSimInterface tsi = TSimInterface.getInstance(); // Change to private?

    public Train(int TRAIN_ID, int speed, boolean direction) {
        this.TRAIN_ID = TRAIN_ID;
        this.speed = speed;
        this.direction = direction;
    }


    @Override
    public void run() {
        System.out.println("choo choo");

        while(true) {
            try {
                SensorEvent se = tsi.getSensor(TRAIN_ID);

                if (se.getXpos() == 7 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE)
                    System.out.println("Houston, we've got contact. dick n balls");


                /*
                if (se.getXpos() ==  && se.getYpos() == 11 && se.getStatus() == SensorEvent.ACTIVE) {
                    // to South
                    if (!direction) {
                        tsi.setSpeed(id, 0);
                        fourWay.acquire();
                        tsi.setSpeed(id, speed);
                        // to North
                    } else {
                        fourWay.release();
                    }
                }*/
            } catch (CommandException | InterruptedException ce) {
                ce.printStackTrace();
                System.out.println("CAT-ASTROPHE");
            }





        }
    }
}
