

import java.util.ArrayList;
import java.util.List;

public class Table {
	
	private final Customer _first; // this variable is meaningless after DDCRP.assort()
	private List<Customer> _at_table = new ArrayList<Customer>();
	
	public Table(Customer i) {
		_first = i;
		//_at_table.add(_first);
		i.first_to_sit(this);
	}
	
	public void sort_customers() {
		List<Customer> temp = new ArrayList<Customer>();
		temp.add(_first);
		while (!temp.isEmpty()) {
			Customer c = temp.remove(0);
			_at_table.add(c);
			temp.addAll(c.get_pointers());
			//System.out.println(c.get_pointers().size() + "   " + temp.size());
		}
	}
	
	public List<Customer> get_customers() {
		return _at_table;
	}
	
	public Customer first() {
		return _first;
	}
	
	public void remove(Customer customer) {
		_at_table.remove(customer);
	}
	
	public void add(Customer customer) {
		_at_table.add(customer);
	}
	
	

}
