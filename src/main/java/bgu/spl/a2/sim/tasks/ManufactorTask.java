package bgu.spl.a2.sim.tasks;
import bgu.spl.a2.sim.*;
import bgu.spl.a2.*;
import bgu.spl.a2.sim.conf.*;
import bgu.spl.a2.sim.tools.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ManufactorTask extends Task<Product> {
	
	/**
	 * @param ans - the final product (then it's ready)
	 */
	private Product ans;
	
	/**
	 * @param plan - the plan to manufacture the requested product
	 */
	private ManufactoringPlan plan;
	
	/**
	 * @param ware - a reference to the warehouse
	 */
	private Warehouse ware;
	
	/**
	 * @param subtasks - a list of subtasks (the parts needed to be manufacture)
	 */
	private ArrayList<ManufactorTask> subtasks = new ArrayList<>();
	
	
	
	protected void start(){
		
        CountDownLatch l = new CountDownLatch(plan.getTools().length);
		
		for(String p : plan.getParts()){
			subtasks.add(new ManufactorTask(p, ware.getPlan(p), ware, ans.getStartId()+1));
		}
		
		if(subtasks.size()>0){ // if we need other parts to build this product
			for(ManufactorTask t : subtasks){
				spawn(t);
				}
		
		whenResolved(subtasks , ()-> { // when the subtasks are completed
			for(ManufactorTask t : subtasks){
				ans.addPart(t.getResult().get());
			}
			
			for(String t : plan.getTools()){				
				
				Deferred<Tool> tool = ware.acquireTool(t);

				tool.whenResolved(()->{ // when the tool is available in the warehouse
					ans.setFinalId(tool.get().useOn(ans)); // Adds the useOn result to the ans finalID
					ware.releaseTool(tool.get());
					l.countDown();
				});
				
			}
			
			try {
				l.await();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			complete(ans);
			
		});
		

		}
		
		else{ // if we does not need other parts to build this product, completes this product immediatly
			complete(ans);
		}
		
	}
	
	/**
	 * this function is called by the ToolChecker when all the tools "useOn" on all the parts
	 */
	/*package*/ void complete(){
		complete(ans);
	}
	
	
	/**
	 * Constructor
	 * @param name - the requested product name
	 * @param plan - the plan to manufacture the product
	 * @param ware - a reference for the warehouse
	 * @param id - the product StartId
	 */
	public ManufactorTask(String name, ManufactoringPlan plan, Warehouse ware, long id){
		ans = new Product(id, name);
		this.plan = plan;
		this.ware = ware;
	}

}