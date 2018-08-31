//SimulateRR based off SimulateFCFS with minor changes
//PDYSHA009
import java.util.Scanner;
import simulator.Config;
import simulator.SystemTimer;
import simulator.TRACE;

public class SimulateRR
{
    public static void main(String[] args)
    {
        Scanner inputScanner = new Scanner(System.in);
        
        System.out.print("*** RR Simulator ***\n");
        System.out.print("Enter configuration file name: ");
        String fileName;
        fileName = inputScanner.nextLine();

        System.out.print("Enter slice time: ");
        int sliceTime;
        sliceTime = inputScanner.nextInt();
        
        System.out.print("Enter cost of system call: ");
        int systemCallCost;
        systemCallCost = inputScanner.nextInt();
        
        System.out.print("Enter cost of context switch: ");
        int contextSwitchCost;
        contextSwitchCost = inputScanner.nextInt();
        
        System.out.print("Enter trace level: ");
        int traceLevel;
        traceLevel = inputScanner.nextInt();
        
        //Done Taking in-----------------------------------------------
        TRACE.SET_TRACE_LEVEL(traceLevel);
        
        final RoundRobinKernel RR_kernel = new RoundRobinKernel(sliceTime);
        Config.init(RR_kernel, contextSwitchCost, systemCallCost);
        Config.buildConfiguration(fileName);
        Config.run();
        
        SystemTimer sysTimer = Config.getSystemTimer();
        
        System.out.println(sysTimer);
        System.out.println("Context switches: "+Config.getCPU().getContextSwitches());
        System.out.printf("CPU utilization: %.2f\n", ((double)sysTimer.getUserTime())/sysTimer.getSystemTime()*100);
        
    }
}


