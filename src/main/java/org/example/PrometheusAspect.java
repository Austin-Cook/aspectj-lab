package org.example;

import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

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

  //New Prometheus Metrics
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
   * This pointcut targets the serverOperation method in the Main package
   */
  @Pointcut("execution(* org.example.Main.serverOperation(..))")
  public void serverOperationExecution(){}

  /**
   * This pointcut targets the startThread Method in the Main.java
   */
  @Pointcut("execution(* org.example.Main.startThread(..))")
  public void startThreadPointcut(){}

  //New pointcut definitions
  @Pointcut("execution(public T org.example.BST.remove(T))")
  public void removeNodePointcut(){}

    @Pointcut("execution(public boolean org.example.BST.add(T))")
    public void addNodePointcut(){}

  /**
   * This After Advice tells the numberOfIterations to increment after the serverOperation finishes
   * @param joinPoint Holds a reference to the method that was executed
   */
  @After("serverOperationExecution()")
  public void afterServerOperation(JoinPoint joinPoint) {
      numberOfIterations.inc();
  }

  /**
   * Starts the Prometheus Exporter Server
   * All prometheus data is viewable on localhost:SERVER_PORT/metrics if prometheus is running and configured to scrape data from your given server port
   *@param joinPoint Holds a reference to the method that was executed
   */
  @Before("startThreadPointcut()")
  public void afterThreadInitialization(JoinPoint joinPoint) {
    final int SERVER_PORT = 8080;
          try {
            HTTPServer server = new HTTPServer(SERVER_PORT);
            System.out.println("Prometheus exporter running on port " + SERVER_PORT);
        } catch (IOException e) {
            System.out.println("Prometheus exporter was unable to start");
            e.printStackTrace();
        }
  }

  // New advices
  @Around("removeNodePointcut()")
  public Object aroundBSTRemove(ProceedingJoinPoint joinPoint) throws Throwable {
      try {
          joinPoint.proceed();
          numberOfNodes.dec();
      } catch (FailedRemoveException e) {
          failedRemoves.inc();
      }

      return null;
  }

  @Around("addNodePointcut()")
  public Object aroundBSTAdd(ProceedingJoinPoint joinPoint) throws Throwable {
      Summary.Timer timer = insertionTimer.startTimer();
      Object returnObject = joinPoint.proceed();
      timer.observeDuration();

      if ((boolean)returnObject) {
          numberOfNodes.inc();
      } else {
          failedAdds.inc();
      }

      return null;
  }
}
