package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;
import java.util.Random;

public class RandomSumPliers implements Tool {
	/**
	 * @return tool name as string
	 */
	public String getType() {
		return "rs-pliers";
	}

	/**
	 * Tool use method
	 * 
	 * @param p- Product to use tool on
	 * @return a long describing the result of tool use on Product package
	 */
	public long useOn(Product p) {
    	long value=0;
    	for(Product part : p.getParts()){
    		value+=Math.abs(func(part.getFinalId()));
    	}
      return value;
	}
	
    public long func(long id){
    	Random r = new Random(id);
        long  sum = 0;
        for (long i = 0; i < id % 10000; i++) {
            sum += r.nextInt();
        }
        return sum;
    }
}
