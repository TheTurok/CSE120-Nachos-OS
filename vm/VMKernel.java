package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.util.LinkedList;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);
    frameslock = new Lock();
    swaplock = new Lock();
    freeswaps = new LinkedList<Integer>();
    unpinnedpage = new Condition(new Lock());
    swapfile = ThreadedKernel.fileSystem.open(swapfilename, true);
    frames = new frame[Machine.processor().getNumPhysPages()];
    for( int i = 0; i < Machine.processor().getNumPhysPages(); i++)
      frames[i] = new frame();
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
	  swapfile.close();
    ThreadedKernel.fileSystem.remove(swapfilename);
	  super.terminate();
	}

  public static void swapIn(int swapOff, int pageNum)
  {
    int pointer = swapOff * (Machine.processor()).pageSize;
    int memPointer = pageNum * (Machine.processor()).pageSize;
    byte[] memByte = Machine.processor().getMemory();
    swapfile.read(pointer, memByte, memPointer, Processor.pageSize);
  }

  public static int swapOut(int pageNum, int swapNum)
  {

    byte[] memory = Machine.processor().getMemory();
  
    int memPointer = pageNum * Processor.pageSize;
    int pointer;
      swaplock.acquire();
System.out.println("before mem!!!!");
    if(swapNum == -1)
    {  
System.out.println("1");
      if(!freeswaps.isEmpty())
      {
System.out.println("2");

        pointer = freeswaps.removeFirst();
System.out.println("3");

        pointer = pointer * (Machine.processor()).pageSize;
System.out.println("4");

      }
      else
      {
System.out.println("5");

        pointer = swapcount * (Machine.processor()).pageSize;
System.out.println("6");
        swapcount++;
System.out.println("7");
      }

System.out.println("8");
    }
    else
{
      pointer = swapNum * Processor.pageSize;
System.out.println("9");
}
System.out.println("before write!!!!");
    swapfile.write(pointer, memory, memPointer, Processor.pageSize);
System.out.println("after write!!!!");
    swaplock.release();
    return pointer/(Machine.processor()).pageSize;
  }
  

  public static int clockRA()
  {
    frameslock.acquire();
    for( int i = 0; i < frames.length; i++)
    {
      if( !frames[i].ispinned )
      {
        areunpinned = true;
        break;
      }
    }
//    while( !areunpinned )
  //    unpinnedpage.sleep();
    int toevict = 0;
    while( frames[victim] != null )
    {
      if (frames[victim].entry.used != true && frames[victim].ispinned != true )
        break;
      else
        frames[victim].entry.used = false;
      victim = (victim + 1) % frames.length;
    }
    frameslock.release();
    toevict = victim;
    victim = (victim + 1) % frames.length;
    return toevict;
  }

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';

/*******************/
   public class frame {
    public VMProcess process = null;
    public int swappage = -1;
    public TranslationEntry entry = null;
    public boolean ispinned = false;
  }
   
//  public static frame[] frames = new frame[Machine.processor().getNumPhysPages()];
  public static frame[] frames;
  public static Lock frameslock;
  public static Condition unpinnedpage;
  private static Lock swaplock;
  private static OpenFile swapfile;
  public static LinkedList<Integer> freeswaps;
  private static final String swapfilename = "theSwap";
  private static int swapcount = 0;
  private static int victim = 0;
  public static boolean areunpinned = false;
/******************/
}
