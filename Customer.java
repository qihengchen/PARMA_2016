
import java.util.List;

public interface Customer {

	public void pointed_by(Customer j);
	
	public List<Customer> get_pointers();

	public void first_to_sit(Table table);
	
	public Table get_table();
}
