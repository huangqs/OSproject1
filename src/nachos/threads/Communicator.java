package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	private boolean word_ready = false;
	private int num_listener = 0;
	private int num_speaker = 0;
	private int word = -1;
	private Lock l = new Lock();
	private Condition cond_listener = new Condition(l);
	private Condition cond_speaker = new Condition(l);

	public Communicator() {
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 *
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 *
	 * @param	word	the integer to transfer.
	 */
	public void speak(int word) {
		l.acquire();
		num_speaker++;

		while (num_listener == 0 || word_ready)
		{
			cond_speaker.sleep();
		}

		this.word = word;
		word_ready = true;

		cond_listener.wake();

		cond_speaker.sleep();
		num_speaker--;

		l.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return
	 * the <i>word</i> that thread passed to <tt>speak()</tt>.
	 *
	 * @return	the integer transferred.
	 */    
	public int listen() {
		l.acquire();
		num_listener++;

		while (!word_ready)
		{
			cond_speaker.wake();
			cond_listener.sleep();
		}

		int w = word;
		word_ready = false;

		num_listener--;

		cond_speaker.wakeAll();

		l.release();

		return w;
	}

	private static class Listener implements Runnable {
		Listener(Communicator c, int number) {
			this.number = number;
			communicator = c;
		}

		public void run() {

			KThread.yield();
			System.out.println("listener" + String.valueOf(number) + " forked.");
			int i = communicator.listen();	
			System.out.println("listener" + String.valueOf(number) + " listened " + String.valueOf(i) + " and left.");

		}
		private int number;
		private Communicator communicator;

	}

	private static class Speaker implements Runnable {
		Speaker(Communicator c, int number, int word) {
			this.number = number;
			communicator = c;
			this.word = word;
		}

		public void run() {

			KThread.yield();
			System.out.println("Speaker" + String.valueOf(number) + " forked.");
			communicator.speak(word);	
			System.out.println("Speaker" + String.valueOf(number) + " spoke " + String.valueOf(word) + " and left.");

		}
		private int number;
		private Communicator communicator;
		private int word;

	}  

	public static void Test(int s, int l)
	{
		Communicator c = new Communicator();

		KThread[] speakers = new KThread[s];
		KThread[] listeners = new KThread[l];

		for (int i = 0; i< s; i++)
		{
			speakers[i] = new KThread(new Speaker(c,i,i));
		}

		for (int i = 0; i< l; i++)
		{
			listeners[i] = new KThread(new Listener(c,i));
		}

		for (int i = 0; i< s; i++)
		{
			speakers[i].fork();
		}

		for (int i = 0; i< l; i++)
		{

			listeners[i].fork();
		}

		if (s < l)
		{
			for (int i = 0; i< s; i++)
			{
				speakers[i].join();
			}
		}
		else
		{
			for (int i = 0; i< l; i++)
			{
				listeners[i].join();
			}
		}
	}


	public static void selfTest() {

		System.out.println("--------Test when more speakers than listeners-----------");

		Test(3,5);

		System.out.println("--------Test when more listeners than speakers----------");

		Test(5,3);

		System.out.println("---------Test when only one listener----------");

		Test(4,1); 

		System.out.println("----------Test when only one speaker---------");

		Test(1,3); 

		System.out.println("---------Test when speakers equal to listeners--------");

		Test(4,4); 
	}
}
