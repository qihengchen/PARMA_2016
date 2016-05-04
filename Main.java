
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		// args[1:4]  1-ser_path, 2-output, 3-alpha, 4-beta
		String OUTPUT = args[0];
		String ser_path = args[1]; //"/Users/Qiheng/Dropbox/wna_norms.ser";
		double ALPHA = Double.parseDouble(args[2]);  // 5.0 to be tuned
		double BETA = Double.parseDouble(args[3]); // 0.05 to be tuned
		int ITER = Integer.parseInt(args[4]);  // 10
		
		System.out.println("Step 0");
		// read in .ser file
		//Set<String> edge_keys = new HashSet<String>();
		
		HashMap<String, Edge> edges = new HashMap<String, Edge>();
		try {
		    ObjectInputStream in = new ObjectInputStream(new FileInputStream(ser_path));
		    edges = (HashMap<String, Edge>) in.readObject();
		    in.close();
		} catch(Exception e) {
		    e.printStackTrace();
		    System.exit(0);
		}
		
		Set<String> temp = new HashSet<String>();
		for (String key : edges.keySet()) {
			String[] k = key.split(",");
			if (k[0].startsWith("31")) {
				temp.add(k[0] + "," + k[1]);
			}
			if (k[2].startsWith("31")) {
				temp.add(k[2] + "," + k[3]);
			}
		}
		System.out.println("edge_keys: " + edges.keySet().size());
		System.out.println("mentions : " + temp.size());
		//edge_keys = null;
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
		
		/*for (String s : c_by_rest.keySet()) {
			System.out.println(s + ": " + c_by_rest.get(s).size());
		}*/
		
		System.out.println("Step 1");
		// step1: intra-doc CRP
		F f = new DistanceF(edges);
		List<DDCRP> restaurants = new ArrayList<DDCRP>();
		for (String key : c_by_rest.keySet()) { // DDCRP for each document
			//  ***  System.out.println("  " + key);
			restaurants.add(new DDCRP(c_by_rest.get(key), ALPHA, f));
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
		/*for (Mention leader : leaders) {
			if (!old_tables.containsKey(leader)) {
				System.out.println("not in old_table");
			}
		}*/
		//System.out.println("check this number:  " + leaders.size());
		// run cross doc DDCRP
		DDCRP cross_doc = new DDCRP(leaders, ALPHA, f);
		
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
		
		//  *** System.out.println("mentions :" + c2t.size());
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
		
		Random rn = new Random();
		System.out.println("ITER: " + ITER);
		for (int iter=0; iter<ITER; iter++) {
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
					}
					for (int ind=0; ind < prior.length; ind ++) {
						//System.out.println(prior[ind]); //[0] + " " + prior[1] + " " + prior[2]);
					}
					Dirichlet dir = new Dirichlet(prior);
					// sample cluster distribution
					double[] p = dir.sample();
					//System.out.println(p[0] + " " + p[1] + " " + p[2]);
					if (rn.nextFloat() < BETA) { //relocated to a new table
						List<Mention> t = new ArrayList<Mention>();
						t.add((Mention) customer);
						c2t.put((Mention) customer, t);
						clusters.add(t);
					} else {
						//re-locate
						double max_p=0; int new_cluster = 0;
						for (int i=0; i<p.length; i++) {
							double proximity = calc_proximity((Mention) customer, clusters.get(i), f);
							if (p[i] * proximity > max_p) {
								max_p = p[i] * proximity;
								new_cluster = i;
							}
						}
						c2t.put((Mention) customer, clusters.get(new_cluster));
						clusters.get(new_cluster).add((Mention) customer);
					}
				}
			}
		}

		for (List<Mention> inner : clusters) {
			System.out.print(inner.size() + " ");
		}
		/*
		 * just run this main class. it takes care of all sub-procedures.
		 * clusters is ArrayList<ArrayList<Mention>>; the inner list is a cluster of mentions
		 * the mention class has doc_id and m_id of course. it's easy to easy in Mention.java
		 * in line 56, alpha affects the DDCRP of both hierarchies and need to be tuned.
		 * in line 121, beta affects the dirichlet and need to be tuned too. 
		 */
		
		PrintWriter writer = new PrintWriter(OUTPUT, "UTF-8");
		System.out.println("output path is: " + OUTPUT);
		// List<List<Mention>> clusters = new ArrayList<List<Mention>>();
		for (List<Mention> cluster : clusters) {
			writer.println("");
			for (Mention m : cluster) {
				writer.println(m);
			}
		}
		writer.close();
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