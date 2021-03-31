import java.io.*;
import java.util.*;

public class Scheduling {
	static ArrayList<Process> processes; 
	static Queue<Process> readyQueue;
	static ArrayList<Process> blockedList, rearReadyQueue, unstartedList;
	static int n, cycle, blocked,  terminateNum , randPos ;
	static Process pro = null;
	static CompArrival compArr;
	static CompRun compRun;
	static boolean verbose = false;
	
	private static int randomOS(int U) throws FileNotFoundException {

		File file = new File("random-numbers.txt");
		Scanner inScan = new Scanner(file);
		int X = 0;
		for (int i = 0; i < randPos; i++) {
			X = inScan.nextInt();
		}
		randPos++;
		return 1 + X%U;
	}
	public static void initializeProcess(File file) throws FileNotFoundException{
		processes = new ArrayList<Process>();
		readyQueue = new LinkedList<Process>();
		blockedList = new ArrayList<Process>();
		rearReadyQueue = new ArrayList<Process>();
		unstartedList = new ArrayList<Process>();
		n=0; cycle = 0; blocked = 0;  terminateNum = 0; randPos = 1;
		pro = null; 
		compArr = new CompArrival();
		compRun = new CompRun();
		
		Scanner inFile = new Scanner(file);
		n = inFile.nextInt(); 
		int counter = n;
		int a, b, c, io;

		while (counter > 0) {
			a = inFile.nextInt();
			b = inFile.nextInt();
			c = inFile.nextInt();
			io = inFile.nextInt();
			processes.add(new Process(a, b, c, io));
			counter--;
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String fileName = args[args.length - 1];
		if (args[0].equals("--verbose")) 
			verbose = true;
		File file = new File(fileName);
		initializeProcess(file);
		FCFS();
		
		initializeProcess(file);
		RR();
		
		initializeProcess(file);
		UniProgrammed();
		initializeProcess(file);
		PSJF();

	}

	private static void printInitialVerbose(boolean verboseFlag){
		if (verboseFlag) {
			System.out.println("\nThis detailed printout gives the state and remaining burst for each process");
			System.out.print("\nBefore cycle " + cycle + ": ");
			for (int i = 0; i < n; i++) 
				System.out.printf("\t%10s %d", processes.get(i).status, processes.get(i).burst);
		}	
	}

	private static void FCFS() throws FileNotFoundException {
		printProcesses();
		printInitialVerbose(verbose);
		cycle++;
		// cycle zero
		initialRound();
	
		while (terminateNum != n) {
			putUnstartedToReadyQueue();
			
			if (pro == null) {
				pro = readyQueue.poll();
				if (pro != null) {
					pro.status = "running";
					pro.burst = randomOS(pro.cpuBase);
				}
			}
			
			if (pro != null) {
				pro.burst--;
				pro.cpuTimeRemaining--;
			}
			verbosePrinter(verbose);

			for (Process p: readyQueue) 
				p.waitTime++;
			
			putBlockedToReadyQueue();
			
			if (pro != null) {
				if (pro.cpuTimeRemaining == 0) {
					processTerminated();
				} else {
					if (pro.burst == 0) {
						pro.status = "blocked";
						pro.burst = randomOS(pro.ioBase);
						blockedList.add(pro);
						pro = null;
					}
				}
			}
			
			cycle++;
		}
		System.out.printf("\n\nThe scheduling algorithm used was First Come First Served\n\n");
		printSummary();
	}
	
	private static void UniProgrammed() throws FileNotFoundException{
		printProcesses();
		printInitialVerbose(verbose);
		cycle++;
		initialRound();
		
		while (terminateNum != n) {
			putUnstartedToReadyQueue();

			
			if (pro == null) {
				pro = readyQueue.poll();
				if (pro != null) {
					pro.status = "running";
					pro.burst = randomOS(pro.cpuBase);
				}
			}
			if (verbose) {
				System.out.print("\nBefore cycle " + cycle + ": ");
				for (int i = 0; i < n; i++) 
					System.out.printf("\t%9s %d", processes.get(i).status, processes.get(i).burst);
				
			}		
			
			if (pro != null) {
				if (pro.status.equals("running")){
					pro.burst--;
					pro.cpuTimeRemaining--;
					if (pro.cpuTimeRemaining == 0) {
						processTerminated();
					} else {
						if (pro.burst <= 0) {
							pro.status = "blocked";
							pro.burst = randomOS(pro.ioBase);
							}
						}
					}
				else if (pro.status.equals("blocked")){
					pro.burst--;
					blocked++;
					pro.ioTime++;
					if (pro.burst <= 0) {
						pro.status = "running";
						pro.burst = randomOS(pro.cpuBase);
					}
				}
			}
			for (Process p: readyQueue) 
				p.waitTime++;
	
			
			cycle++;
		}
		System.out.printf("\n\nThe scheduling algorithm used was Uniprocessor\n\n");
		printSummary();
	}
	
	private static void processTerminated() {
		terminateNum++;
		pro.status = "terminated";
		pro.burst = 0;
		pro.finishTime = cycle;
		pro.turnTime = pro.finishTime - pro.arrivalTime;
		pro = null;		
	}
	
	private static void putUnstartedToReadyQueue() {
		for (int i = 0; i < unstartedList.size(); i++) {
			if (unstartedList.get(i).arrivalTime + 1 == cycle) {
				unstartedList.get(i).status = "ready";
				readyQueue.add(unstartedList.get(i));
			}
		}		
	}
	
	private static void putUnstartedToRearReadyQueue() {
		for (int i = 0; i < unstartedList.size(); i++) {
			if (unstartedList.get(i).arrivalTime + 1 == cycle) {
				unstartedList.get(i).status = "ready";
				rearReadyQueue.add(unstartedList.get(i));
			}
		}		
	}
	private static void verbosePrinter(boolean verboseFlag) {
		if (verboseFlag) {
			System.out.print("\nBefore cycle " + cycle + ": ");
			for (int i = 0; i < n; i++) {
				if (processes.get(i).status.equals("running")) {
					System.out.printf("\t%9s %d", processes.get(i).status, 1 + processes.get(i).burst);
				} else {
					System.out.printf("\t%9s %d", processes.get(i).status, processes.get(i).burst);
				}
			}
		}		
	}
	private static void initialRound() {
		for (int i = 0; i < n; i++) {
			processes.get(i).burst = 0;

			if (processes.get(i).arrivalTime == 0) {
				processes.get(i).status = "ready";
				readyQueue.add(processes.get(i));
			} else {
				processes.get(i).status = "unstarted";
				unstartedList.add(processes.get(i));
			}
		}
		
	}
	private static void initialRoundToRRQ() {
		for (int i = 0; i < n; i++) {
			processes.get(i).burst = 0;

			if (processes.get(i).arrivalTime == 0) {
				processes.get(i).status = "ready";
				rearReadyQueue.add(processes.get(i));
			} else {
				processes.get(i).status = "unstarted";
				unstartedList.add(processes.get(i));
			}
		}
		
	}
	
	private static void RR() throws FileNotFoundException {
		printProcesses();
		printInitialVerbose(verbose);
		cycle++;
		initialRound();
		while (terminateNum != n) {
			putUnstartedToReadyQueue();
			if (pro == null){
				pro = readyQueue.poll();
				if (pro !=null){
					pro.status="running";
					if ((pro.burst==0) && (pro.timer ==0))
						pro.burst = randomOS(pro.cpuBase);
					
					if ((pro.burst==0) && (pro.timer !=0)){
						pro.burst = pro.timer;
						pro.timer = 0;
					}
				}
			}

		
				
			if (pro != null) {
				pro.burst--;
				pro.cpuTimeRemaining--;
				pro.quantum--;
			}
				
			if (verbose) {
				System.out.print("\nBefore cycle " + cycle + ": ");
				for (int i = 0; i < n; i++) {
					if (processes.get(i).status.equals("running")) {
							if (processes.get(i).burst != 0)
								System.out.printf("\t%10s %d", processes.get(i).status, 1 + processes.get(i).quantum);
							else
								System.out.printf("\t%10s %d", processes.get(i).status, 1);
					}
					else 
						System.out.printf("\t%10s %d", processes.get(i).status, processes.get(i).burst);
					}
				}
			
			for (Process p: readyQueue) 
					p.waitTime++;
			
			putBlockedToRearReadyQueue();
			
			
			
			if (pro != null) {
				if (pro.cpuTimeRemaining == 0) {
					processTerminated();
				} else {
					if (pro.burst == 0) {
						pro.status = "blocked";
						pro.burst = randomOS(pro.ioBase);
						pro.quantum = 2;
						blockedList.add(pro);
						pro = null;
					} else if (pro.quantum == 0) {
							pro.status = "ready";
							pro.timer = pro.burst;
							pro.burst = 0;
							pro.quantum = 2;
							rearReadyQueue.add(pro);
							pro = null;
						}					
					}
				
			}
			cycle++;
			
			Collections.sort(rearReadyQueue, compArr);
			readyQueue.addAll(rearReadyQueue);
			rearReadyQueue = new ArrayList<Process>(); ;
		}
		System.out.printf("\n\nThe scheduling algorithm used was Round Robbin\n\n");
		printSummary();
	}


	private static void putBlockedToRearReadyQueue() {
		if (!blockedList.isEmpty()) {
			blocked++;
			ArrayList<Process> readyArray = new ArrayList<Process>(),
					tempBlockedList = new ArrayList<Process>();
            tempBlockedList.addAll(blockedList);
			
			for (int i = 0; i < tempBlockedList.size(); i++) {
				tempBlockedList.get(i).burst--;
				tempBlockedList.get(i).ioTime++;
				if (tempBlockedList.get(i).burst == 0) {
					tempBlockedList.get(i).status = "ready";
					readyArray.add(tempBlockedList.get(i));
					blockedList.remove(tempBlockedList.get(i));
				}
			}
				Collections.sort(readyArray, compArr);
				rearReadyQueue.addAll(readyArray);
		
		}
		
					

		
		
	}
	private static void putBlockedToReadyQueue() {
		if (!blockedList.isEmpty()) {
			blocked++;
			ArrayList<Process> readyArray = new ArrayList<Process>(),
					tempBlockedList = new ArrayList<Process>();
            tempBlockedList.addAll(blockedList);
			
			for (int i = 0; i < tempBlockedList.size(); i++) {
				tempBlockedList.get(i).burst--;
				tempBlockedList.get(i).ioTime++;
				if (tempBlockedList.get(i).burst == 0) {
					tempBlockedList.get(i).status = "ready";
					readyArray.add(tempBlockedList.get(i));
					blockedList.remove(tempBlockedList.get(i));
				}
			}
				Collections.sort(readyArray, compArr);
				readyQueue.addAll(readyArray);
		
		}
		
	}
	private static void PSJF() throws FileNotFoundException {
		printProcesses();
		printInitialVerbose(verbose);
		cycle++;

		initialRoundToRRQ();
		//initialWaitCounting();

		while (terminateNum != n) {
				

			if (verbose && !(cycle ==1)) {
				System.out.print("\nBefore cycle " + (cycle-1) + ": ");
			}
			
			putUnstartedToRearReadyQueue();
			
			if (pro != null && !readyQueue.isEmpty()) {
				if (pro.cpuTimeRemaining > readyQueue.peek().cpuTimeRemaining) {
					if (pro.burst == 0) {
						pro.status = "blocked";
						pro.burst = randomOS(pro.ioBase);
						blockedList.add(pro);
					} else {
						pro.status = "ready";
						rearReadyQueue.add(pro);
					}
					pro = null;
				}
			}
			
			if (pro == null) {
				pro = readyQueue.poll();
				if (pro != null) {  
					pro.status = "running";
					if (pro.burst == 0) {
						pro.burst = randomOS(pro.cpuBase);
					}
				}
			}
			
			for (Process p: readyQueue) 
				p.waitTime++;
			
			rearReadyQueue.addAll(readyQueue);
			readyQueue.clear();
			
			
			if (pro != null) {
				pro.burst--;
				pro.cpuTimeRemaining--;
			}
			
	    	if (verbose && !(cycle==1)) {
				for (int i = 0; i < n; i++) {
					if (processes.get(i).status.equals("running")) {
							System.out.printf("\t%10s %d", processes.get(i).status, 1 + processes.get(i).burst);
					} //else if (cycle==0 && i==0){
						//System.out.printf("\t%10s %d", "unstarted", processes.get(i).burst);
					//}
					else
					
						System.out.printf("\t%10s %d", processes.get(i).status, processes.get(i).burst);
					
				}
			}
			
			putBlockedToRearReadyQueue();

			if (pro != null) {
				if (pro.cpuTimeRemaining == 0) {
					pro.status = "terminated";
					pro.burst = 0;
					pro.finishTime = cycle -1;
					pro.turnTime = pro.finishTime - pro.arrivalTime;
					terminateNum++;
					pro = null;
				} else {
					if (pro.burst == 0) {
						pro.status = "blocked";
						pro.burst = randomOS(pro.ioBase);
						blockedList.add(pro);
						pro = null;
					} else {
						pro.status = "ready";
						rearReadyQueue.add(pro);
						pro = null;
					}
				}
			};
			cycle++;
			
			Collections.sort(rearReadyQueue, compRun);
			readyQueue.addAll(rearReadyQueue);
			rearReadyQueue.clear();
		}
		cycle--;
		System.out.printf("\n\nThe scheduling algorithm used was Preemptive Shortest Job First\n\n");
		printSummary();
	}

//	private static void initialWaitCounting() {
//		for (int i = 1; i < n; i++)	{
//			processes.get(i).waitTime++;
//		}
//	}
	private static void printSummary() {
		double totalRun = 0;
		double totalTurnaround = 0;
		double totalWait = 0;
		for (int i = 0; i < n; i++) {
			totalRun += processes.get(i).cpuTime;
			totalTurnaround += processes.get(i).turnTime;
			totalWait += processes.get(i).waitTime;
			System.out.println("Process " + i + ": ");
			System.out.printf("\t(A, B, C, IO) = (%d, %d, %d, %d)\n", processes.get(i).arrivalTime, processes.get(i).cpuBase, processes.get(i).cpuTime, processes.get(i).ioBase);
			System.out.println("\tFinishing time: " + processes.get(i).finishTime);
			System.out.println("\tTurnaround time: " + processes.get(i).turnTime);
			System.out.println("\tI/O time: " + processes.get(i).ioTime);
			System.out.println("\tWaiting time: " + processes.get(i).waitTime);
			System.out.println();
		}

		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + (cycle - 1));
		System.out.printf("\tCPU Utilization: %6f\n", totalRun/(double)(cycle - 1));
		System.out.printf("\tI/O Utilization: %6f\n", (double)blocked/(double)(cycle - 1));
		System.out.printf("\tThroughput: %6f processes per hundred cycles\n", (double)((100*n)/(double)(cycle - 1)));
		System.out.printf("\tAverage turnaround time: %6f\n", totalTurnaround/n);
		System.out.printf("\tAverage waiting time: %6f\n", totalWait/n);
		
		System.out.println();
	}

