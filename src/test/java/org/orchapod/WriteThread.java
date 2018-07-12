package org.orchapod;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

class WriteThread implements Runnable{

	private final String label;
	private final ReadWriteLock rwl;
	private final CountDownLatch cdl;
	private final boolean verbose;
	private final long task_duration;

	public WriteThread(
		final String label, 
		final ReadWriteLock rwl, 
		final CountDownLatch cdl, 
		final boolean verbose, 
		final long task_duration
	){
		this.label = label.trim() + " ";
		this.rwl = rwl;
		this.cdl = cdl;
		this.verbose = verbose;
		this.task_duration = task_duration;
	}

	public void run(){
		
		// Announce that thread has been created
		if(this.verbose) System.out.println(this.label + "#WRITE# Thread Started!");

		// Lock
		Lock lock = this.rwl.writeLock(); // Creates writelock
		lock.lock();

		// Announce that lock has been successfully created
		if(this.verbose) System.out.println(this.label + "#WRITE# Thread locked!");

		// Pretend that there's computationally intensive task to do
		ReentrantLockTest.sleep(this.task_duration);

		// Unlock
		lock.unlock();

		// Announce that lock is released
		if(this.verbose) System.out.println(this.label + "#WRITE# Thread unlocks!");

		// Count down
		this.cdl.countDown();
	}
}
