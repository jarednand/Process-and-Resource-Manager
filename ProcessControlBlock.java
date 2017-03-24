package project1;

import java.util.*;

public class ProcessControlBlock {

	//Declare values or attributes of a Process Control Block
	private String pid;
	private ProcessControlBlock parent;
	private LinkedList<ProcessControlBlock> children;
	private String status;
	private int priority;
	private LinkedList<ResourceControlBlock> otherResources;
	private HashMap<Integer,Integer> resourceRequests; //used to save number of requested units if process gets blocked
	private HashMap<Integer,Integer> resourceUnits; //used to keep track of how many units of a resource a process is holding on to

	//Custom constructor
	public ProcessControlBlock(String pid, ProcessControlBlock parent, String status, int priority){
		this.pid = pid;
		this.parent = parent;
		children = new LinkedList<ProcessControlBlock>();
		this.status = status;
		this.priority = priority;
		otherResources = new LinkedList<ResourceControlBlock>();
		resourceRequests = new HashMap<Integer,Integer>();
		resourceUnits = new HashMap<Integer,Integer>();
	}

	//Setters
	public void setPid(String pid){ this.pid = pid; }
	public void setParent(ProcessControlBlock parent){ this.parent = parent; }
	public void setStatus(String status){ this.status = status; }
	public void setPriority(int priority){ this.priority = priority; }

	//Getters
	public String getPid(){ return pid; }
	public ProcessControlBlock getParent(){ return parent; }
	public LinkedList<ProcessControlBlock> getChildren(){ return children; }
	public String getStatus(){ return status; }
	public int getPriority(){ return priority; }
	public LinkedList<ResourceControlBlock> getOtherResources(){ return otherResources; }
	public HashMap<Integer,Integer> getResourceRequests(){ return resourceRequests; }
	public HashMap<Integer,Integer> getResourceUnits(){ return resourceUnits; }

	//Adds a child Process Control Block to the end of the children Linked List
	public void addChild(ProcessControlBlock child){ children.addLast(child); }

	//Adds a Resource Control Block to the end of the otherResources Linked List
	public void addResourceControlBlock(ResourceControlBlock resourceControlBlock){
		otherResources.addLast(resourceControlBlock);
	}

	//Removes a child given a pid
	public void removeChild(String pid){
		int index = -1;
		for (int i = 0; i < children.size(); i++){
			if (children.get(i).getPid().equals(pid)){
				index = i;
				break;
			}
		}
		if (index != -1)
			children.remove(index);
	}

	//Adds a resource request when a process gets blocked
	public void addResourceRequest(int rid, int numberOfRequestedResourceUnits){
		if (resourceRequests.containsKey(rid)){
			resourceRequests.put(rid,resourceRequests.get(rid) + numberOfRequestedResourceUnits);
		} else {
			resourceRequests.put(rid,numberOfRequestedResourceUnits);
		}
	}

    //Stores a resource request
	public void setResourceRequestUnits(int rid, int units){
		resourceRequests.put(rid,units);
	}

	//Gets resource request units
	public int getResourceRequestUnits(int rid){
		return resourceRequests.get(rid);
	}

	//Adds a resource units to resource units whenever a process successfully gets resources from a resource
	public void addResourceUnit(int rid, int numberOfResourceUnits){
		if (resourceUnits.containsKey(rid)){
			resourceUnits.put(rid,resourceUnits.get(rid) + numberOfResourceUnits);
		} else {
			resourceUnits.put(rid,numberOfResourceUnits);
		}
	}

	//Update a resource unit
	public void updateResourceUnit(int rid, int numberOfResourceUnits){
		if (resourceUnits.containsKey(rid)){
			resourceUnits.put(rid,resourceUnits.get(rid) - numberOfResourceUnits);
		}
	}

	//Gets number of units of a resource the process is holding on to
	public int getResourceUnit(int rid){
		return resourceUnits.get(rid);
	}

	//Checks to see if resource unit event exists
	public boolean hasResourceUnit(int rid){
		return resourceUnits.containsKey(rid);
	}

	//Checks whether or not a given resource is in the resources list
	public boolean hasResource(int rid){
		for (int i = 0; i < otherResources.size(); i++){
			if (otherResources.get(i).getRid() == rid){
				return true;
			}
		}
		return false;
	}

}
