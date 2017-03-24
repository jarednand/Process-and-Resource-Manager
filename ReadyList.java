package project1;

import java.util.*;

public class ReadyList {

	//Declare a ready list, which is an array of linked lists
	@SuppressWarnings("unchecked") private LinkedList<ProcessControlBlock>[] readyList = new LinkedList[3];

	//Create an array of size 3 and at each index, initialize a ready list
	public ReadyList(){
		for (int i = 0; i < 3; i++){
			readyList[i] = new LinkedList<ProcessControlBlock>();
		}
	}

	//Getter
	public LinkedList<ProcessControlBlock>[] getReadyList(){ 
		return readyList; 
	}

	//Stores a value at the end of a linked list given an priority (index)
	public void store(ProcessControlBlock processControlBlock){ 
		readyList[processControlBlock.getPriority()].addLast(processControlBlock); 
	}

	//Returns the length of a linked list associated with a given priortiy (index)
	public int getLengthAtPriority(int priority){ 
		return readyList[priority].size(); 
	}

	//Returns the length of the ready list
	public int getLength(){ 
		return readyList.length; 
	}

	//Returns linked list associated with a given priority (index)
	public LinkedList<ProcessControlBlock> getLinkedListAtPriority(int priority){
		return readyList[priority];
	}	

}
