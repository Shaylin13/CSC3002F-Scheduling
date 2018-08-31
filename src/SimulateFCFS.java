//SimulateFCFS
//PDYSHA009
import java.util.Scanner;
import simulator.Config;
import simulator.SystemTimer;
import simulator.TRACE;

public class SimulateFCFS
{
    public static void main(String[] args)
    {
        Scanner inputScanner = new Scanner(System.in);
        
        System.out.print("*** FCFS Simulator ***\n");
        System.out.print("Enter configuration file name: ");
        String fileName;
        fileName = inputScanner.nextLine();
        
        System.out.print("Enter cost of system call: ");
        int systemCallCost;
        systemCallCost = inputScanner.nextInt();
        
        System.out.print("Enter cost of context switch: ");
        int contextSwitchCost;
        contextSwitchCost = inputScanner.nextInt();
        
        System.out.print("Enter trace level: ");
        int traceLevel;
        traceLevel = inputScanner.nextInt();
        
        //Jy weet mos
        TRACE.SET_TRACE_LEVEL(traceLevel);
        
        final FCFSKernel FCFS_kernel = new FCFSKernel();
        Config.init(FCFS_kernel, contextSwitchCost, systemCallCost);
        Config.buildConfiguration(fileName);
        Config.run();
        
        SystemTimer sysTimer = Config.getSystemTimer();
        
        System.out.println(sysTimer);
        System.out.println("Context switches: "+Config.getCPU().getContextSwitches());
        System.out.printf("CPU utilization: %.2f\n", ((double)sysTimer.getUserTime())/sysTimer.getSystemTime()*100);
        
    }
}


