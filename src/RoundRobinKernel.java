//Round Robin Kernel
import simulator.Config;
import simulator.IODevice;
import simulator.Kernel;
import simulator.ProcessControlBlock;
import simulator.CPU;
import simulator.SystemTimer;
import simulator.InterruptHandler.*;
import simulator.SystemCall.*;
//
import java.io.FileNotFoundException;
import java.io.IOException;
//
import java.util.ArrayDeque;
import java.util.Deque;
import java.io.FileReader;
import java.util.Scanner;


/**
 * Concrete Kernel type
 * 
 * @author Stephan Jamieson
 * 
  edited by:
 *@author Shaylin Padayachee
 *@version 05/05/18
   
 */
 //games
 //loadobject from file x2
 //identify max coordinates of first one and set offset for there
 
public class RoundRobinKernel implements Kernel {
    
    private static int time;
    private Deque<ProcessControlBlock> readyQueue;
        
    public RoundRobinKernel(int inTime) {
		// Set up the ready queue.
		readyQueue = new ArrayDeque<ProcessControlBlock>();
		time = inTime;
    }
    
    private ProcessControlBlock dispatch() {
		// Perform context switch, swapping process
		// currently on CPU with one at front of ready queue.
		// If ready queue empty then CPU goes idle ( holds a null value).
		// Returns process removed from CPU.
		
		CPU current_CPU = Config.getCPU();
		
		//*this will hold whether the readyQueue is empty or not
		boolean isQueue_Empty = readyQueue.isEmpty();
		
		if(isQueue_Empty == true)
		//*if the queue is empty return null context.
		{
		    return current_CPU.contextSwitch(null);
		}
		else
		{
		    ProcessControlBlock current_PCB = readyQueue.pop();
		    
		    boolean isCPU_Idle = current_CPU.isIdle();
		    //boolean isPID_NotSame = current_PCB.getPID()!=current_CPU.getCurrentProcess().getPID();
		    
		    if((isCPU_Idle == false) && (current_PCB.getPID()!=current_CPU.getCurrentProcess().getPID()))
		    {
		        if(current_CPU.getCurrentProcess().getState() == ProcessControlBlock.State.RUNNING)
		        {
		            current_CPU.getCurrentProcess().setState(ProcessControlBlock.State.READY);
		        }
		        //
		        if(current_PCB.getState() == ProcessControlBlock.State.RUNNING)
		        {
		            current_PCB.setState(ProcessControlBlock.State.READY);
		        }
		    }
		    //same as FCFS
		    ProcessControlBlock previous_PCB = current_CPU.contextSwitch(current_PCB);
		    current_PCB.setState(ProcessControlBlock.State.RUNNING);
		    
		    SystemTimer sysTimer = Config.getSystemTimer();
		    sysTimer.scheduleInterrupt(time, this, current_PCB.getPID());
		    
		    return previous_PCB;
		}
	}
            
    
                
    public int syscall(int number, Object... varargs) {
        int result = 0;
        
        //*set a current CPU object
        CPU current_CPU = Config.getCPU();
        
        switch (number) {
             case MAKE_DEVICE:
                {
                    IODevice device = new IODevice((Integer)varargs[0], (String)varargs[1]);
                    Config.addDevice(device);
                }
                break;
             case EXECVE: 
                {
                    ProcessControlBlock pcb = this.loadProgram((String)varargs[0]);
                    if (pcb!=null) {
                        // Loaded successfully.
						// Now add to end of ready queue.
						readyQueue.add(pcb);
						// If CPU idle then call dispatch.
						boolean isCPU_Idle = current_CPU.isIdle();
						if(isCPU_Idle == true)
						{
						    //*dispatching if CPU is Idle
						    dispatch();
						    pcb.setState(ProcessControlBlock.State.RUNNING);
						}
                    }
                    else {
                        result = -1;
                    }
                }
                break;
             case IO_REQUEST: 
                {
					// IO request has come from process currently on the CPU.
					// Get PCB from CPU.
					ProcessControlBlock current_PCB = current_CPU.getCurrentProcess();
					
					// Find IODevice with given ID: Config.getDevice((Integer)varargs[0]);
					IODevice current_Device = Config.getDevice((Integer)varargs[0]);
					
					// Make IO request on device providing burst time (varages[1]),
					// the PCB of the requesting process, and a reference to this kernel (so // that the IODevice can call interrupt() when the request is completed.
					current_Device.requestIO((Integer)(varargs[1]), current_PCB , this);
					
					// Set the PCB state of the requesting process to WAITING.
					current_PCB.setState(ProcessControlBlock.State.WAITING);
					
					//required for RR
					SystemTimer sysTimer = Config.getSystemTimer();
					sysTimer.cancelInterrupt(current_PCB.getPID());
					
					// Call dispatch().
					this.dispatch();
                }
                break;
             case TERMINATE_PROCESS:
                {
					// Process on the CPU has terminated.
					// Get PCB from CPU.
					ProcessControlBlock current_PCB = current_CPU.getCurrentProcess();
					
					// Set status to TERMINATED more needed in RR.
					current_PCB.setState(ProcessControlBlock.State.TERMINATED);
					SystemTimer sysTimer = Config.getSystemTimer();
					sysTimer.cancelInterrupt(current_PCB.getPID());
					
                    // Call dispatch().
                    dispatch();
                }
                break;
             default:
                result = -1;
        }
        return result;
    }
   
    
    public void interrupt(int interruptType, Object... varargs){
    
    //needed to handle more info
    CPU current_CPU = Config.getCPU();
    
        switch (interruptType) {
            case TIME_OUT:
                //not FCFS so not needed
                //throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not suppor timeouts.");
                
                ProcessControlBlock current_PCB = current_CPU.getCurrentProcess();
                readyQueue.add(current_PCB);
                this.dispatch();
                current_PCB.setState(ProcessControlBlock.State.RUNNING);
                
                break;
                
            case WAKE_UP:
				// IODevice has finished an IO request for a process.
				// Retrieve the PCB of the process (varargs[1]),
				ProcessControlBlock wake_PCB = (ProcessControlBlock)varargs[1];
				//, set its state to READY,
				wake_PCB.setState(ProcessControlBlock.State.READY);
				//put it on the end of the ready queue.
				readyQueue.add(wake_PCB);
				
				// If CPU is idle then dispatch().
				//*first make CPU
				//CPU current_CPU = Config.getCPU();
				//*now check if its idle
				boolean isCPU_Idle = current_CPU.isIdle();
				if(isCPU_Idle == true)
				{
				    dispatch();
				}
				
                break;
            default:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): unknown type.");
        }
    }
    
    private static ProcessControlBlock loadProgram(String filename) {
        /*try {*/
            return ProcessControlBlockImpl.loadProgram(filename);
       /* }
        catch (FileNotFoundException fileExp) {
            return null;
        }
        catch (IOException ioExp) {
            return null;
        }*/
    }
}
