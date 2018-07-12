package org.orchapod;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ReentrantLockTest extends TestCase{

	private boolean verbose = true;
	private int read_thread_count = 20;

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public ReentrantLockTest(String testName){
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite(){
		return new TestSuite(ReentrantLockTest.class);
	}

	public static void sleep(long time){
		try{ 
			Thread.sleep(time/8); 
		}catch(InterruptedException ie){ 
			ie.printStackTrace(); 
			System.exit(1); 
		}
	}

	/**
	*/
	public void testReadLockBeforeWriteLock() throws InterruptedException{

		// Create Lock, CDL, counter, and label
		final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
		final CountDownLatch cdl = new CountDownLatch(1);
		final AtomicInteger counter = new AtomicInteger(0);
		final String label = "RRWL";
		final long task_duration = 4000;
		int result = 0;

		// Create write thread
		Thread writeThread = new Thread(new WriteThread(label, rrwl, cdl, this.verbose, task_duration)); 

		// Create read threads
		Thread[] readThreads = new Thread[this.read_thread_count];
		for(int i = 0; i < readThreads.length; i++){
			readThreads[i] = new Thread(new ReadThread(label, rrwl, counter, this.verbose, task_duration, i+1));
		}

		// Fire off read threads
		for(int i = 0; i < readThreads.length; i++) readThreads[i].start();

		// Wait a bit to let Read Threads to acquire a lock 
		sleep(task_duration/8);

		// Fire off write thread
		writeThread.start();

		// Wait for long enough for Read Threads to finish
		sleep(task_duration);

		// Collect result
		result = counter.get();

		// Announce that result has been collected
		if(this.verbose) System.out.println(label + " Result has been collected! Result: " + result);

		// Wait for writer thread to finish
		cdl.await();

		// Join all read threads
		for(int i = 0; i < readThreads.length; i++) readThreads[i].join();

		// Report the results
		if(this.verbose){
			StringBuilder sb = new StringBuilder();
			sb.append(label);
			sb.append(' ');
			sb.append("Experiment result: Expected counter value: ");
			sb.append(readThreads.length);
			sb.append(",\tactual counter value: ");
			sb.append(result);
			if(readThreads.length == result) sb.append("\t TEST PASSED!");
			else sb.append("\t TEST FAILED!");
			System.out.println(sb.toString());
		}
		assertEquals(readThreads.length, result);
	}

	public void testReadLockAfterWriteLock() throws InterruptedException{

		// Create Lock, CDL, counter, and label
		final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
		final CountDownLatch cdl = new CountDownLatch(1);
		final AtomicInteger counter = new AtomicInteger(0);
		final String label = "RRWL";
		final long task_duration = 4000;
		int result = 0;

		// Create write thread
		Thread writeThread = new Thread(new WriteThread(label, rrwl, cdl, this.verbose, task_duration)); 

		// Create read threads
		Thread[] readThreads = new Thread[this.read_thread_count];
		for(int i = 0; i < readThreads.length; i++){
			readThreads[i] = new Thread(new ReadThread(label, rrwl, counter, this.verbose, task_duration, i+1));
		}

		// Fire off write thread
		writeThread.start();

		// Wait a bit to let Write Thread to acquire a lock 
		sleep(task_duration/8);

		// Fire off read threads
		for(int i = 0; i < readThreads.length; i++) readThreads[i].start();

		// Wait for writer thread to finish
		cdl.await();

		// Wait for long enough for Read Threads to finish
		sleep(task_duration);

		// Collect result
		result = counter.get();

		// Announce that result has been collected
		if(this.verbose) System.out.println(label + " Result has been collected! Result: " + result);

		// Join all read threads
		for(int i = 0; i < readThreads.length; i++) readThreads[i].join();

		// Report the results
		if(this.verbose){
			StringBuilder sb = new StringBuilder();
			sb.append(label);
			sb.append(' ');
			sb.append("Experiment result: Expected counter value: ");
			sb.append(readThreads.length);
			sb.append(",\tactual counter value: ");
			sb.append(result);
			if(readThreads.length == result) sb.append("\t TEST PASSED!");
			else sb.append("\t TEST FAILED!");
			System.out.println(sb.toString());
		}
		assertEquals(readThreads.length, result);
	}
}
