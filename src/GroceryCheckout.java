import java.util.*;
import java.io.*;
public class GroceryCheckout
{

	private PriorityQueue<SimEvent> FEL;


	private Queue<Customer> checkLine; 
	
	private ArrayList<Customer> customers;
	private int numCustomers;
	private Customer currCust;
	private CheckOut cashier;
	private Random r;

	public GroceryCheckout(int n)
	{
		FEL = new PriorityQueue<SimEvent>();
		                   // Keeping the Future Event List as a PQ allows
		                   // it to be maintained by the next closest event
						  
		checkLine = new LinkedList<Customer>();  // The customers waiting to be
		                   // checked out are in a simple queue (FIFO).
		numCustomers = n;
		customers = new ArrayList<Customer>();
		                   
		cashier = new CheckOut();
		r = new Random((new Date()).getTime());  // Seed the random number
		                   // generator so output differs from run to run.
	}

	
	public void runSimulation() throws ClassNotFoundException
	{
		int custNum = 0;
		int clock = 0;
		FEL.add(new ArrivalEvent(0));  // Initialize FEL with first arrival
		// Simulation should run until no more events are left
		while (!FEL.isEmpty())
		{
			SimEvent e = FEL.remove();
			clock = e.get_e_time();
			if (Class.forName("ArrivalEvent").isInstance(e))
			{
				custNum++;
				currCust = new Customer(custNum, e.get_e_time());
				currCust.serviceT = serviceTime();
				if (!cashier.isBusy())
				{
					currCust.startServiceT = clock;
					cashier.addCust(currCust);
					FEL.add(new CompletionEvent(clock + currCust.serviceT));
				}
				else
				{
					checkLine.add(currCust);
				}
				if (custNum < numCustomers)
					FEL.add(new ArrivalEvent(clock + deltaCustomer()));
			}
			else // Event is a CompletionEvent
			{
				currCust = cashier.removeCust();
				currCust.endServiceT = clock;
				currCust.waitT = clock - currCust.arrivalT - currCust.serviceT;
				currCust.inSystemT = clock - currCust.arrivalT;
				customers.add(currCust);
				if (!checkLine.isEmpty())
				{
					currCust = checkLine.remove();
					currCust.startServiceT = clock;
					cashier.addCust(currCust);
					FEL.add(new CompletionEvent(clock + currCust.serviceT));
				}
			}
		}
	}

	// Go through the list of customers and output their information (as well as
	// some other statistics).  Note that alternatively much of this output
	// could be done as the simulation is executing.
	public void showResults()
	{
		System.out.println("Customer  Time Since  Arrival  Service  Time Service  Time Customer  Time Service   Time Spent  Idle Server");
		System.out.println("   Id        Last      Time     Time       Begins         Waits         Ends          in Sys       Time");
		System.out.println("--------  ----------  -------  -------  ------------  -------------  ------------   ----------  -----------");
		int oldA = 0;
		int oldT = 0;
		int arrTot = 0;
		int servTot = 0;
		int waitTot = 0;
		int sysTot = 0;
		int idleTot = 0;
		int numWait = 0;
		int totalTime = 0;
		for (int i = 0; i < customers.size(); i++)
		{
			currCust = customers.get(i);
			System.out.print("   " + currCust.id + "\t");
			if (i == 0)
				System.out.print("      " + 0 + "\t\t");
			else
			{
				int deltaA = currCust.arrivalT - oldA;
				System.out.print("      " + deltaA + "\t\t");
				arrTot += deltaA;
			}
			System.out.print(currCust.arrivalT + "\t");
			System.out.print("  " + currCust.serviceT + "\t");
			servTot += currCust.serviceT;
			System.out.print("    " + currCust.startServiceT + "\t\t");
			int currWait = currCust.startServiceT - currCust.arrivalT;
			System.out.print("   " + currWait + "\t");
			waitTot += currWait;
			if (currWait > 0) numWait++;
			System.out.print("\t " + currCust.endServiceT + "\t");
			System.out.print("\t" + (currCust.endServiceT - currCust.arrivalT) + "\t");
			sysTot += currCust.endServiceT - currCust.arrivalT;
			if (i == 0)
				System.out.print("     " + 0 + "\t");
			else
			{
				int idleT = currCust.startServiceT - oldT;
				System.out.print("     " + idleT + " ");
				idleTot += idleT;
			}
			oldT = currCust.endServiceT;
			oldA = currCust.arrivalT;
			if (i == (customers.size()-1))
				totalTime = currCust.endServiceT;
			System.out.println();
		}
		System.out.println();
		System.out.println("Average Wait: " + (waitTot/((float) numCustomers)));
		System.out.println("P(wait): " + (numWait/((float) numCustomers)));
		System.out.println("Frac. Idle: " + (idleTot/((float) totalTime)));
		System.out.println("Ave. Service: " + (servTot/((float) numCustomers)));
		System.out.println("Ave. Interarrival: " + (arrTot/((float) (numCustomers - 1))));
		System.out.println("Ave. Waiter Wait: " + (waitTot/((float) numWait)));
		System.out.println("Ave. in System: " + (sysTot/((float) numCustomers)));
	}

	
	public static void main (String [] args) throws ClassNotFoundException
	{
		int numCusts = 20;
		GroceryCheckout sim = new GroceryCheckout(numCusts);
		sim.runSimulation();
		sim.showResults();
	}

	
	public int deltaCustomer()
	{
		return (r.nextInt(8) + 1);
	}

	// Method to provide the service time distribution
	public int serviceTime()
	{
		double zeroOne = r.nextDouble();
		if (zeroOne <= 0.1) return 1;
		else if (zeroOne <= 0.30) return 2;
		else if (zeroOne <= 0.60) return 3;
		else if (zeroOne <= 0.85) return 4;
		else if (zeroOne <= 0.95) return 5;
		else return 6;
	}


	private class CheckOut
	{
		private boolean busy;
		private Customer currentCust;

		public CheckOut()
		{
			busy = false;
			currentCust = null;
		}

		public boolean isBusy()
		{
			return busy;
		}

		public void addCust(Customer c)
		{
			currentCust = c;
			busy = true;
		}

		public Customer removeCust()
		{
			Customer t = currentCust;
			currentCust = null;
			busy = false;
			return t;
		}
	}

	private class Customer
	{
		private int id;
		public int arrivalT, serviceT, startServiceT, waitT, endServiceT, inSystemT;

		public Customer(int newid, int arr)
		{
			id = newid;
			arrivalT = arr;
		}
	}
}