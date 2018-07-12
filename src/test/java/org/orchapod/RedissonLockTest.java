package org.orchapod;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;

/**
 * Unit test for simple App.
 */
public class RedissonLockTest extends TestCase{

	private boolean verbose = true;
	private int read_thread_count = 20;
	private RedissonClient client;
	public static final String KEY = "redisson.test.lockpoint";

	static{
		// Shuting the netty and redisson logger up
		Configurator.setRootLevel(Level.WARN);
	}

	public void setUp() throws Exception{
		try{
			Config config = new Config();
			String url = "redis://localhost:6379";
			config.useSingleServer().setAddress(url);
			this.client = Redisson.create(config);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	public void teardown(){
		this.client.shutdown();
	}

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public RedissonLockTest(String testName){
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite(){
		return new TestSuite(RedissonLockTest.class);
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
		final RReadWriteLock rrrwl = this.client.getReadWriteLock(KEY);
		final CountDownLatch cdl = new CountDownLatch(1);
		final AtomicInteger counter = new AtomicInteger(0);
		final String label = "Redisson";
		final long task_duration = 4000;
		int result = 0;

		// Create write thread
		Thread writeThread = new Thread(new WriteThread(label, rrrwl, cdl, this.verbose, task_duration)); 

		// Create read threads
		Thread[] readThreads = new Thread[this.read_thread_count];
		for(int i = 0; i < readThreads.length; i++){
			readThreads[i] = new Thread(new ReadThread(label, rrrwl, counter, this.verbose, task_duration, i+1));
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
		final RReadWriteLock rrrwl = this.client.getReadWriteLock(KEY);
		final CountDownLatch cdl = new CountDownLatch(1);
		final AtomicInteger counter = new AtomicInteger(0);
		final String label = "Redisson";
		final long task_duration = 4000;
		int result = 0;

		// Create write thread
		Thread writeThread = new Thread(new WriteThread(label, rrrwl, cdl, this.verbose, task_duration)); 

		// Create read threads
		Thread[] readThreads = new Thread[this.read_thread_count];
		for(int i = 0; i < readThreads.length; i++){
			readThreads[i] = new Thread(new ReadThread(label, rrrwl, counter, this.verbose, task_duration, i+1));
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
