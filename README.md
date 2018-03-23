This project was given as an assignment in the course Systems Programming at Ben Gurion University. In this assignment we were required to implement a work stealing scheduler, testing it by implementing a simple parallel merge sort algorithm, and afterwards to use it to build a smart phone factory. The  work  stealing  principle  in  this  assignment  can  be  synthesized  as  follows.   Pool  of Threads where each thread runs a Processor, each with own queue are working in order to complete tasks in their queues.  When a processor is out of tasks it attempts to steal some from its neighbors.
In a work stealing scheduler, each processor in the computer system has a queue of work tasks to perform.  while
running, each task can spawn a new task or more that can feasibly be executed in parallel with its other work.
When a processor runs out of work, it looks at the queues of other processors and steals their work items.
Each processor is a thread which maintains local work queue. A processor can push and pop tasks from its local queue. When it runs out of work, in order for a processor to steal from another one, it should select first a ”victim” from which it will steal the task. 
<br>
A full description of the assignment can be found [here](https://www.cs.bgu.ac.il/~spl171/wiki.files/spl171-assignment2.pdf).
