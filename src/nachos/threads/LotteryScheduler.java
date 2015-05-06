package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.PriorityQueue;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.Enumeration;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
	/**
	 * Allocate a new lottery scheduler.
	 */
	
	private static class lotteryTest1 implements Runnable {
	
		lotteryTest1(int which, Lock l){
			this.l = l;
			ip = which;
		}
		public void run(){
			
			LotteryScheduler schduler = (LotteryScheduler)(ThreadedKernel.scheduler);
			int lottery = schduler.getThreadState(KThread.currentThread()).getPriority();
			ThreadedKernel.alarm.waitUntil(2000);
			l.acquire();
			System.out.println(KThread.currentThread().getName() + " has priority " + lottery);
			l.release();
		}
		
		private Lock l;
		private int ip;
	}
	
	private static class lotteryTest2 implements Runnable {
		
		lotteryTest2(int which, Lock l){
			this.l = l;
			ip = which;
		}
		public void run(){
			
			LotteryScheduler schduler = (LotteryScheduler)(ThreadedKernel.scheduler);
			schduler.getThreadState(KThread.currentThread()).print();
					
			ThreadedKernel.alarm.waitUntil(2000);
			l.acquire();
			schduler.getThreadState(KThread.currentThread()).print();
			l.release();
		}
		
		private Lock l;
		private int ip;
	}
	
private static class lotteryTest4 implements Runnable {
		
		lotteryTest4(int which, Lock l){
			this.l = l;
			ip = which;
		}
		public void run(){
			
			LotteryScheduler schduler = (LotteryScheduler)(ThreadedKernel.scheduler);
			schduler.getThreadState(KThread.currentThread()).print();
			Lock acquiredLock = new Lock();
			
			ThreadedKernel.alarm.waitUntil(2000);
			
			acquiredLock.acquire();
			
			if (ip == 5)
			{
				 
				 KThread t = new KThread(new lotteryTest2(ip+1,acquiredLock)).setName("threads "+ String.valueOf(ip+1)+" ");
			     schduler.getThreadState(t).setPriority((int)(Math.random() * 20 + 1));
			     t.fork();
			}
			else
			{
				KThread t = new KThread(new lotteryTest4(ip+1,acquiredLock)).setName("threads "+ String.valueOf(ip+1)+" ");
			     schduler.getThreadState(t).setPriority((int)(Math.random() * 20 + 1));
			     t.fork();
			}
			
			l.acquire();
			schduler.getThreadState(KThread.currentThread()).print();
			l.release();
			
			acquiredLock.release();
			
			schduler.getThreadState(KThread.currentThread()).print();
		}
		
