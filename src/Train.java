import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;
import java.util.concurrent.Semaphore;
import static java.lang.Thread.sleep;

public class Train implements Runnable {

    private final int TRAIN_ID;
    private int speed;
    private boolean forwardDirection;
    private final TSimInterface tsi = TSimInterface.getInstance();
    private static boolean yellowInit = false;
    private static boolean brownInit = false;

    // semaphores for different parts of the track
    // see reference picture for colors
    static Semaphore yellow = new Semaphore(1);
    static Semaphore red = new Semaphore(1);
    static Semaphore blue = new Semaphore(1);
    static Semaphore green = new Semaphore(1);
    static Semaphore brown = new Semaphore(1);
    static Semaphore crossing = new Semaphore(1);

    /**
     * Standard Train object, uses ID, speed and direction
     * @param TRAIN_ID  Train ID
     * @param speed     Train speed
     * @param direction Train direction
     */
    public Train(int TRAIN_ID, int speed, boolean direction) {
        this.TRAIN_ID = TRAIN_ID;
        this.speed = speed;
        this.forwardDirection = direction;
    }

    /**
     * Initial Train speed
     * @param initSpeed Initial speed
     */
    public void initSpeed(int initSpeed){
        try{
            tsi.setSpeed(TRAIN_ID, initSpeed);
        }catch (CommandException ce) {
            ce.printStackTrace();
            System.out.println("NOT GUWD");
        }
    }

