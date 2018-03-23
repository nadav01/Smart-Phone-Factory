package bgu.spl.a2.sim.tools;

import java.math.BigInteger;

import bgu.spl.a2.sim.Product;

public class GCDScrewdriver implements Tool {
	/**
	 * @return tool name as string
	 */
	public String getType() {
		return "gs-driver";
	}

	/**
	 * Tool use method
	 * 
	 * @param p- Product to use tool on
	 * @return a long describing the result of tool use on Product package
	 */
	public long useOn(Product p) {
		long sum = 0;
		for(Product part : p.getParts()){
			BigInteger b1 = BigInteger.valueOf(part.getFinalId());
	        BigInteger b2 = BigInteger.valueOf(reverse(part.getFinalId()));	        
	        long value= (b1.gcd(b2)).longValue();
	        sum += Math.abs(value);
		}
		return sum;
	}
	
	  private long reverse(long n){
		    long reverse=0;
		    while( n != 0 ){
		        reverse = reverse * 10;
		        reverse = reverse + n%10;
		        n = n/10;
		    }
		    return reverse;
		  }

}