		private Lock l;
		private int ip;
	}
	
	
	public static void selfTest()
	{
		KThread[] threads = new KThread[8];
		Lock l = new Lock();
		LotteryScheduler schduler = (LotteryScheduler)(ThreadedKernel.scheduler);
		
		System.out.println("-------- lottery test 1 ---------");
		
		for (int j = 1; j < 10; j++)
		{
		
		System.out.println("round" + j);
		
		for(int i=0; i<8; i++)
		{
		     threads[i] = new KThread(new lotteryTest1(i,l)).setName("threads "+ String.valueOf(i)+" ");
		     schduler.getThreadState(threads[i]).setPriority((int)(10 * i + 5));
		     threads[i].fork();
		}
		
		l.acquire();
		ThreadedKernel.alarm.waitUntil(3000);
		l.release();
		
		ThreadedKernel.alarm.waitUntil(30000);
		}
		
       System.out.println("-------- lottery test 2 ---------");
		
		for (int j = 1; j < 3; j++)
		{
		
		System.out.println("");
		
		for(int i=0; i<8; i++)
		{
		     threads[i] = new KThread(new lotteryTest2(i,l)).setName("threads "+ String.valueOf(i)+" ");
		     schduler.getThreadState(threads[i]).setPriority((int)(Math.random() * 10 * i + 1));
		     threads[i].fork();
		}
		
		System.out.println("Before getting the lock");
		schduler.getThreadState(KThread.currentThread()).print();
		l.acquire();
		ThreadedKernel.alarm.waitUntil(3000);
		System.out.println("After getting the lock");
		schduler.getThreadState(KThread.currentThread()).print();
		l.release();
		
		ThreadedKernel.alarm.waitUntil(30000);
		}
		
        System.out.println("-------- lottery test 3 ---------");
		
		for (int j = 1; j < 3; j++)
		{
		Lock[] ls = new Lock[8];
		System.out.println("");
		
		for(int i=0; i<8; i++)
		{
			 ls[i] = new Lock();
		     threads[i] = new KThread(new lotteryTest2(i,ls[i])).setName("threads "+ String.valueOf(i)+" ");
		     schduler.getThreadState(threads[i]).setPriority((int)(Math.random() * 10 * i + 1));
		     threads[i].fork();
		}
		
		System.out.println("Before getting the lockes");
		schduler.getThreadState(KThread.currentThread()).print();
		for (int t=0; t<8; t++)
		{
		    ls[t].acquire();
		}
		ThreadedKernel.alarm.waitUntil(3000);
		for (int t=0; t<8; t++)
		{
		System.out.println("Before releaving the lock" + String.valueOf(t));
		schduler.getThreadState(KThread.currentThread()).print();
		ls[t].release();
		}
		
		ThreadedKernel.alarm.waitUntil(30000);
		}
		
		System.out.println("-------- lottery test 4 ---------");
		
		for (int j = 0; j < 3; j++)
		{
		
		System.out.println("");
		
        KThread thread = new KThread(new lotteryTest4(1,l)).setName("threads "+ String.valueOf(1)+" ");
		schduler.getThreadState(thread).setPriority((int)(Math.random() * 10 + 1));
		thread.fork();
		
		System.out.println("Before getting the lock");
		schduler.getThreadState(KThread.currentThread()).print();
		l.acquire();
		for (int k = 0; k < 10; k++)
		{
		ThreadedKernel.alarm.waitUntil(3000);
		schduler.getThreadState(KThread.currentThread()).print();
		}
		l.release();
		
		ThreadedKernel.alarm.waitUntil(30000);
		}
	}
	
	public LotteryScheduler() {
	}

	/**
	 * Allocate a new lottery thread queue.
	 *
	 * @param	transferPriority	<tt>true</tt> if this queue should
	 *					transfer tickets from waiting threads
	 *					to the owning thread.
	 * @return	a new lottery thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		// implement me
		return new LotteryQueue(transferPriority);
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= 1);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == Integer.MAX_VALUE)
			return false;

		setPriority(thread, priority+1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == 1)
			return false;

		setPriority(thread, priority-1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	protected LotteryThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new LotteryThreadState(thread);

		return (LotteryThreadState) thread.schedulingState;
	}

	protected class LotteryQueue extends PriorityQueue
	{
		LotteryQueue() {
		}

		LotteryQueue(boolean transferPriority) {
			super(transferPriority);
		}

		@Override
		protected ThreadState pickNextThread() {
			if(waitQueue.isEmpty()) return null;

			int randNum = (int)(Math.random() * getTotalTickets());

			for (ThreadState ts : waitQueue) {
				randNum -= ts.getEffectivePriority();
				if(randNum < 0) return ts;
			}

			Lib.assertNotReached("LotteryQueue.pickNextThread didn't pick a thread!");
			return null;
		}

		protected int getTotalTickets()
		{
			int res = 0;

			for (ThreadState ts : waitQueue) {
				res += ts.getEffectivePriority();
			}
			return res;
		}

		protected void donatePriority() {
			int newDonation = 0;

			if (transferPriority)
				newDonation = Math.max(newDonation , this.getTotalTickets());

			if (newDonation == donation)
				return;

			donation = newDonation;
			if (this.resAccessing != null) {
				this.resAccessing.resources.put(this , donation);
				this.resAccessing.updatePriority();
			}
		}
	}

	protected class LotteryThreadState extends ThreadState
	{
		public LotteryThreadState() {
		}

		public LotteryThreadState(KThread thread) {
			super(thread);
		}

		protected void updatePriority() {
			int newEffectivePriority = originalPriority;
			if (!resources.isEmpty()) {
				for (Enumeration<PriorityQueue> queues = resources.keys(); 
						queues.hasMoreElements(); ) {
					PriorityQueue q = queues.nextElement();
					newEffectivePriority += q.donation;
				}
			}
			if (newEffectivePriority == priority)
				return;

			priority = newEffectivePriority;
			if (resourceWaitQueue != null)
				resourceWaitQueue.donatePriority();
		}
	}
}
