//PDYSHA009
import simulator.ProcessControlBlock;
import simulator.ProcessControlBlock.State;
import simulator.Instruction;
import simulator.CPUInstruction;
import simulator.IOInstruction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class ProcessControlBlockImpl implements ProcessControlBlock
{

    private State state;
    private int processID;
    private int priority_Value;
    private String programName;
    private ArrayList<Instruction> instruction_AList;
    private static int pCounter = 1;
    
    public ProcessControlBlockImpl()
    {
        instruction_AList = new ArrayList<Instruction>();
        processID = pCounter++;
    }
    
    //get processID
    public int getPID()
    {
        return processID;
    }
    
    //get program name
    public String getProgramName()
    {
        return programName;
    }
    
    //get process priority value
    public int getPriority()
    {
        return priority_Value;
    }
    
    //set priority value
    public int setPriority(int inValue)
    {
        int prev = getPriority();
        priority_Value = inValue;
        return prev;
    }
    
    //get current program instruction
    public Instruction getInstruction()
    {
        return instruction_AList.get(0);
    }
    
    //check if there are any more instructions
    public boolean hasNextInstruction()
    {
        if(instruction_AList.size()>1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    //move to next instruction
    public void nextInstruction()
    {
        if(hasNextInstruction() == true)
        {
            instruction_AList.remove(0);
        }
    }
    
    //get process state
    public State getState()
    {
        return state;
    }
    
    //set process state
    public void setState(State inState)
    {
        if( getState()!=State.TERMINATED)
        {
            this.state = inState;
        }
    }
    
    //return toString in specific format
    public String toString()
    {
        String tempStr;
        tempStr = String.format("process(pid=%d, state=%s, name=\"%s\")",processID , state, programName);
        
        return tempStr;
    }
    
    // load the program into the ArrayList instructions_AList
    public static ProcessControlBlock loadProgram(String input_File)
    {
        ProcessControlBlockImpl current_PCB = new ProcessControlBlockImpl();
        
        //set the name
        current_PCB.programName = input_File;
        //set the state
        current_PCB.state = ProcessControlBlock.State.READY;
        
        try
        {
            Scanner fileScanner = new Scanner(new FileReader(input_File));
            
            while(fileScanner.hasNextLine() == true)
            {
                String current_Line = fileScanner.nextLine();
                if(!(current_Line.charAt(0) == '#'))
                {
                    Scanner lineScanner = new Scanner(current_Line);
                    String begin = lineScanner.next();
                    if(begin.equals("IO"))
                    {
                        int burstTime;
                        int ID;
                        if(lineScanner.hasNextInt() == true)
                        {
                            burstTime = lineScanner.nextInt();
                        }
                        else
                        {
                            System.out.println("Returning null");
                            return null;
                        }
                        
                        if(lineScanner.hasNextInt() == true)
                        {
                            ID = lineScanner.nextInt();
                        }
                        else
                        {
                            System.out.println("Returning null");
                            return null;
                        }
                        
                        IOInstruction ioInstruction = new IOInstruction(burstTime, ID);
                        //add the instruction the the ArrayList
                        current_PCB.instruction_AList.add(ioInstruction);
                    }
                    else if(begin.equals("CPU")==true)
                    {
                        if(lineScanner.hasNextInt()==true)
                        {
                            CPUInstruction temp_CPUInstruction = new CPUInstruction(lineScanner.nextInt());
                            //add to the ArrayList a CPU
                            current_PCB.instruction_AList.add(temp_CPUInstruction);
                        }
                        else
                        {
                            System.out.println("Returning null");
                            return null;
                        }
                    }
                    else//if it must exit
                    {
                        fileScanner.close();
                        return null;
                    }
                }
            }
            fileScanner.close();
        }
        catch (FileNotFoundException fileExp)
        {
            return null;
        }
        return current_PCB;
        
        
    }
    
    
}
