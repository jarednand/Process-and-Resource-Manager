package project1;

import java.util.NoSuchElementException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ProcessAndResourceManager {

	//Create a ready list, a variable which keeps track of current running process, and an array of resource control blocks
	private ReadyList readyList;
	private ProcessControlBlock runningProcess;
	private ResourceControlBlock[] resourceControlBlocks;
	private int foundError = 0;

	//Constructor
	public ProcessAndResourceManager(){
		readyList = new ReadyList();
		runningProcess = null;
		resourceControlBlocks = new ResourceControlBlock[4];
		for (int i = 0; i < 4; i++){
			resourceControlBlocks[i] = new ResourceControlBlock(i+1);
		}
	}

	//Starting with priority 2, then 1, then 0, checks if priority's linked list's size is > 0. If so, return first value of the linked list.
	public ProcessControlBlock findProcessWithHighestPriority(){
		for (int i = readyList.getLength() - 1; i >= 0; i--){
			if (readyList.getLengthAtPriority(i) > 0){
				return readyList.getLinkedListAtPriority(i).getFirst();
			}
		}
		return null; //should never happen
	}

	//Returns the index of a linked list given a priority and pid that you are looking for. Used to change value in ready list.
	public int findProcessLocation(int priority, String pid){
		for (int i = 0; i < readyList.getLengthAtPriority(priority); i++){
			if (readyList.getLinkedListAtPriority(priority).get(i).getPid().equals(pid)){
				return i;
			}
		}
		return -1; //should never happen
	}

	//Changes runningProcess status to ready and process with highest priority p's status to running. Then sets runningProcess to p.
	public void preempt(ProcessControlBlock p){
		if (runningProcess != null && !runningProcess.getStatus().equals("blocked")){
			int runningProcessLocation = findProcessLocation(runningProcess.getPriority(),runningProcess.getPid());
			readyList.getLinkedListAtPriority(runningProcess.getPriority()).get(runningProcessLocation).setStatus("ready");
		}
		int pProcessLocation = findProcessLocation(p.getPriority(),p.getPid());
		readyList.getLinkedListAtPriority(p.getPriority()).get(pProcessLocation).setStatus("running");
		runningProcess = p;
	}

	//Invokes premept function if running process is null, has a lower priority than process with highest priority, or if its status is not running
	public void scheduler(){
		ProcessControlBlock p = findProcessWithHighestPriority();
		if (runningProcess == null || runningProcess.getPriority() < p.getPriority() || !runningProcess.getStatus().equals("running")){
			preempt(p);
		}
	}

	//Creates a process. Note that the parent is always set to the running process (even when initally null) and the status is always set to ready.
	public void createProcess(String pid, int priority){
		if (processExists(pid)){
			foundError = 1;
		} else {
			ProcessControlBlock processControlBlock = new ProcessControlBlock(pid,runningProcess,"ready",priority);
			readyList.store(processControlBlock);
			if (runningProcess != null){
				runningProcess.addChild(processControlBlock);
			}
			scheduler();
		}
	}

	//Removes the currently running process from the Ready List
	public void removeRunningProcessFromReadyList(){
		int runningProcessLocation = findProcessLocation(runningProcess.getPriority(),runningProcess.getPid());
		try {
			readyList.getLinkedListAtPriority(runningProcess.getPriority()).remove(runningProcessLocation);
		} catch (NoSuchElementException e){
			foundError = 1;
		}
	}

	//Removes a process from the Ready List given its priority and pid
	public void removeProcessFromReadyList(int priority, String pid){
		int processLocation = findProcessLocation(priority,pid);
		try {
			readyList.getLinkedListAtPriority(priority).remove(processLocation);
		} catch (NoSuchElementException e){
			foundError = 1;
		}
	}

	//Timeout function. Removes current running process, stores at end of linked list, and then call scheduler
	public void timeout(){
		removeRunningProcessFromReadyList();
		runningProcess.setStatus("ready");
		readyList.getLinkedListAtPriority(runningProcess.getPriority()).addLast(runningProcess);
		scheduler();
	}

	//Returns a resource control block from array of resource control blocks given a rid
	public ResourceControlBlock getResourceControlBlock(int rid){
		return resourceControlBlocks[rid-1];
	}

	//Prints data corresponding to each Resource Control Block in resourceControlBlocks array
	public void printEachResourceControlBlock(){
		System.out.println("Resource Control Blocks: ");
		for (int i = 0; i < resourceControlBlocks.length; i++){
			String waitingList = "";
			for (int j = 0; j < resourceControlBlocks[i].getWaitingList().size(); j++){
				waitingList += resourceControlBlocks[i].getWaitingList().get(j).getPid();
				if (j != resourceControlBlocks[i].getWaitingList().size() - 1){
					waitingList += ", ";
				}
			}
			System.out.println("Resource: R" + (i+1) + ", Number of Remaining Free Units: " + resourceControlBlocks[i].getRemainingNumberOfFreeUnits() + ", Waiting List: [" + waitingList + "]");
		}
	}

	//Removes a resource from the running process's otherResources Linked List, given a rid
	public void removeResourceFromOtherResources(int rid, int units){
		try {
			for (int i = 0; i < runningProcess.getOtherResources().size(); i++){
				if (runningProcess.getOtherResources().get(i).getRid() == rid && runningProcess.getOtherResources().get(i).getRemainingNumberOfFreeUnits() == units){
					runningProcess.getOtherResources().remove(i);
					break;
				}
			}
		} catch (NoSuchElementException e){
			foundError = 1;
		}
	}

	/*
		Request a resource. If requested number of units is greater than number of units the resource can initially hold, then print an error.
		If resource has enough units, add resource to running process's otherResources Linked List. 
		Otherwise, remove the running process from the ready list and add it to the end of the Resource Control Block's 
		waitingList. Finally, call the scheduler to find a new running process.
	*/
	public void requestResource(int rid, int numberOfRequestedUnits){
		ResourceControlBlock resourceControlBlock = getResourceControlBlock(rid);
		if (runningProcess.hasResourceUnit(rid)){
			if (runningProcess.getResourceUnit(rid) + numberOfRequestedUnits > resourceControlBlock.getInitialNumberOfFreeUnits()){
				foundError = 1;
			}
		}
		if (foundError == 0){
			if (resourceControlBlock.getInitialNumberOfFreeUnits() < numberOfRequestedUnits){
				foundError = 1;
			}
			else if (resourceControlBlock.getRemainingNumberOfFreeUnits() >= numberOfRequestedUnits){
				int remainingNumberOfFreeUnits = resourceControlBlock.getRemainingNumberOfFreeUnits() - numberOfRequestedUnits;
				resourceControlBlocks[resourceControlBlock.getRid()-1].setRemainingNumberOfFreeUnits(remainingNumberOfFreeUnits);
				runningProcess.addResourceUnit(rid,numberOfRequestedUnits);
			} 
			else {
				removeRunningProcessFromReadyList();
				runningProcess.addResourceRequest(rid,numberOfRequestedUnits);
				runningProcess.setStatus("blocked");
				resourceControlBlocks[resourceControlBlock.getRid()-1].addProcessControlBlock(runningProcess);
			}
		}
		scheduler();
	}

	/*
		Releases a resource. If the number of units the running process is trying to release is greater than the initial number of available
		units or the number of units the running process is trying to release + the number of currently remaining units is greater than the
		initial number of available units, then an error message is displayed. Otherwise, remove the resource from the running process's 
		otherResources Linked List. Then check if the resource has any process control blocks in its waiting list. If it does, then pop the
		head of the list out of the list, set the status of that process control block to "ready", insert the resource control block into the
		popped process's otherResources linked list, and then insert the process control block into the ready list. Finally, call the scheduler.
	*/
	public void releaseResource(int rid, int numberOfReleaseUnits){
		if (!runningProcess.hasResourceUnit(rid) || numberOfReleaseUnits > resourceControlBlocks[rid-1].getInitialNumberOfFreeUnits() || (numberOfReleaseUnits + resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits()) > resourceControlBlocks[rid-1].getInitialNumberOfFreeUnits()){
			foundError = 1;
		} else {
			runningProcess.updateResourceUnit(rid,numberOfReleaseUnits);
			resourceControlBlocks[rid-1].setRemainingNumberOfFreeUnits(resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits() + numberOfReleaseUnits);
			int index = 0;
			if (resourceControlBlocks[rid-1].getWaitingList().size() > 0){
				while (resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits() >= 0 && index <= resourceControlBlocks[rid-1].getWaitingList().size()-1){
					ProcessControlBlock processControlBlock = resourceControlBlocks[rid-1].getWaitingList().get(index); 
					int resourceRequestUnits = processControlBlock.getResourceRequestUnits(rid);
					if (resourceRequestUnits <= resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits()){
						try {
							resourceControlBlocks[rid-1].getWaitingList().remove(index);
						} catch (NoSuchElementException e){
							foundError = 1;
						}
						processControlBlock.setStatus("ready");
						processControlBlock.setResourceRequestUnits(rid,0);
						resourceControlBlocks[rid-1].setRemainingNumberOfFreeUnits(resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits() - resourceRequestUnits);
						processControlBlock.addResourceUnit(rid,resourceRequestUnits);
						readyList.store(processControlBlock);
					} else {
						break;
					}
				}
			}
		}
		scheduler();
	}

	//Requests resource without calling scheduler. Used for the recursively releasing resources
	public void releaseResourceWithoutScheduler(int rid, int numberOfReleaseUnits, ProcessControlBlock p){
		if (numberOfReleaseUnits > resourceControlBlocks[rid-1].getInitialNumberOfFreeUnits()){
			foundError = 1;
		} else {
			p.updateResourceUnit(rid,numberOfReleaseUnits);
			resourceControlBlocks[rid-1].setRemainingNumberOfFreeUnits(resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits() + numberOfReleaseUnits);
			int index = 0;
			if (resourceControlBlocks[rid-1].getWaitingList().size() > 0){
				while (resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits() >= 0 && index <= resourceControlBlocks[rid-1].getWaitingList().size()-1){
					ProcessControlBlock processControlBlock = resourceControlBlocks[rid-1].getWaitingList().get(index); 
					int resourceRequestUnits = processControlBlock.getResourceRequestUnits(rid);
					if (resourceRequestUnits <= resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits()){
						try {
							resourceControlBlocks[rid-1].getWaitingList().remove(index);
						} catch (NoSuchElementException e){
							foundError = 1;
						}
						processControlBlock.setStatus("ready");
						processControlBlock.setResourceRequestUnits(rid,0);
						resourceControlBlocks[rid-1].setRemainingNumberOfFreeUnits(resourceControlBlocks[rid-1].getRemainingNumberOfFreeUnits() - resourceRequestUnits);
						processControlBlock.addResourceUnit(rid,resourceRequestUnits);
						readyList.store(processControlBlock);
					} else {
						break;
					}
				}
			}
		}
	}
	
	//Returns priority of process in the ready list
	public int getProcessPriority(String pid){
		for (int priority = 0; priority < readyList.getLength(); priority++){
			for (int pcb = 0; pcb < readyList.getLengthAtPriority(priority); pcb++){
				if (readyList.getLinkedListAtPriority(priority).get(pcb).getPid().equals(pid)){
					return priority;
				}
			}
		}
		return -1;
	}

	//Returns rid of resource which contains the process in it's waiting list
	public int getRidOfResourceWithBlockedProcess(String pid){
		for (int i = 0; i < resourceControlBlocks.length; i++){
			if (resourceControlBlocks[i].getProcessControlBlockLocationFromWaitingList(pid) != -1){
				return resourceControlBlocks[i].getRid();
			}
		}
		return -1;
	}

	//Recursively checks whether or not the running process is an ancestor of a given process
	public boolean runningProcessIsAncestor(ProcessControlBlock processControlBlock, String pid){
		if (processControlBlock.getPid().equals(pid))
			return true;
		boolean found = false;
		for (int i = 0; i < processControlBlock.getChildren().size(); i++){
			if (runningProcessIsAncestor(processControlBlock.getChildren().get(i),pid)){
				found = true;
			}
		}
		return found;
	}

	//Detects whether or not a process can be deleted
	public boolean isDeletable(String pid){
		if (pid.equals("init"))
			return false;
		if (runningProcessIsAncestor(runningProcess,pid))
			return true;
		return false;
	}

	//Removes a process from its parent Process Control Block and return the parent
	public ProcessControlBlock removeProcessFromParent(ProcessControlBlock processControlBlock){
		String pid = processControlBlock.getParent().getPid();
		int priority = getProcessPriority(pid);
		int rid = getRidOfResourceWithBlockedProcess(pid);
		ProcessControlBlock parent = null;
		if (priority != -1){
			readyList.getLinkedListAtPriority(priority).get(findProcessLocation(priority,pid)).removeChild(processControlBlock.getPid());
			parent = readyList.getLinkedListAtPriority(priority).get(findProcessLocation(priority,pid));
		} else if (rid != -1){
			int processControlBlockWaitingListLocation = resourceControlBlocks[rid-1].getProcessControlBlockLocationFromWaitingList(pid);
			resourceControlBlocks[rid-1].getWaitingList().get(processControlBlockWaitingListLocation).removeChild(processControlBlock.getPid());
			parent = resourceControlBlocks[rid-1].getWaitingList().get(processControlBlockWaitingListLocation);
		} else {
			foundError = 1;
		}
		return parent;
	} 

	//Gets Process Control Block from waiting list or ready list depending on where it is currently located
	public ProcessControlBlock getProcessControlBlock(String pid){
		int priority = getProcessPriority(pid);
		int rid = getRidOfResourceWithBlockedProcess(pid);
		ProcessControlBlock processControlBlock = null;
		if (priority != -1){
			processControlBlock = readyList.getLinkedListAtPriority(priority).get(findProcessLocation(priority,pid));
		} else if (rid != -1){
			int processControlBlockWaitingListLocation = resourceControlBlocks[rid-1].getProcessControlBlockLocationFromWaitingList(pid);
			processControlBlock = resourceControlBlocks[rid-1].getWaitingList().get(processControlBlockWaitingListLocation);
		} else {
			foundError = 1;
		}
		return processControlBlock;
	}

	//Signifies whether or not a process exists in the system (used for error checking)
	public boolean processExists(String pid){
		int priority = getProcessPriority(pid);
		int rid = getRidOfResourceWithBlockedProcess(pid);
		if (priority == -1 && rid == -1){
			return false;
		}
		return true;
	}

	//Frees all resources in the OtherResources list of a Process Control Block
	public void freeResources(ProcessControlBlock processControlBlock){
		for (Map.Entry<Integer,Integer> entry : processControlBlock.getResourceUnits().entrySet()){
			ResourceControlBlock resourceControlBlock = resourceControlBlocks[entry.getKey()-1];
			releaseResourceWithoutScheduler(entry.getKey(),entry.getValue(),processControlBlock);
		}
	}

	//Deletes the Process Control Block by removing it from the wait list or ready list and updating its parent
	public void deleteProcessControlBlockAndUpdate(ProcessControlBlock processControlBlock){
		String pid = processControlBlock.getPid();
		int priority = getProcessPriority(pid);
		int rid = getRidOfResourceWithBlockedProcess(pid);
		if (priority != -1){
			removeProcessFromReadyList(priority,pid);
		} else if (rid != -1){
			int processControlBlockWaitingListLocation = resourceControlBlocks[rid-1].getProcessControlBlockLocationFromWaitingList(pid);
			resourceControlBlocks[rid-1].removeProcessControlBlock(processControlBlockWaitingListLocation);
		}
	}

	//Kills the entire process tree given a starting process
	public void killTree(ProcessControlBlock processControlBlock){
		for (int i = 0; i < processControlBlock.getChildren().size(); i++){
			killTree(processControlBlock.getChildren().get(i));
		}
		freeResources(processControlBlock);
		deleteProcessControlBlockAndUpdate(processControlBlock);
	}

	//Destroys a process given its Process Control Block ID
	public void destroyProcess(String pid){
		//First, check whether or not the process is even deletable
		if (!isDeletable(pid)){
			foundError = 1;
		} else {
			if (pid.equals(runningProcess.getPid())){
				runningProcess = removeProcessFromParent(getProcessControlBlock(pid));
			}
			killTree(getProcessControlBlock(pid));
		}
		scheduler();
	}

	//Prints out the resources in resourceControlBlocks
	public void printResourceControlBlocks(){
		System.out.println("Resource Control Blocks: ");
		for (int i = 0; i < resourceControlBlocks.length; i++){
			resourceControlBlocks[i].printResourceControlBlock();
		}
	}

	//Restores the system back to its initial state by destroying all children of the init Process Control Block
	public void restoreSystem(){
		readyList = new ReadyList();
		runningProcess = null;
		resourceControlBlocks = new ResourceControlBlock[4];
		for (int i = 0; i < 4; i++){
			resourceControlBlocks[i] = new ResourceControlBlock(i+1);
		}
		createProcess("init",0);
	}

	//Determines rid given R1, R2, R3, or R4
	public int getRidFromName(String resourceName){
		int rid = 0;
		if (resourceName.equals("R1")){
			rid = 1;
		} else if (resourceName.equals("R2")){
			rid = 2;
		} else if (resourceName.equals("R3")){
			rid = 3;
		} else if (resourceName.equals("R4")){
			rid = 4;
		} else {
			foundError = 1;
		}
		return rid;
	}

	//Given a line, splits the line by spaces, and executes commands accordingly
	public void executeCommands(String line){
		String[] commands = line.split(" ");
		String command = commands[0];
		if (command.equals("init")){
			restoreSystem();
		} else if (command.equals("cr")){
			if (Integer.parseInt(commands[2])  <= 0 || Integer.parseInt(commands[2]) > 2){
				foundError = 1;
			} else {
				createProcess(commands[1],Integer.parseInt(commands[2]));
			}
		} else if (command.equals("de")){
			destroyProcess(commands[1]);
		} else if (command.equals("req")){
			if (!commands[1].equals("R1") && !commands[1].equals("R2") && !commands[1].equals("R3") && !commands[1].equals("R4")){
				foundError = 1;
			}
			else if (Integer.parseInt(commands[2]) == 0 || runningProcess.getPid().equals("init")){
				foundError = 1;
			} else {
				requestResource(getRidFromName(commands[1]),Integer.parseInt(commands[2]));
			}
		} else if (command.equals("rel")){
			if (!commands[1].equals("R1") && !commands[1].equals("R2") && !commands[1].equals("R3") && !commands[1].equals("R4")){
				foundError = 1;
			}
			else if (Integer.parseInt(commands[2]) == 0 || runningProcess.getPid().equals("init")){
				foundError = 1;
			} else {
				releaseResource(getRidFromName(commands[1]),Integer.parseInt(commands[2]));
			}
		} else if (command.equals("to")){
			timeout();
		} else {
			foundError = 1;
		}
	}

	//Appends to file
	public void appendToFile(String fileName, String content) throws FileNotFoundException, IOException{
		FileWriter out = null;
		try {
			out = new FileWriter(fileName,true);
			out.write(content);
		} catch (FileNotFoundException e){
			System.out.println(e.toString());
		} catch (IOException e){
			e.printStackTrace();
		} finally {
			if (out != null){
				try {
					out.close();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}

	public void executeReadAndWrite(String inputFileName, String outputFileName){
		restoreSystem();
		String path = "/Users/jarednand/Documents/UCI/Senior Year/Winter Quarter/CS 143B/Projects/project1/";
		String stringToAppend = "";
		try {
			Scanner file = new Scanner(new File(path + inputFileName));
			stringToAppend += runningProcess.getPid() + " ";
			while (file.hasNextLine()){
				String line = file.nextLine();
				if (line.length() != 0){
					executeCommands(line);
					if (foundError == 1){
						stringToAppend += "error";
						foundError = 0;
					} else {
						stringToAppend += runningProcess.getPid() + " ";
					}
				} else {
					stringToAppend += "\n";
				}
			}
			appendToFile(path + outputFileName,stringToAppend);
		} catch (FileNotFoundException e){
			System.out.println("Error: The file " + inputFileName + " was not found.");
		} catch (IOException e){
			System.out.println("Error: Could not append to output file " + outputFileName);
		}
	}

	//Runs the entire process and resource manager.
	public void run(){
		createProcess("init",0);
		Scanner s = new Scanner(System.in);
		while (true){
			System.out.print("$ ");
			String line = s.nextLine();
			String[] words = line.split(" ");
			if (line.equals("exit")){
				break;
			} else if (words.length == 3 && words[1].equals(">")){
				executeReadAndWrite(words[0],words[2]);
			} else {
				executeCommands(line);
				if (foundError == 1){
					System.out.println("error");
					foundError = 0;
				} else {
					System.out.println(runningProcess.getPid());
				}
			}
		}
		s.close();
	}

}