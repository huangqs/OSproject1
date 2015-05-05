package nachos.userprog;

import java.util.*;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
	super();
    }

    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler.
     */
    public void initialize(String[] args) {
	super.initialize(args);

	console = new SynchConsole(Machine.console());
	
	Machine.processor().setExceptionHandler(new Runnable() {
		public void run() { exceptionHandler(); }
	    });

	int numPhysPages = Machine.processor().getNumPhysPages();
	for(int i=0; i<numPhysPages; i++) unusedPPN.push(i);
	unusedPPNLock = new Lock();
	userFileSystem = new UserFileSystem(ThreadedKernel.fileSystem);
    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
	super.selfTest();

	/*System.out.println("Testing the console device. Typed characters");
	System.out.println("will be echoed until q is typed.");

	char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	System.out.println("");*/
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();
	
	String shellProgram = Machine.getShellProgramName();	
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));

	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }

    public static Stack<Integer> unusedPPN = new Stack<Integer>();
    public static Lock unusedPPNLock;
    
    public static class UserFileSystem implements FileSystem
    {
    	FileSystem underlyingFileSystem;
    	public UserFileSystem(FileSystem fs) {underlyingFileSystem = fs;}

		@Override
		public OpenFile open(String name, boolean create) {
			OpenFile res;
			fileRefsLock.acquire();
			
			if(fileRefs.containsKey(name) && fileRefs.get(name).unlinked) res = null;
			else res = underlyingFileSystem.open(name, create);
			if(res == null) {fileRefsLock.release(); return null;}
			
			if(!fileRefs.containsKey(name)) fileRefs.put(name, new FileRefRecord());
			fileRefs.get(name).count++;
			
			fileRefsLock.release();
			return new UserOpenFile(res);
		}

		@Override
		public boolean remove(String name) {
			boolean res;
			fileRefsLock.acquire();
			if(fileRefs.containsKey(name))
			{
				fileRefs.get(name).unlinked = true;
				res = true;
			}
			else res = underlyingFileSystem.remove(name);
			fileRefsLock.release();
			return res;
		}
		
		private class UserOpenFile extends OpenFile
		{
			OpenFile underlyingFile;
			public UserOpenFile(OpenFile f) {super(UserFileSystem.this,f.getName()); underlyingFile = f;}
			@Override
			public int read(int pos, byte[] buf, int offset, int length) {
				return underlyingFile.read(pos, buf, offset, length);
			}
			@Override
			public int write(int pos, byte[] buf, int offset, int length) {
				return underlyingFile.write(pos, buf, offset, length);
			}
			@Override
			public int length() {
				return underlyingFile.length();
			}
			@Override
			public void close() {
				underlyingFile.close();
				String myName = getName();
				fileRefsLock.acquire();
				FileRefRecord rec = fileRefs.get(myName);
				if(--rec.count == 0)
				{
					if(rec.unlinked) underlyingFileSystem.remove(myName);
					fileRefs.remove(myName);
				}
				fileRefsLock.release();
			}
			@Override
			public void seek(int pos) {
				underlyingFile.seek(pos);
			}
			@Override
			public int tell() {
				return underlyingFile.tell();
			}
			@Override
			public int read(byte[] buf, int offset, int length) {
				return underlyingFile.read(buf, offset, length);
			}
			@Override
			public int write(byte[] buf, int offset, int length) {
				return underlyingFile.write(buf, offset, length);
			}
		}
	    
	    private class FileRefRecord
	    {
	    	public int count = 0;
	    	public boolean unlinked = false;
	    }
	    
	    public Map<String, FileRefRecord> fileRefs = new HashMap<String, FileRefRecord>();
	    public Lock fileRefsLock = new Lock();
    }
    
    public static UserFileSystem userFileSystem;

    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;

    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
}
