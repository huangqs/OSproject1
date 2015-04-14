package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }
    
    private static class PriorityTest1 implements Runnable {
    	PriorityTest1(int i, int j, Lock l, Condition c) {
    		this.c = c;
    		this.l = l;
    		number = i;
    		priority = j;
        }

    	public void run() {
    		
    		ThreadedKernel.alarm.waitUntil(2000);
    		System.out.println("wait the lock");
    		l.acquire();
    		System.out.println("Thread" + String.valueOf(number) + " with priority " + String.valueOf(priority) + " finished.");
    		l.release();
    	}
        
    	Condition c;
    	Lock l;
    	int priority;
    	int number;
    }
    
    private static class PriorityTest2 implements Runnable {
    	PriorityTest2(int i, int j) {
    		number = i;
    		priority = j;
        }

    	public void run() {
    		
    		ThreadedKernel.alarm.waitUntil(5000);
    		
    		for (int i =0; i < 300000; i++)
    		{
    			PriorityScheduler schduler = (PriorityScheduler)(ThreadedKernel.scheduler);
        		
    			int priority = schduler.getThreadState(KThread.currentThread()).getPriority();
    			
    			int Effective = schduler.getThreadState(KThread.currentThread()).getEffectivePriority();
    			
    			if (i % 100000 == 0)  System.out.println("Thread" + String.valueOf(number) + " with priority " + String.valueOf(priority) + " effective" + String.valueOf(Effective) + " running");
    		}
    	}
        
    	int priority;
    	int number;
    }
    
    public static void testPriority()
    {
    	Lock l = new Lock();
    	Condition c = new Condition(l);
    	KThread[] threads = new KThread[8];
    	
    	for(int i = 0; i < 8; i++)
    	{
    		int t = (int)(Math.random()*6 + 1);
    		threads[i] = new KThread(new PriorityTest1(i,t,l,c)).setName("thread"+i);
    		
    		PriorityScheduler schduler = (PriorityScheduler)(ThreadedKernel.scheduler);
    		schduler.getThreadState(threads[i]).setPriority(t);
    		
    		threads[i].fork();
    	}
    	
    	l.acquire();
    	System.out.println("get the lock");
    	ThreadedKernel.alarm.waitUntil(5000);
    	System.out.println("release the lock");
    	l.release();
    	
    	for(int i = 0; i < 8; i++)
    	{
    		threads[i].join();
    	}
    }
    
    
    
    public static void selftest()
    {
    	System.out.println("---------- Test 0 ----------");
    	testPriority();
    	
        KThread[] threads = new KThread[8];
    	
        System.out.println("---------- Test 1 ----------");
        
    	for(int i = 0; i < 8; i++)
    	{
    		threads[i] = new KThread(new PriorityTest2(i,i)).setName("thread"+i);
    		
    		PriorityScheduler schduler = (PriorityScheduler)(ThreadedKernel.scheduler);
    		schduler.getThreadState(threads[i]).setPriority(i);
    		
    		threads[i].fork();
    	}
		PriorityScheduler schduler = (PriorityScheduler)(ThreadedKernel.scheduler);
		
		int priority = schduler.getThreadState(KThread.currentThread()).getPriority();
		
		int Effective = schduler.getThreadState(KThread.currentThread()).getEffectivePriority();
		
		System.out.println("Thread main with priority " + String.valueOf(priority) + " effective" + String.valueOf(Effective) + " running");
	
    	for(int i = 0; i < 8; i++)
    	{
    		threads[i].join();
    	}

        System.out.println("---------- Test 2 ----------");
        
    	for(int i = 7; i >= 0; i--)
    	{
    		threads[i] = new KThread(new PriorityTest2(i,i)).setName("thread"+i);
    		
    		schduler.getThreadState(threads[i]).setPriority(i);
    		
    		threads[i].fork();
    	}
    	for(int i = 0; i < 8; i++)
    	{
    		threads[i].join();
    	}
    	
        System.out.println("---------- Test 3 ----------");
        
    	for(int i = 7; i >= 0; i--)
    	{
    		threads[i] = new KThread(new PriorityTest2(i,i)).setName("thread"+i);
    		
    		schduler.getThreadState(threads[i]).setPriority(i);
    		
    		threads[i].fork();
    	}
    	
    	Lock l = new Lock();
    	Condition c = new Condition(l);
    	
        KThread thread2s = new KThread(new PriorityTest1(1,2,l,c)).setName("thread x");
        
		schduler.getThreadState(thread2s).setPriority(2);
    	
    	thread2s.fork();
    	
    	l.acquire();
    	System.out.println("get the lock");
    	ThreadedKernel.alarm.waitUntil(5000);
    	System.out.println("release the lock");
    	l.release();
    	
    	for(int i = 0; i < 8; i++)
    	{
    		threads[i].join();
    	}
    	//ThreadedKernel.alarm.waitUntil(50000);
    	
    	
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	    this.waitQueue = new java.util.PriorityQueue<ThreadState>(new java.util.Comparator<ThreadState>()
	    {
			@Override
			public int compare(ThreadState arg0, ThreadState arg1) {
				int priority0 = arg0.getEffectivePriority();
				int priority1 = arg1.getEffectivePriority();
				if(priority0 > priority1) return -1;
				if(priority0 < priority1) return 1;
				int num0 = arg0.serialNum;
				int num1 = arg1.serialNum;
				if(num0 > num1) return 1;
				if(num0 < num1) return -1;
				return 0; //TODO: first come first served
			}
	    });
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	    /*System.out.println("Next Thread");
	    for(ThreadState s:this.waitQueue)
	    {
	    	System.out.println(s.thread.getName());
	    	System.out.println(s.priority);
	    	System.out.println(s.effective);
	    }*/
	    if(this.transferPriority)
	    {
	    	getThreadState(this.waitedThread).waitedQueue.remove(this);
	    	getThreadState(this.waitedThread).resetPriority();
	    }
	    if(this.waitQueue.isEmpty()) {
	    	this.waitedThread = null; 
	    	return null;
	    	}
	    ThreadState res = waitQueue.remove();
	    res.acquire(this);
	    return res.thread;
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
	    // implement me
	    return waitQueue.peek();
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}
	
	public int getMaxPriority(){
		ThreadState ts=this.pickNextThread();
		if(ts==null) return priorityMinimum;
		return ts.getEffectivePriority();
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
	public KThread waitedThread;
	private java.util.PriorityQueue<ThreadState> waitQueue;
	public int serialNum=0;
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    // implement me
	    return effective;
	    //return priority;
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
		return;
	    
	    this.priority = priority;
	    this.resetPriority();
	    
	    // implement me
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	    // implement me
		waitQueue.waitQueue.add(this);
		this.selfQueue = waitQueue;
		this.serialNum = waitQueue.serialNum++;
		if(waitQueue.transferPriority)
			getThreadState(waitQueue.waitedThread).setEffectivePriority(this.effective);
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
		if(waitQueue.transferPriority) waitedQueue.add(waitQueue);
	    waitQueue.waitedThread = this.thread;
	    this.selfQueue = null;
	}	
	
	public void setEffectivePriority(int priority)
	{
		if(this.effective < priority)
		{
			this.effective = priority;
			if(this.selfQueue != null)
			{
				this.selfQueue.waitQueue.remove(this);
				this.selfQueue.waitQueue.add(this);
				if(this.selfQueue.transferPriority)
					getThreadState(this.selfQueue.waitedThread).setEffectivePriority(priority);
			}
		}
	}
	
	public void resetPriority()
	{
		this.effective = waitedQueue.isEmpty() ? priorityMinimum : waitedQueue.peek().getMaxPriority();
		if(this.effective<this.priority) this.effective = this.priority;
	}

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	protected int effective;
    protected PriorityQueue selfQueue = null;
    
    private java.util.PriorityQueue<PriorityQueue> waitedQueue = new java.util.PriorityQueue<PriorityQueue>(new java.util.Comparator<PriorityQueue>()
    	    {
    			@Override
    			public int compare(PriorityQueue arg0, PriorityQueue arg1) {
    				int priority0 = arg0.getMaxPriority();
    				int priority1 = arg1.getMaxPriority();
    				if(priority0 > priority1) return -1;
    				if(priority0 < priority1) return 1;
    				return 0;
    			}
    	    });
    public int serialNum=0;
    
    }
}
