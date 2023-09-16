package org.example;

public class Main {

    // Range of random values to be inserted and removed from BST
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 300;

    private static BST<Integer> tree;
    private static Thread bgThread;

    //Metrics Defined
    //These metric definitions will go into an aspect, But will not be static in the aspect
    //MOVED TO PrometheusAspect.java


    /**
     * Initializes a thread
     * Starts the thread
     * @param args has no effect
     */
    public static void main(String[] args) {
        tree = new BST<>();
        initializeThread();
        startThread();
    }


    /**
     * initializes a thread that will just run the serverOperation() until you tell the program to stop
     */
    private static void initializeThread(){
        //Initializes the thread
        bgThread = new Thread(() -> {
            while (true) {
                try {
                    serverOperation();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * tree.add returns TRUE if the add was SUCCESSFUL and FALSE if it FAILED
     * tree.remove doesn't return anything if it was SUCCESSFUL and throws a FailedRemoveException if it FAILS
     * @throws InterruptedException if the thread is operating and it is interrupted
     */
    private static void serverOperation() throws InterruptedException {
        // Two random values to insert or remove from the BST are selected
        int randomAdd = randomNumber();
        int randomRemove = randomNumber();

        tree.add(randomAdd);

        try {
            tree.remove(randomRemove);
        } catch (FailedRemoveException e) { }

        // The server waits for 1000 milliseconds
        Thread.sleep(1000);
    }

    /**
     * This method starts the Thread initialized by the initializeThread() method
     */
    private static void startThread(){
        bgThread.start();
    }


    /**
     * Function that selects a random number.
     * @return the randomly selected integer between MIN_VALUE and MAX_VALUE
     */
    private static int randomNumber() {
        return (int)(Math.random() * ((MAX_VALUE - MIN_VALUE) + 1)) + MIN_VALUE;
    }
}
