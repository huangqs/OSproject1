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