    /**
     * Standard Train thread
     * Runs checks to see if a train wants to enter a track. If another train is already present, try to acquire
     * the semaphore and proceed when track is free.
     */
    @Override
    public void run() {
        System.out.println("choo choo");

        while(true) {
            try {
                SensorEvent se = tsi.getSensor(TRAIN_ID);

                // ########################### From North to South ######################### //

                // Want to enter Red coming from North Station A
                if (se.getXpos() == 15 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    red.acquire();
                    tsi.setSwitch(17,7,TSimInterface.SWITCH_RIGHT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Want to enter Red coming from North Station B
                if (se.getXpos() == 15 && se.getYpos() == 8 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    red.acquire();
                    tsi.setSwitch(17,7,TSimInterface.SWITCH_LEFT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered red coming from North Station A
                if (se.getXpos() == 19 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    if(yellow.availablePermits() == 0)
                        yellow.release();
                }

                // Want to enter Blue coming from Red
                if (se.getXpos() == 17 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    //Tries to acquire main track (blue), else flips the switch and continues on side track
                    if (blue.tryAcquire()) {
                        // Continue on track 1
                        tsi.setSwitch(15,9,TSimInterface.SWITCH_RIGHT);
                    } else{
                        // Switch to track 2
                        tsi.setSwitch(15,9,TSimInterface.SWITCH_LEFT); // need sync?
                    }
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered Blue coming from Red
                if (se.getXpos() == 13 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    red.release();
                }

                // Has entered Blue2 coming from Red
                if (se.getXpos() == 13 && se.getYpos() == 10 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    red.release();
                }

                // Want to enter Green coming from Blue
                if (se.getXpos() == 6 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    green.acquire(); // Waiting until acquired
                    tsi.setSwitch(4,9,TSimInterface.SWITCH_LEFT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Want to enter Green coming from Blue2 (side track)
                if (se.getXpos() == 6 && se.getYpos() == 10 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    green.acquire(); // Waiting until acquired
                    tsi.setSwitch(4,9,TSimInterface.SWITCH_RIGHT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered Green coming from Blue
                if (se.getXpos() == 2 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    if(blue.availablePermits() == 0)
                        blue.release();
                }

                // Want to enter South Station coming from Green
                if (se.getXpos() == 1 && se.getYpos() == 11 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    if (brown.tryAcquire()) {
                        tsi.setSwitch(3,11,TSimInterface.SWITCH_LEFT);
                    } else{
                        tsi.setSwitch(3,11,TSimInterface.SWITCH_RIGHT); // need sync?
                    }
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered South Station A coming from Green
                if (se.getXpos() == 5 && se.getYpos() == 11 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    green.release();
                }

                // Has entered South Station B coming from Green
                if (se.getXpos() == 3 && se.getYpos() == 13 && se.getStatus() == SensorEvent.ACTIVE && forwardDirection){
                    green.release();
                }

                // ########################### From South to North ######################### //

                // Want to enter Green coming from South Station A
                if (se.getXpos() == 5 && se.getYpos() == 11 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    green.acquire();
                    tsi.setSwitch(3,11,TSimInterface.SWITCH_LEFT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Want to enter Green coming from South Station B
                if (se.getXpos() == 3 && se.getYpos() == 13 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    green.acquire();
                    tsi.setSwitch(3,11,TSimInterface.SWITCH_RIGHT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered Green coming from South Station A
                if (se.getXpos() == 1 && se.getYpos() == 11 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    if(brown.availablePermits() == 0)
                        brown.release();
                }

                // Want to enter Blue coming from Green
                if (se.getXpos() == 2 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    //Tries to acquire main track (blue), else flips the switch and continues on side track
                    if (blue.tryAcquire()) {
                        tsi.setSwitch(4,9,TSimInterface.SWITCH_LEFT);
                    } else{
                        tsi.setSwitch(4,9,TSimInterface.SWITCH_RIGHT); // need sync?
                    }
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered Blue1 coming from Green
                if (se.getXpos() == 6 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    green.release();
                }

                // Has entered Blue2 coming from Green
                if (se.getXpos() == 6 && se.getYpos() == 10 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    green.release();
                }

                // Want to enter Red coming from Blue
                if (se.getXpos() == 13 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    red.acquire(); // Waiting until acquired
                    //System.out.println("got red");
                    tsi.setSwitch(15,9,TSimInterface.SWITCH_RIGHT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Want to enter Red coming from Blue2
                if (se.getXpos() == 13 && se.getYpos() == 10 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    tsi.setSpeed(TRAIN_ID, 0);
                    red.acquire(); // Waiting until acquired
                    //System.out.println("got red");
                    tsi.setSwitch(15,9,TSimInterface.SWITCH_LEFT);
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered Red coming from Blue
                if (se.getXpos() == 17 && se.getYpos() == 9 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    if(blue.availablePermits() == 0)
                        blue.release();
                    //System.out.println("got blue");
                }

                // Want to enter North Station coming from Red
                if (se.getXpos() == 19 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    if (yellow.tryAcquire()) {
                        tsi.setSwitch(17,7,TSimInterface.SWITCH_RIGHT);
                    } else{
                        tsi.setSwitch(17,7,TSimInterface.SWITCH_LEFT); // need sync?
                    }
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered North Station A coming from Red
                if (se.getXpos() == 15 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    red.release();
                }

                // Has entered North Station B coming from Red
                if (se.getXpos() == 15 && se.getYpos() == 8 && se.getStatus() == SensorEvent.ACTIVE && !forwardDirection){
                    red.release();
                }

                // ###################### Crossing ######################

                // Want to pass horizontally (left)
                if (se.getXpos() == 6 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE) {
                    if(forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        crossing.acquire();
                        tsi.setSpeed(TRAIN_ID, speed);
                    }else{
                        crossing.release();
                    }
                }

                // Want to pass horizontally (right)
                if (se.getXpos() == 10 && se.getYpos() == 7 && se.getStatus() == SensorEvent.ACTIVE) {
                    if(!forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        crossing.acquire();
                        tsi.setSpeed(TRAIN_ID, speed);
                    }else{
                        crossing.release();
                    }
                }

                // Want to pass vertically (upper)
                if (se.getXpos() == 8 && se.getYpos() == 5 && se.getStatus() == SensorEvent.ACTIVE) {
                    if(forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        crossing.acquire();
                        tsi.setSpeed(TRAIN_ID, speed);
                    }else{
                        crossing.release();
                    }
                }

                // Want to pass vertically (lower)
                if (se.getXpos() == 10 && se.getYpos() == 8 && se.getStatus() == SensorEvent.ACTIVE) {
                    if(!forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        crossing.acquire();
                        tsi.setSpeed(TRAIN_ID, speed);
                    }else{
                        crossing.release();
                    }
                }

                // ###################### Stop and turn at stations ######################

                // Stop and turn at North Station A
                if (se.getXpos() == 14 && se.getYpos() == 3 && se.getStatus() == SensorEvent.ACTIVE) {
                    if(!forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        sleep(2000);
                        speed *= -1;
                        tsi.setSpeed(TRAIN_ID, speed);
                        forwardDirection = !forwardDirection;
                    }
                    // Acquire yellow one time at start
                    if(!yellowInit){
                        yellowInit = true;
                        yellow.acquire();
                    }
                }

                // Stop and turn at North Station B
                if (activationCheck(14, 5, se, !forwardDirection)) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    sleep(2000);
                    speed *= -1;
                    tsi.setSpeed(TRAIN_ID, speed);
                    forwardDirection = !forwardDirection;
                }

                // Stop and turn at South Station A
                if (activationCheck(14, 11, se)) {
                    if(forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        sleep(2000);
                        speed *= -1;
                        tsi.setSpeed(TRAIN_ID, speed);
                        forwardDirection = !forwardDirection;
                    }
                    // Acquire yellow one time at start
                    if(!brownInit){
                        brownInit = true;
                        brown.acquire();
                    }
                }

                // Stop and turn at South Station B
                if (activationCheck(14, 13, se, forwardDirection)) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    sleep(2000);
                    speed *= -1;
                    tsi.setSpeed(TRAIN_ID, speed);
                    forwardDirection = !forwardDirection;
                }

            } catch (CommandException | InterruptedException ce) {
                ce.printStackTrace();
                System.out.println("CAT-ASTROPHE");
            }
        }
    }

    /**
     * Based on sensor position, check if sensor is active. Used in run()
     * @param xPos              Sensor x position
     * @param yPos              Sensor y position
     * @param se                Sensor event
     * @param forwardDirection  Train direction
     * @return                  Boolean
     */
    private boolean activationCheck(int xPos, int yPos, SensorEvent se, boolean forwardDirection) {
        return se.getXpos() == xPos && se.getYpos() == yPos && se.getStatus() == SensorEvent.ACTIVE && forwardDirection;
    }

    /**
     * Based on sensor position, check if sensor is active. Used in run()
     * @param xPos              Sensor x position
     * @param yPos              Sensor y position
     * @param se                Sensor event
     * @return                  Boolean
     */
    private boolean activationCheck(int xPos, int yPos, SensorEvent se) {
        return se.getXpos() == xPos && se.getYpos() == yPos && se.getStatus() == SensorEvent.ACTIVE;
    }
}
