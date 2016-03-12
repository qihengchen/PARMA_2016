

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

// fortunately, this class is independent from Customer.java implementation
public class DDCRP {
	
	private double _alpha;
	private F _F;
	private List<? extends Customer> _customers;  // this might be set to null after use.
	private List<Table> _tables = new ArrayList<Table>();
	
	public DDCRP(List<? extends Customer> customers, double alpha, F distance_func) {
		_customers = customers;
		_alpha = alpha;
		_F = distance_func;
		run();
		inspect_customers();
		assort();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" }) //will be relaxed later
	private void run() {
		for (int i = 0; i < _customers.size(); i++) {
			Customer c_i = _customers.get(i);
			
			// link with an existing customer or itself
			List<Pair<Customer,Double>> pmf = new ArrayList<Pair<Customer,Double>>();
			pmf.add(new Pair(null, _alpha));
			for (int j = 0; j < i; j++) {
				Customer c_j = _customers.get(j);
				pmf.add(new Pair(c_j, _F.distance(c_i, c_j)));
			}
			EnumeratedDistribution<Customer> dist = new EnumeratedDistribution(pmf);
			Customer pointee = dist.sample();
			
			if (pointee == null) { // start a new table
				_tables.add(new Table(c_i));
			} else {
				pointee.pointed_by(c_i); // join a table
			}
		}
	}
	
	//get rid of pointers, group connected customers around one table
	public void assort() {
		for (Table t : _tables) {
			t.sort_customers();
		}
	}
	
	//print out pointer info, for debugging and visualization
	public void inspect_customers() {
		System.out.println("there are " + _tables.size() + " tables");
		
	}
	
	public List<Table> get_tables() {
		return _tables;
	}

}
