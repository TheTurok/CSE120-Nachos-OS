package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
//    for( int i = 0; i < swappages.length; i++)
//      swappages[i] = -2;
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
		super.saveState();
/*    
    for( int i = 0; i < Machine.processor().getTLBSize(); i++)
    {
      if( (Machine.processor().readTLBEntry(i)).valid)
      {
        int vpn = (Machine.processor().readTLBEntry(i)).vpn;
        if( vpn >= 0 && vpn < pageTable.length )
        {
          pageTable[vpn] = Machine.processor().readTLBEntry(i);
          if(pageTable[vpn].valid)
            VMKernel.frames[pageTable[vpn].ppn].entry = pageTable[vpn];
          Machine.processor().writeTLBEntry( i, new TranslationEntry());
        }
      }
    }*/
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
	//	super.restoreState();
	}

	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
	protected boolean loadSections() {
//		return super.loadSections();
	UserKernel.memoryLock.acquire();
	pageTable = new TranslationEntry[numPages];
  swappages = new int[numPages];
	for (int vpn=0; vpn<numPages; vpn++) {
	    pageTable[vpn] = new TranslationEntry(vpn, -1,
						  false, false, false, false);
	}
	
    for( int i = 0; i < swappages.length; i++)
      swappages[i] = -2;

	UserKernel.memoryLock.release();

	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
System.out.println("\tinitializing " + section.getName()
                              + " section (" + section.getLength() + " pages)");
    
//	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
//		      + " section (" + section.getLength() + " pages)");

	    for (int i=0; i<section.getLength(); i++) {
		int vpn = section.getFirstVPN()+i;

		pageTable[vpn].readOnly = section.isReadOnly();
    pageTable[vpn].ppn = -s;

System.out.println("vpn:"+ vpn + " pagetablelength" + pageTable.length + "  swappages length  " + swappages.length);
    swappages[vpn] = -1;
	    }
	}
	
	return true;




	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		super.unloadSections();
    for( int i = 0; i < swappages.length; i++)
    {
      if (swappages[i] >= 0)
        VMKernel.freeswaps.add(swappages[i]);
    }
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
    case Processor.exceptionTLBMiss:            
       handleTLBMiss();            
       break;
		default:
			super.handleException(cause);
			break;
		}
	}

  private void handleTLBMiss() {

    int vaddr = Machine.processor().readRegister(Processor.regBadVAddr);
    int vpn = Processor.pageFromAddress(vaddr);
//System.out.println("--------------------miss for vpn: " + vpn);
 System.out.println("-----------------------Handling Miss for vpn: " + vpn);
    int tlbindex = -1;
//    UserKernel.memoryLock.acquire();
    if (vpn >= 0 && vpn < pageTable.length)
    {
System.out.println("1");

      for( int i = 0; i < Machine.processor().getTLBSize(); i++)
      {
System.out.println("21");

        if( (Machine.processor().readTLBEntry(i)).valid == false )
        {
System.out.println("3");

          tlbindex = i;
          break;
        }
      }
System.out.println("4");

      if( tlbindex == -1)
      {
System.out.println("5");

        do {
          tlbindex = Lib.random(Machine.processor().getTLBSize());
        } while ( tlbindex == lastreplace );
System.out.println("6");
        int evictedvpn = (Machine.processor().readTLBEntry(tlbindex)).vpn;
System.out.println("7");

        pageTable[evictedvpn] = Machine.processor().readTLBEntry(tlbindex);
System.out.println("8");

 //       VMKernel.frames[pageTable[evictedvpn].ppn].entry = pageTable[evictedvpn];
System.out.println("9");

        lastreplace = tlbindex;
      }
System.out.println("69");

        lastreplace = tlbindex;
    //if( pageTable[vpn] != null && pageTable[vpn].vpn >= 0)
 //   if( swappages[vpn] != -2)
 //   {
//      if( pageTable[vpn] == null || pageTable[vpn].valid == false)
        if( pageTable[vpn].valid == false)
        {
System.out.println("420");

           pagefaulthandler(vpn, tlbindex, pageTable[vpn]);
        }
        else
        {
System.out.println("ricardo is gay");

           Machine.processor().writeTLBEntry( tlbindex, pageTable[vpn]);
        }
 //   }System.out.println("1");

    for( int i = 0; i < Machine.processor().getTLBSize(); i++)
    {
        int pageindex = (Machine.processor().readTLBEntry(i)).vpn;
        if( pageindex >= 0 && pageindex < pageTable.length )
        {
          Machine.processor().writeTLBEntry( i, pageTable[pageindex]);

 System.out.println("----------------updatetlbafter: "+ pageindex + "valid bit " + Machine.processor().readTLBEntry(i).valid );
//          pageTable[vpn] = Machine.processor().readTLBEntry(i);
//          if(pageTable[pageindex].valid)
//            VMKernel.frames[pageTable[pageindex].ppn].entry = pageTable[pageindex];
//          Machine.processor().writeTLBEntry( i, new TranslationEntry());
        }
    }
       
    }
//    UserKernel.memoryLock.release();
  }

  private void pagefaulthandler(int vpn, int tlb, TranslationEntry entry) {
 System.out.println("               -----------------------Handling fault!!!!!");
    UserKernel.memoryLock.acquire();
   if( entry != null )
    {
//    UserKernel.memoryLock.acquire();
      int ppn;
      if( UserKernel.freePages.size() > 0 )
      {

 System.out.println("               --------------------freepages" + UserKernel.freePages.size());
        ppn = ((Integer)UserKernel.freePages.removeFirst()).intValue();
      }
      else
      {

        ppn = VMKernel.clockRA();
        int virt = (VMKernel.frames[ppn]).entry.vpn;
 System.out.println("               ----------------------clocking:" + ppn  + "vpn:" + virt);

        if( (VMKernel.frames[ppn]).entry.dirty )
        {
 System.out.println("                   -----------------------swappingout!!!!!");
          (VMKernel.frames[ppn]).process.swappages[virt] = VMKernel.swapOut(ppn, (VMKernel.frames[ppn]).swappage);
        }
          (VMKernel.frames[ppn]).process.pageTable[virt].valid = false;

 System.out.println("                   --evictedpage validity " +  (VMKernel.frames[ppn]).process.pageTable[virt].valid );
//        }
      }
      if(entry.readOnly && swappages[entry.vpn] != -2)
      {
 System.out.println("               ----------------------readonly load!!!!!");
//        pinVirtualPage(entry.vpn, false);
        int coffindex = -entry.ppn;
        CoffSection sect = coff.getSection(coffindex);
        int sectionpage = entry.vpn - sect.getFirstVPN();
        sect.loadPage(sectionpage, ppn);

      }
      else if(! entry.dirty && swappages[entry.vpn] != -2)
      {

 System.out.println("               ----------------------other load!!!!!");
 //       pinVirtualPage(entry.vpn, true);
        entry.dirty = true;
        int coffindex = -entry.ppn;
        CoffSection sect = coff.getSection(coffindex);
        int sectionpage = entry.vpn - sect.getFirstVPN();
        sect.loadPage(sectionpage, ppn);
      }
//      else*/
      else if (entry.dirty && swappages[entry.vpn] != -2)
      {
 System.out.println("                   -----------------------swappingin!!!!!");
        VMKernel.swapIn(swappages[vpn], ppn);
      } 
      VMKernel.frameslock.acquire();
      VMKernel.frames[ppn].entry = entry; 
      VMKernel.frames[ppn].entry.ppn = ppn;
      VMKernel.frames[ppn].entry.valid = true;
      VMKernel.frames[ppn].process = this;
      VMKernel.frames[ppn].ispinned = false;
      VMKernel.frameslock.release();
      pageTable[vpn] = VMKernel.frames[ppn].entry;
      Machine.processor().writeTLBEntry( tlb, pageTable[vpn]);
    }  
    UserKernel.memoryLock.release();
  }
    protected int pinVirtualPage(int vpn, boolean isUserWrite) {
 System.out.println("----------------pinning: "+ vpn);
	if (vpn < 0 || vpn >= pageTable.length)
	    return -1;

	TranslationEntry entry = pageTable[vpn];
	if (!entry.valid || entry.vpn != vpn)
	    return -1;

	if (isUserWrite) {
	    if (entry.readOnly)
      {
		    return -1;
      }
	    entry.dirty = true;
	}

	entry.used = true;
//  if( VMKernel.frames[entry.ppn] != null )
  (VMKernel.frames[entry.ppn]).ispinned = true;
//  VMKernel.frames[0].ispinned = true;

	return entry.ppn;
    }
    
    protected void unpinVirtualPage(int vpn) {
 System.out.println("----------------unpinning: "+ vpn);
      (VMKernel.frames[(pageTable[vpn]).ppn]).ispinned = false;
      VMKernel.areunpinned = true;
//      VMKernel.unpinnedpage.wake();
    }


	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';

/***********/
  private static int lastreplace = -1;
  private int[] swappages;
/**********/
}