	private static void printProcesses() {
		System.out.printf("The original input was: %d", n);
		for (int i = 0; i < n; i++) {
			System.out.printf("\t%d %d %d %d ", processes.get(i).arrivalTime, processes.get(i).cpuBase, processes.get(i).cpuTime, processes.get(i).ioBase);
			processes.get(i).id = i;
		}

		Collections.sort(processes, compArr);

		System.out.printf("\nThe (sorted) input is:  %d", n);
		for (int i = 0; i < n; i++) {			processes.get(i).id = i;
			System.out.printf("\t%d %d %d %d ", processes.get(i).arrivalTime, processes.get(i).cpuBase, processes.get(i).cpuTime, processes.get(i).ioBase);

		}
		
	}
	
	
}

class Process {
	
	int ioBase, finishTime, turnTime, ioTime, waitTime, quantum,
	 id, arrivalTime, burst, timer, cpuBase, cpuTime,
	cpuTimeRemaining;
	
	String status;
	

	public Process(int A, int B, int C, int IO){
		this.arrivalTime = A;
		this.cpuBase = B;
		this.cpuTime = C;
		this.cpuTimeRemaining = C;
		this.timer = 0;
		this.ioBase = IO;
		this.status = "unstarted";
		this.quantum = 2;
	}
}

class CompArrival implements Comparator<Process> {
	public int compare(Process p1, Process p2) {
		if(p1.arrivalTime > p2.arrivalTime) {
			return 1;
		} else if(p1.arrivalTime < p2.arrivalTime) {
			return -1;
		} else if (p1.id > p2.id) {
			return 1;
		} else {
			return -1;
		}
	}
}

class CompRun implements Comparator<Process> {
	public int compare(Process p1, Process p2) {
		if(p1.cpuTimeRemaining > p2.cpuTimeRemaining) {
			return 1;
		} else if(p1.cpuTimeRemaining < p2.cpuTimeRemaining) {
			return -1;
		} else if (p1.id > p2.id) {
			return 1;
		} else {
			return -1;
		}
	}
}