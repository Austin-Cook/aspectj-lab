package org.example;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;


@Aspect
public class PrometheusAspect {

  //Here the prometheus metric is Built and registered
  //Notice that these metrics don't need to be static like the Main method Counters
  Counter numberOfIterations = Counter.build()
        .namespace("java")
        .name("number_of_iterations")
        .help("Counts the number of attempted inserts and removes")
        .register();

  //Add your other Prometheus Metrics here


  /**
   * This pointcut targets the serverOperation method in the Main package
   */
  @Pointcut("execution(* org.example.Main.serverOperation(..))")
  public void serverOperationExecution(){}

  /**
   * This pointcut targets the startThread Method in the Main.java
   */
  @Pointcut("execution(* org.example.Main.startThread(..))")
  public void startThreadPointcut(){}

  //Add your other pointcut definitions here

  /**
   * This After Advice tells the numberOfIterations to increment after the serverOperation finishes
   * @param joinPoint Holds a reference to the method that was executed
   */
  @After("serverOperationExecution()")
  public void afterServerOperation(JoinPoint joinPoint) {
      System.out.println("From aspect: increasing numberOfIterations");
      numberOfIterations.inc();
  }


  /**
   * Starts the Prometheus Exporter Server
   * All prometheus data is viewable on localhost:SERVER_PORT/metrics if prometheus is running and configured to scrape data from your given server port
   *@param joinPoint Holds a reference to the method that was executed
   */
  @Before("startThreadPointcut()")
  public void afterThreadInitialization(JoinPoint joinPoint) {
      System.out.println("Starting server!");
    final int SERVER_PORT = 8080;
          try {
            HTTPServer server = new HTTPServer(SERVER_PORT);
            System.out.println("Prometheus exporter running on port " + SERVER_PORT);
        } catch (IOException e) {
            System.out.println("Prometheus exporter was unable to start");
            e.printStackTrace();
        }

  }



  //Add your other Advices here


}
