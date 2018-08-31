import simulator.Config;
import simulator.IODevice;
import simulator.Kernel;
import simulator.ProcessControlBlock;
import simulator.CPU;
//
import java.io.FileNotFoundException;
import java.io.IOException;
//
import java.util.ArrayDeque;
import java.util.Deque;

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
public class FCFSKernel implements Kernel {
    

    private Deque<ProcessControlBlock> readyQueue;
        
    public FCFSKernel() {
		// Set up the ready queue.
		readyQueue = new ArrayDeque<ProcessControlBlock>();
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
		    ProcessControlBlock previous_PCB = current_CPU.contextSwitch(current_PCB);
		    current_PCB.setState(ProcessControlBlock.State.RUNNING);
		    
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
					
					// Call dispatch().
					dispatch();
                }
                break;
             case TERMINATE_PROCESS:
                {
					// Process on the CPU has terminated.
					// Get PCB from CPU.
					ProcessControlBlock current_PCB = current_CPU.getCurrentProcess();
					
					// Set status to TERMINATED.
					current_PCB.setState(ProcessControlBlock.State.TERMINATED);
					
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
        switch (interruptType) {
            case TIME_OUT:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not suppor timeouts.");
            case WAKE_UP:
				// IODevice has finished an IO request for a process.
				// Retrieve the PCB of the process (varargs[1]),
				ProcessControlBlock current_PCB = (ProcessControlBlock)varargs[1];
				//, set its state to READY,
				current_PCB.setState(ProcessControlBlock.State.READY);
				//put it on the end of the ready queue.
				readyQueue.add(current_PCB);
				
				// If CPU is idle then dispatch().
				//*first make CPU
				CPU current_CPU = Config.getCPU();
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
