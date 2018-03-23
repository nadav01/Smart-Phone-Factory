package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.GCDScrewdriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.Deferred;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {
	
	/**
	 * @param GCDs - available "gs-driver" tools in the warehouse
	 */
	private ConcurrentLinkedDeque<Tool> GCDs = new ConcurrentLinkedDeque<>();
	
	/**
	 * @param NPHs - available "np-hammer" tools in the warehouse
	 */
	private ConcurrentLinkedDeque<Tool> NPHs = new ConcurrentLinkedDeque<>();
	
	/**
	 * @param RSPs - available "rs-pliers" tools in the warehouse
	 */
	private ConcurrentLinkedDeque<Tool> RSPs = new ConcurrentLinkedDeque<>();
	
	/**
	 * @param waitingForGCD - queue of deferred waiting to be resolved with a "gs-driver"
	 */
	private ConcurrentLinkedDeque<Deferred<Tool>> waitingForGCD = new ConcurrentLinkedDeque<>();
	
	/**
	 *  @param waitingForNPH - queue of deferred waiting to be resolved with a "np-hammer"
	 */
	private ConcurrentLinkedDeque<Deferred<Tool>> waitingForNPH = new ConcurrentLinkedDeque<>();
	
	/**
	 *  @param waitingForRSP - queue of deferred waiting to be resolved with a "rs-pliers"
	 */
	private ConcurrentLinkedDeque<Deferred<Tool>> waitingForRSP = new ConcurrentLinkedDeque<>();
	
	/**
	 * @param plans - the manufactoring plans of products in the warehouse
	 */
	private Vector<ManufactoringPlan> plans = new Vector<>();
	
	
	/**
	* Constructor
	*/
    public Warehouse(){}

	/**
	* Tool acquisition procedure
	* Note that this procedure is non-blocking and should return immediatly
	* @param type - string describing the required tool
	* @return a deferred promise for the  requested tool
	*/
    public Deferred<Tool> acquireTool(String type){
    	Tool t = null;
    	Deferred<Tool> ans = new Deferred<>();
    	
    	// trying to grab a tool
    	switch(type){
    		case "gs-driver": t = GCDs.poll(); break;
    		case "np-hammer": t = NPHs.poll(); break;
    		case "rs-pliers": t = RSPs.poll(); break;
    	}
    	
    	// if there isn't a tool available at this moment, wait for it
    	if(t == null){
    		switch(type){
        	case "gs-driver": waitingForGCD.add(ans); break;
        	case "np-hammer": waitingForNPH.add(ans); break;
        	case "rs-pliers": waitingForRSP.add(ans); break;
        	}
    		return ans;
    	}
    	
    	// if there is a tool available at the moment, resolved the deferred with it
    	else{    		
    		ans.resolve(t);
    		return ans;
    	}
    }

	/**
	* Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	* @param tool - The tool to be returned
	*/
    public void releaseTool(Tool tool){
    	String name = tool.getType();
    	Deferred<Tool> d = null;
    	
    	// ask if anyone waits for this tool to be released
    	switch(name){
			case "gs-driver": d = waitingForGCD.poll(); break;
			case "np-hammer": d = waitingForNPH.poll(); break;
			case "rs-pliers": d = waitingForRSP.poll(); break;
    	}
    	
    	// if there is someone waiting, resolved their deferred with the tool
    	if(d!=null){
    		d.resolve(tool);
    	}
    	
    	// if nobody waits for this tool to be released, add it to the inventory
    	else{
    		switch(name){
			case "gs-driver": GCDs.add(tool); break;
			case "np-hammer": NPHs.add(tool); break;
			case "rs-pliers": RSPs.add(tool); break;
    		}
    	}
    }

	
	/**
	* Getter for ManufactoringPlans
	* @param product - a string with the product name for which a ManufactoringPlan is desired
	* @return A ManufactoringPlan for product
	*/
    public ManufactoringPlan getPlan(String product){
    	ManufactoringPlan ans = null;
    	for(ManufactoringPlan p : plans){
    		if(p.getProductName().equals(product)){
    			ans = p;
    			return ans;
    		}
    	}    	
    	return ans;
    }
	
	/**
	* Store a ManufactoringPlan in the warehouse for later retrieval
	* @param plan - a ManufactoringPlan to be stored
	*/
    public void addPlan(ManufactoringPlan plan){
    	plans.add(plan);
    }
    
	/**
	* Store a qty Amount of tools of type tool in the warehouse for later retrieval
	* @param tool - type of tool to be stored
	* @param qty - amount of tools of type tool to be stored
	*/
    public void addTool(Tool tool, int qty){

    	if(tool.getType().equals("gs-driver")){
    		for(int i = 0; i<qty; i++){
    			GCDs.add(new GCDScrewdriver());
    		}
    	}
    	
    	if(tool.getType().equals("np-hammer")){
    		for(int i = 0; i<qty; i++){
    			NPHs.add(new NextPrimeHammer());
    		} 		
    	}
    	
    	if(tool.getType().equals("rs-pliers")){
    		for(int i = 0; i<qty; i++){
    			RSPs.add(new RandomSumPliers());
    		}
    	}
    	
    }

}
