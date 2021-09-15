import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;
import java.util.concurrent.Semaphore;
import static java.lang.Thread.sleep;

public class Train implements Runnable {

    private final int TRAIN_ID;
    private int speed;
    private boolean forwardDirection; // forward direction = positive speed
    private final TSimInterface tsi = TSimInterface.getInstance();
    private static boolean yellowInit = false; // To acquire the starting segment
    private static boolean brownInit = false; // To acquire the starting segment

    // Semaphores for different parts of the track
    // See reference picture for colors
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

                // To avoid double triggers
                if (se.getStatus() == 2) {
                    continue;
                }

                // ########################### From North to South ######################### //

                // Want to enter Red coming from North Station A
                if (activationCheck(15,7, se, forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 17, 7, TSimInterface.SWITCH_RIGHT, red, tsi);
                }

                // Want to enter Red coming from North Station B
                if (activationCheck(15, 8, se, forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 17, 7, TSimInterface.SWITCH_LEFT, red, tsi);
                }

                // Has entered red coming from North Station A
                if (activationCheck(19,7, se, forwardDirection)){
                    // So as not to release a permit if not acquired
                    if(yellow.availablePermits() == 0)
                        yellow.release();
                }

                // Want to enter Blue coming from Red
                if (activationCheck(17,9, se, forwardDirection)) {
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
                if (activationCheck(13, 9, se, forwardDirection)){
                    red.release();
                }

                // Has entered Blue2 coming from Red
                if (activationCheck(13,10, se, forwardDirection)){
                    red.release();
                }

                // Want to enter Green coming from Blue
                if (activationCheck(6, 9, se, forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 4, 9, TSimInterface.SWITCH_LEFT, green, tsi);
                }

                // Want to enter Green coming from Blue2 (side track)
                if (activationCheck(6, 10, se, forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 4, 9, TSimInterface.SWITCH_RIGHT, green, tsi);
                }

                // Has entered Green coming from Blue
                if (activationCheck(2, 9, se, forwardDirection)){
                    if(blue.availablePermits() == 0)
                        blue.release();
                }

                // Want to enter South Station coming from Green
                if (activationCheck(1, 11, se, forwardDirection)) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    if (brown.tryAcquire()) {
                        tsi.setSwitch(3,11,TSimInterface.SWITCH_LEFT);
                    } else{
                        tsi.setSwitch(3,11,TSimInterface.SWITCH_RIGHT); // need sync?
                    }
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered South Station A coming from Green
                if (activationCheck(5, 11, se, forwardDirection)){
                    green.release();
                }

                // Has entered South Station B coming from Green
                if (activationCheck(3, 13, se, forwardDirection)){
                    green.release();
                }

                // ########################### From South to North ######################### //

                // Want to enter Green coming from South Station A
                if (activationCheck(5, 11, se, !forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 3, 11, TSimInterface.SWITCH_LEFT, green, tsi);
                }

                // Want to enter Green coming from South Station B
                if (activationCheck(3, 13, se, !forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 3, 11, TSimInterface.SWITCH_RIGHT, green, tsi);
                }

                // Has entered Green coming from South Station A
                if (activationCheck(1, 11, se, !forwardDirection)){
                    if(brown.availablePermits() == 0)
                        brown.release();
                }

                // Want to enter Blue coming from Green
                if (activationCheck(2, 9, se, !forwardDirection)) {
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
                if (activationCheck(6, 9, se, !forwardDirection)){
                    green.release();
                }

                // Has entered Blue2 coming from Green
                if (activationCheck(6, 10, se, !forwardDirection)){
                    green.release();
                }

                // Want to enter Red coming from Blue
                if (activationCheck(13, 9, se, !forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 15, 9, TSimInterface.SWITCH_RIGHT, red, tsi);
                }

                // Want to enter Red coming from Blue2
                if (activationCheck(13, 10, se, !forwardDirection)){
                    acquireSegment(TRAIN_ID, speed, 15, 9, TSimInterface.SWITCH_LEFT, red, tsi);
                }

                // Has entered Red coming from Blue
                if (activationCheck(17, 9, se, !forwardDirection)){
                    if(blue.availablePermits() == 0)
                        blue.release();
                }

                // Want to enter North Station coming from Red
                if (activationCheck(19, 7, se, !forwardDirection)) {
                    tsi.setSpeed(TRAIN_ID, 0);
                    if (yellow.tryAcquire()) {
                        tsi.setSwitch(17,7,TSimInterface.SWITCH_RIGHT);
                    } else{
                        tsi.setSwitch(17,7,TSimInterface.SWITCH_LEFT); // need sync?
                    }
                    tsi.setSpeed(TRAIN_ID, speed);
                }

                // Has entered North Station A coming from Red
                if (activationCheck(15, 7, se, !forwardDirection)){
                    red.release();
                }

                // Has entered North Station B coming from Red
                if (activationCheck(15, 8, se, !forwardDirection)){
                    red.release();
                }

                // ###################### Crossing ######################

                // Want to pass horizontally (left)
                if (activationCheck(6, 7, se)) {
                    if(forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        crossing.acquire();
                        tsi.setSpeed(TRAIN_ID, speed);
                    }else{
                        crossing.release();
                    }
                }

                // Want to pass horizontally (right)
                if (activationCheck(10, 7, se)) {
                    if(!forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        crossing.acquire();
                        tsi.setSpeed(TRAIN_ID, speed);
                    }else{
                        crossing.release();
                    }
                }

                // Want to pass vertically (upper)
                if (activationCheck(8, 5, se)) {
                    if(forwardDirection){
                        tsi.setSpeed(TRAIN_ID, 0);
                        crossing.acquire();
                        tsi.setSpeed(TRAIN_ID, speed);
                    }else{
                        crossing.release();
                    }
                }

                // Want to pass vertically (lower)
                if (activationCheck(10, 8, se)) {
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
                if (activationCheck(14, 3, se)) {
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
                if (activationCheck(14, 5, se)) {
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
                if (activationCheck(14, 13, se)) {
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
     * @param direction  Train direction
     * @return                  Boolean
     */
    private boolean activationCheck(int xPos, int yPos, SensorEvent se, boolean direction) {
        return se.getXpos() == xPos && se.getYpos() == yPos && se.getStatus() == SensorEvent.ACTIVE && direction;
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

    /**
     * Sets speed to 0,
     * waits to acquire permit,
     * sets the switch
     * sets the speed back up to the initial speed
     *
     * @param trainID
     * @param speed
     * @param switchPosX
     * @param switchPosY
     * @param switchDir right or left
     * @param sem the segment
     * @param tsi TSim interface
     */

    private void acquireSegment(int trainID, int speed, int switchPosX, int switchPosY, int switchDir, Semaphore sem, TSimInterface tsi){
        try {
            tsi.setSpeed(trainID, 0); // Sets speed to 0 to wait on passing train
            sem.acquire(); // Waiting until acquired
            tsi.setSwitch(switchPosX, switchPosY, switchDir);
            tsi.setSpeed(trainID, speed);
        } catch (CommandException | InterruptedException ce) {
            ce.printStackTrace();
            System.out.println("CAT-ASTROPHE");
        }
    }
}
