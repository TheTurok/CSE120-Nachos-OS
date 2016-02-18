package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */

	private LinkedList<Long> timeQ = new LinkedList<Long>();
  private LinkedList<Semaphore> semQ = new LinkedList<Semaphore>();



	public void timerInterrupt() {	

    int i = 0;

    while( i < timeQ.size() ){
      if(timeQ.size() == 0)
        break;

      if(Machine.timer().getTime() >= timeQ.get(i)){
        timeQ.remove(i);
          Semaphore removed = semQ.remove(i);
        removed.V();
        i--;
      }
      i++;
    }

    KThread.currentThread().yield();
}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {

      Lock lock = new Lock();
      long alarm = Machine.timer().getTime() + x;

      if( alarm >= Machine.timer().getTime()){
        Semaphore added = new Semaphore(0);
        
        lock.acquire();  
        semQ.add(added);  
        timeQ.add(alarm);
        lock.release();
        added.P();

      }

	}


// Place this function inside Alarm. And make sure Alarm.selfTest() is called inside ThreadedKernel.selfTest() method.

/*public static void selfTest() {
    KThread t1 = new KThread(new Runnable() {
        public void run() {
            long time1 = Machine.timer().getTime();
            int waitTime = 10000;
            System.out.println("Thread calling wait at time:" + time1);
            ThreadedKernel.alarm.waitUntil(waitTime);
            System.out.println("Thread woken up after:" + (Machine.timer().getTime() - time1));
            Lib.assertTrue((Machine.timer().getTime() - time1) > waitTime, " thread woke up too early.");
            
        }
    });
    t1.setName("T1");
    t1.fork();
    t1.join();
} */

public static void selfTest() {
KThread t1 = new KThread(new Runnable() {
public void run() {
long time1 = Machine.timer().getTime();
int waitTime = 10000;
System.out.println("Thread 1 calling wait at time:" + time1);
ThreadedKernel.alarm.waitUntil(waitTime);
System.out.println("Thread 1 woken up after:" + (Machine.timer().getTime() - time1));
Lib.assertTrue((Machine.timer().getTime() - time1) >= waitTime, " thread woke up too early.");

}
});
KThread t2 = new KThread(new Runnable() {
public void run() {
long time1 = Machine.timer().getTime();
int waitTime = 7000;
System.out.println("Thread 2 calling wait at time:" + time1);
ThreadedKernel.alarm.waitUntil(waitTime);
System.out.println("Thread 2 woken up after:" + (Machine.timer().getTime() - time1));
Lib.assertTrue((Machine.timer().getTime() - time1) >= waitTime, " thread woke up too early.");

}
});
t1.setName("T1");
t2.setName("T2");
t1.fork(); t2.fork();
t1.join(); t2.join();
}
 
}
