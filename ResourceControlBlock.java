package project1;

import java.util.*;

public class ResourceControlBlock {

	//Declare values or attributes of a Resource Control Block
	private int rid;
	private int initialNumberOfFreeUnits;
	private int remainingNumberOfFreeUnits;
	private LinkedList<ProcessControlBlock> waitingList;

	//Custom Constructor
	public ResourceControlBlock(int rid){
		this.rid = rid;
		initialNumberOfFreeUnits = rid;
		remainingNumberOfFreeUnits = rid;
		waitingList = new LinkedList<ProcessControlBlock>();
	}

	//Setters
	public void setRid(int rid){ this.rid = rid; }
	public void setInitialNumberOfFreeUnits(int initialNumberOfFreeUnits){ this.initialNumberOfFreeUnits = initialNumberOfFreeUnits; }
	public void setRemainingNumberOfFreeUnits(int remainingNumberOfFreeUnits){ this.remainingNumberOfFreeUnits = remainingNumberOfFreeUnits; }

	//Getters
	public int getRid(){ return rid; }
	public int getInitialNumberOfFreeUnits(){ return initialNumberOfFreeUnits; }
	public int getRemainingNumberOfFreeUnits() { return remainingNumberOfFreeUnits; }
	public LinkedList<ProcessControlBlock> getWaitingList(){ return waitingList; }

	//Adds a Process Control Block to the waitingList
	public void addProcessControlBlock(ProcessControlBlock processControlBlock){
		waitingList.addLast(processControlBlock);
	}

	//Returns process control block location if it exists in waiting list
	public int getProcessControlBlockLocationFromWaitingList(String pid){
		for (int i = 0; i < waitingList.size(); i++){
			if (waitingList.get(i).getPid().equals(pid)){
				return i;
			}
		}
		return -1;
	}

	//Removes a Process Control Block from the waiting list given the process's location
	public void removeProcessControlBlock(int location){
		try {
			waitingList.remove(location);
		} catch (NoSuchElementException e){
			System.out.println("Error: Could not remove process from resource's waiting list.");
		}
	}

	//Prints out the process in the waiting list as well as the rid, initialNumberOfUnits and remianingNumberOfUnits
	public void printResourceControlBlock(){
		System.out.println("Resource: " + rid);
		System.out.println("Initial Number of Units: " + initialNumberOfFreeUnits);
		System.out.println("Remaining Number of Units: " + remainingNumberOfFreeUnits);
		String waitingListString = "Waiting List: [";
		for (int i = 0; i < waitingList.size(); i++){
			waitingListString += waitingList.get(i).getPid();
			if (i != waitingList.size() - 1){
				waitingListString += ", ";
			}
		}
		waitingListString += "]";
		System.out.println(waitingListString);
	}

}