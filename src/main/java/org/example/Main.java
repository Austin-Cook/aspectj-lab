package org.example;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;

public class Main {

    // Range of random values to be inserted and removed from BST
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 300;

    private static BST<Integer> tree;
    private static Thread bgThread;

    //Metrics Defined
    //These metric definitions will go into an aspect, But will not be static in the aspect
    private static Counter failedAdds = Counter.build()
            .namespace("java")
            .name("number_of_failed_adds")
            .help("Counts the number of failed adds")
            .register();
    private static Counter failedRemoves = Counter.build()
            .namespace("java")
            .name("number_of_failed_removes")
            .help("Counts the number of failed removes")
            .register();
    private static Gauge numberOfNodes = Gauge.build()
            .namespace("java")
            .name("number_of_nodes")
            .help("Counts Num Nodes")
            .register();
    private static Summary insertionTimer = Summary.build()
            .namespace("java")
            .name("time_to_add")
            .help("returns add time")
            .register();


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
        System.out.println("In serverOperation()");
        // Two random values to insert or remove from the BST are selected
        int randomAdd = randomNumber();
        int randomRemove = randomNumber();

        Summary.Timer timer = insertionTimer.startTimer();
        if(tree.add(randomAdd)){
            numberOfNodes.inc();
        }else{
            failedAdds.inc();
        }
        timer.observeDuration();


        try{
            tree.remove(randomRemove);
            numberOfNodes.dec();
        }catch (FailedRemoveException e){
            failedRemoves.inc();
        }

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
