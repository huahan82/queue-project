/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This is a test program of the queue library
 * A test file will be generated first;
 * The test file will be sent to process as multiple stations, multiple queues 
 * per station, FIFO policy, shortest queue and fairness
 * @author huahan
 */
import queueNode.*;
import manager.*;
import distributor.*;
import java.io.*;

public class QueueTest {
    File testFile;
    String filepath;
    int serviceCount;
    int queueCount;
    int queueType;
    int arrivalType;
    int dispatchType;
    int queueMax;
    int serviceLimit;
    int serviceLimitType;
    int serviceTime;
    String seviceStationName;
    String queueLineName;

    

    public QueueTest(){

       
    
}
    public void main(){
        TestWriter tw = new TestWriter();
               tw.TestWriter(serviceTime,seviceStationName, queueLineName);

 
       QueueManager queue = new QueueManager(serviceCount, queueCount, queueType,
               arrivalType, dispatchType, queueMax, serviceLimit,
               serviceLimitType, filepath);
       
    }
}
