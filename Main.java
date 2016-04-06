import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Main {
	public static void main(String[] argv) {
		System.out.println("Step 0");
		// step0: read in corpus
		Set<String> edge_keys = new HashSet<String>();
		try {
			String ser_path = "/Users/Qiheng/Dropbox/wna_norms.ser";
		    ObjectInputStream in = new ObjectInputStream(new FileInputStream(ser_path));
		    edge_keys = ((Map<String, Edge>) in.readObject()).keySet();
		    in.close();
		} catch(Exception e) {
		    e.printStackTrace();
		    System.exit(0);
		}
		Set<String> temp = new HashSet<String>();
		for (String key : edge_keys) {
			String[] k = key.split(",");
			temp.add(k[0] + "," + k[1]);
			temp.add(k[2] + "," + k[3]);
		}
		System.out.println("edge_keys: " + edge_keys.size());
		System.out.println("mentions : " + temp.size());
		edge_keys = null;
		List<Mention> all_customers = new ArrayList<Mention>();
		for (String key : temp) {
			String[] k = key.split(",");
			all_customers.add(new Mention(k[0], k[1]));
		}
		temp = null;
		
		// c_by_rest -- customers by restaurants -- {doc_id : [mentions]}
		Map<String, List<Mention>> c_by_rest = new HashMap<String, List<Mention>>();
		for (Mention m : all_customers) {
			if (!c_by_rest.containsKey(m.doc_id())) {
				c_by_rest.put(m.doc_id(), new ArrayList<Mention>());
			}
			c_by_rest.get(m.doc_id()).add(m);
		}
		
		for (String s : c_by_rest.keySet()) {
			System.out.println(s + ": " + c_by_rest.get(s).size());
		}
		
		System.out.println("Step 1");
		// step1: intra-doc CRP
		double alpha = 5.0;  // to be tuned
		F f = new DistanceF();
		List<DDCRP> restaurants = new ArrayList<DDCRP>();
		for (String key : c_by_rest.keySet()) { // DDCRP for each document
			System.out.println("  " + key);
			restaurants.add(new DDCRP(c_by_rest.get(key), alpha, f));
		}
		
		System.out.println("Step 2");
		// step2: cross-doc CRP
		// grab table leaders
		List<Mention> leaders = new ArrayList<Mention>();
		Map<Mention, Table> old_tables = new HashMap<Mention, Table>(); // m -> local table
		for (DDCRP restaurant : restaurants) {
			for (Table table : restaurant.get_tables()) {
				Mention leader = (Mention) table.first();
				old_tables.put(leader, leader.get_table());
				leaders.add(new Mention(leader.doc_id(), leader.m_id()));
			}
		}
		assert(old_tables.size() == leaders.size());
		for (Mention leader : leaders) {
			if (!old_tables.containsKey(leader)) {
				System.out.println("not in old_table");
			}
		}
		System.out.println("check this number:  " + leaders.size());
		// run cross doc DDCRP
		DDCRP cross_doc = new DDCRP(leaders, alpha, f);
		
		System.out.println("Step 3");
		// step3: assort customers
		List<Table> cross_doc_tables = cross_doc.get_tables();  //synchronization may be necessary
		
		Map<Mention, List<Mention>> c2t  = new HashMap<Mention, List<Mention>>();
		List<List<Mention>> clusters = new ArrayList<List<Mention>>();
		
		for (Table cd_table : cross_doc_tables) {
			List<Mention> cluster = new ArrayList<Mention>();
			for (Customer leader : cd_table.get_customers()) {
				for (Customer c : old_tables.get(leader).get_customers()) {  //err
					c2t.put((Mention) c, cluster);
					cluster.add((Mention) c);
				}
			}
			clusters.add(cluster);
		}
		
		cross_doc_tables = null;
		
		System.out.println("mentions :" + c2t.size());
		/*
		for (Customer c : clusters.get(10).get_customers()) {
			Mention m = (Mention) c;
			System.out.println(m.doc_id() + " " + m.m_id());
		}*/
		
		System.out.println("Step 4");
		// step4: Gibbs sampling; repeat
		// <-- observation -> sample distribution over clusters -> sample mention assignment -->
		
		// position -> sample a distribution of clusters -> p(cluster)*p(proximity to cluster) -> take the highest
		//             number of mentions in cluster					distance function
		
		
		double beta = 0.05; // to be tuned
		Random rn = new Random();
		
		for (int iter=0; iter<30; iter++) {
			// for every customer, do:
			for (Customer customer : c2t.keySet()) {
				List<Mention> table = c2t.get(customer);
				// take the customer out
				double[] prior;
				synchronized (clusters) {
					table.remove(customer);
					if (table.size() == 0) {
						clusters.remove(table);
					}
					// dirichlet
					prior = new double[clusters.size()];
					for (int i=0; i<clusters.size(); i++) {
						prior[i] = clusters.get(i).size();
						//System.out.println(prior[i]);
					}
					
					Dirichlet dir = new Dirichlet(prior);
					// sample cluster distribution
					double[] p = dir.sample();
					if (rn.nextFloat() < beta) { //relocated to a new table
						List<Mention> t = new ArrayList<Mention>();
						t.add((Mention) customer);
						c2t.put((Mention) customer, t);
						clusters.add(t);
					} else {
						//re-locate
						double max_p=0; int new_cluster = 0;
						//System.out.println("p length: " + p.length);
						for (int i=0; i<p.length; i++) {
							double proximity = calc_proximity((Mention) customer, clusters.get(i), f); // distance to other mentions in cluster
							if (p[i] * proximity > max_p) {
								max_p = p[i] * proximity;
								new_cluster = i;
							}
						}
						c2t.put((Mention) customer, clusters.get(new_cluster));
					}
				}
			}
		}
		
		/*
		 * just run this main class. it takes care of all sub-procedures.
		 * clusters is ArrayList<ArrayList<Mention>>; the inner list is a cluster of mentions
		 * the mention class has doc_id and m_id of course. it's easy to easy in Mention.java
		 * in line 56, alpha affects the DDCRP of both hierarchies and need to be tuned.
		 * in line 121, beta affects the dirichlet and need to be tuned too. 
		 */
	}

	
	// average distance between customer to all others in table
	public static double calc_proximity(Mention customer, List<Mention> table, F f) {
		double sum = 0;
		for (Customer c : table) {
			if (c != customer) {
				sum += f.distance(customer, c);
		
			}
		}
		return sum / table.size();
	}
}