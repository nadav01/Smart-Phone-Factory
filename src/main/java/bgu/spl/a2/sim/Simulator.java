/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.conf.Order;
import bgu.spl.a2.sim.conf.Plan;
import bgu.spl.a2.sim.conf.Result;
import bgu.spl.a2.sim.conf.ToolJ;
import bgu.spl.a2.sim.tasks.ManufactorTask;
import bgu.spl.a2.sim.tools.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import com.google.gson.*;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	
	/**
	 * @param w - the warehouse of the simulator
	 */
	static Warehouse w = new Warehouse();
	
	/**
	 * @param pool - the pool of processors (threads) to run the simulator
	 */
	static WorkStealingThreadPool pool;
	
	/**
	 * @param l - counter in order to control the wave's order
	 */
	static CountDownLatch l = null;
	
	/**
	 * @param result - the object of the parsed Json file
	 */
	static Result result = null;
	
	
	/**
	* Begin the simulation
	* Should not be called before attachWorkStealingThreadPool()
	*/
    public static ConcurrentLinkedQueue<Product> start(){
    	
    	ConcurrentLinkedQueue<Product> answer = new ConcurrentLinkedQueue<>(); // the object to be returned
    		int numOfTasksInWave = 0;
    		List<List<Order>> waves = result.getWaves();
    		ConcurrentLinkedQueue<Deferred<Product>> deferredsByOrder = new ConcurrentLinkedQueue<>(); // will contain the deferred objects by order
    		
    		for(List<Order> wave : waves){
    			numOfTasksInWave = 0;
    			for(Order o : wave){
    				numOfTasksInWave += o.getQty();
    			}
    			
        		l = new CountDownLatch(numOfTasksInWave); // for assuring that the next wave will start only after the current is done
        		
    			for(Order o : wave){
    				
    				for(int k = 0; k<o.getQty(); k++){
    					ManufactorTask t = new ManufactorTask(o.getProduct(), w.getPlan(o.getProduct()), w, o.getStartId()+k);
    					deferredsByOrder.add(t.getResult());
    					pool.submit(t);
            			t.getResult().whenResolved(()->{
            				l.countDown();
            			});
    					
    				}
    				
    			}
    			
    			
        		/*
        		 * wait for the wave to complete before starting the next one
        		 * */
            	try {
        			l.await();
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
    			
    		}
    		
    	try {
			pool.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	for(Deferred<Product> d : deferredsByOrder){ // adds the final product to the queue in the order of ordering
    		answer.add(d.get());
    	}
    	
    	return answer;
    	
    }
	
    
    
    
    
	/**
	* attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
	* @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
	*/
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool){
		pool = myWorkStealingThreadPool;
		pool.start();
	}
	
	public static void main(String [] args){
		
	String location = "";
	for(String s : args){
		location += s;
	}
	
	Gson gson = new Gson();
	BufferedReader br= null;;
	try {
		br = new BufferedReader(new FileReader(location));
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	}
	
	result = gson.fromJson(br, Result.class);
	
	// ### setting the pool ###
	attachWorkStealingThreadPool(new WorkStealingThreadPool(result.getThreads()));
	
	// ### sending tools to warehouse ###
	List<ToolJ> tools = result.getTools();
	for(ToolJ t : tools){
		switch(t.getTool()){
		case "gs-driver" : w.addTool(new GCDScrewdriver(), t.getQty()); break;
		case "np-hammer" : w.addTool(new NextPrimeHammer(), t.getQty()); break;
		case "rs-pliers" : w.addTool(new RandomSumPliers(), t.getQty()); break;
		}
	}
	
	// ### sending plans to warehouse ###
	List<Plan> plans = result.getPlans();
	for(Plan p : plans){
		
		String[] parts = new String[p.getParts().size()];
		for(int i = 0; i<parts.length; i++){
			parts[i] = p.getParts().get(i);
		}
		
		String[] toolsp = new String[p.getTools().size()];
		for(int i = 0; i<toolsp.length; i++){
			toolsp[i] = p.getTools().get(i);
		}
		
		w.addPlan(new ManufactoringPlan(p.getProduct(), parts, toolsp));
	}
	
	
	// -----------------------------------------------
	
	try {
	Simulator SimulatorImpl = new Simulator();
	ConcurrentLinkedQueue<Product> SimulationResult;
	SimulationResult = SimulatorImpl.start();
	FileOutputStream fout = new FileOutputStream("result.ser");
	ObjectOutputStream oos = new ObjectOutputStream(fout);
	oos.writeObject(SimulationResult);
	oos.close();
	} catch (Exception e) {e.printStackTrace();}
	
	// -----------------------------------------------
 
	}
	
}
