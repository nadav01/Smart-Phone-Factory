/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.test;

import bgu.spl.a2.Task;

import bgu.spl.a2.WorkStealingThreadPool;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;

public class MergeSort extends Task<int[]> {
	
	/**
	 * @param array - the array to be sorted
	 */
    private final int[] array;
    
    /**
     * @param subtasks - list of subtasks (smaller arrays to be sorted)
     */
    private ArrayList<Task<int[]>> subtasks = new ArrayList<>();

    
    /**
     * Constructor
     * @param array - the array to be sorted
     */
    public MergeSort(int[] array) {
        this.array = array;
    }

    @Override
    protected void start() {
    	
        if(array.length > 2){ // if the array's size is bigger than 2, create sub tasks
        	int[] arr1 = new int[(int)array.length/2];
        	int[] arr2 = new int[(int)array.length/2];
        	for(int i=0; i<(int)array.length/2; i++){
        		arr1[i] = array[i];
        		arr2[i] = array[((int)array.length/2)+i];
        	}
        	Task<int[]> t1 = new MergeSort(arr1);
        	Task<int[]> t2 = new MergeSort(arr2);
            subtasks.add(t1);
            subtasks.add(t2);

            
            whenResolved(subtasks, ()->{ // when the subtasks are complete, merge them and this is the result
            	int[] firstArray = (int[]) ((subtasks.get(0)).getResult()).get();
            	int[] secArray = (int[]) ((subtasks.get(1)).getResult()).get();
            	int[] res = merge(firstArray, secArray);
            	complete(res);
            });
            
            spawn(t1, t2); // spawn the two subtasks to the processor
            
        }
        
        else { // if the array is of size 2, it sort itself and this is the result
        	int[] array1 = new int[1];
        	int[] array2 = new int[1];
        	array1[0]=array[0];
        	array2[0]=array[1];
        	int[] result = merge(array1,array2);
        	complete(result);
        }
    }
    
    /**
     * This function merge's two sorted arrays into one sorted array
     * @param first - the first array
     * @param second - the second array
     * @return - one sorted array containing the elements of the first and second arrays
     */
    private int[] merge(int[] first, int[] second){
    	int[] result = new int[first.length+second.length];
    	int f = 0;
    	int s = 0;
    	
    	while((f != first.length) && (s != second.length)){
    		if(first[f] <= second[s]){
    			result[f+s] = first[f];
    			f++;
    		}
    		else{
    			result[f+s] = second[s];
    			s++;
    		}
    	}
    	
    	int ind = s+f;
    	if(f==first.length){
    		for(int j = ind; j<result.length && s<second.length; j++){
    			result[j] = second[s];
    			s++;
    		}
    	}
    	else{
    		for(int j = ind; j<result.length && f<first.length; j++){
    			result[j] = first[f];
    			f++;
    		}
    	}
    	return result;
    }
    

    
    
    public static void main(String[] args) throws InterruptedException {
    	try{
    	
    	WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
        int n = 1048576; //you may check on different number of elements if you like
        int[] array = new Random().ints(n).toArray();

        MergeSort task = new MergeSort(array);

        CountDownLatch l = new CountDownLatch(1);
        pool.start();
        pool.submit(task);
        task.getResult().whenResolved(() -> {
            //warning - a large print!! - you can remove this line if you wish
            System.out.println(Arrays.toString(task.getResult().get()));
            l.countDown();
        });

        l.await();
        pool.shutdown();
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    		}
    	}
    }
