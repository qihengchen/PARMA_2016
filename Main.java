

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Main {
	public static void main(String[] argv) {
		int num_doc = 15;
		// step0: read in corpus
		// TODO: Chris
		// replace the next four lines with your code to read in mentions
		// instead of new Mention("1", Integer.toString(i)) as below
		// maybe do something like new Mention("1_16ecbplus.xml", "9") where 9 is the mention id.
		List<Customer> customers = new ArrayList<Customer>();
		for (int i=0; i<50; i++) {
			customers.add(new Mention("1", Integer.toString(i)));
		}
		
		
		// step1: intra-doc CRP
		double alpha = 10.0;
		F f = new DistanceF();
		List<DDCRP> restaurants = new ArrayList<DDCRP>();
		for (int i=0; i<num_doc; i++) { // DDCRP for each document
			restaurants.add(new DDCRP(customers, alpha, f));
		}
		
		
		// step2: cross-doc CRP
		// grab table leaders
		List<Customer> leaders = new ArrayList<Customer>();
		for (DDCRP restaurant : restaurants) {
			for (Table table : restaurant.get_tables()) {
				leaders.add(table.first());
			}
		}
		// run cross doc DDCRP
		DDCRP cross_doc = new DDCRP(leaders, alpha, f);
		
		
		// step3: assort customers
		List<Table> clusters = cross_doc.get_tables();  //synchronization may be necessary
		
		Map<Customer, Table> c2t  = new HashMap<Customer, Table>();
		
		for (Table cluster : clusters) {
			for (Customer leader : cluster.get_customers()) {
				for (Customer c : leader.get_table().get_customers()) {
					c2t.put(c, cluster);
			}
			}
		}
		
		
		// step4: Gibbs sampling; repeat
		// <-- observation -> sample distribution over clusters -> sample mention assignment -->
		
		// position -> sample a distribution of clusters -> p(cluster)*p(proximity to cluster) -> take the highest
		//             number of mentions in cluster					distance function
		
		
		double beta = 0.05;
		Random rn = new Random();
		// for every customer, do:
		for (int iter=0; iter<30; iter++) {
			for (Customer customer : c2t.keySet()) {
				
				Table table = c2t.get(customer);
				
				// take the customer out
				double[] prior;
				synchronized (clusters) {
					table.remove(customer);
					if (table.get_customers().size() == 0) {
						clusters.remove(table);
					}
					// dirichlet
					prior = new double[clusters.size()];
					for (int i=0; i<clusters.size(); i++) {
						prior[i] = clusters.get(i).get_customers().size();
					}

					Dirichlet dir = new Dirichlet(prior);
					// sample cluster distribution
					double[] p = dir.sample();
					if (rn.nextFloat() < beta) { //relocated to a new table
						Table t = new Table(customer);
						c2t.put(customer, t);
						clusters.add(t);
					} else {
						//re-locate
						double max_p=0; int new_cluster = 0;
						for (int i=0; i<p.length; i++) {
							double proximity = calc_proximity(customer, clusters.get(i), f); // distance to other mentions in cluster
							if (p[i] * proximity > max_p) {
								max_p = p[i] * proximity;
								new_cluster = i;
							}
						}
						c2t.put(customer, clusters.get(new_cluster));
					}
				}
			}
		}
	}
	
	
	// average distance between customer to all others in table
	public static double calc_proximity(Customer customer, Table table, F f) {
		double sum = 0;
		for (Customer c : table.get_customers()) {
			if (c != customer) {
				sum += f.distance(customer, c);
		
			}
		}
		return sum / table.get_customers().size();
	}
}