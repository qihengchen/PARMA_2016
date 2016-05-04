
import java.util.ArrayList;
import java.util.List;

public class Mention implements Customer {
	
	private List<Customer> _pointers = new ArrayList<Customer>();
	private final String _doc_id, _m_id;
	private Table _sit_at;

	public Mention(String doc_id, String m_id) {
		_doc_id = doc_id;
		_m_id = m_id;
	}
	
	@Override
	public String toString() {
		return _doc_id + "," + _m_id;
	}

	public void pointed_by(Customer j) {
		_pointers.add(j);
	}
	
	public String doc_id() {
		return _doc_id;
	}
	
	public String m_id() {
		return _m_id;
	}
	
	@Override
	public int hashCode() {
		return _doc_id.hashCode() + _m_id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
            return true;
        }
        if (!(obj instanceof Mention)) {
            return false;
        }
		Mention m = (Mention) obj;
		return m.doc_id() == _doc_id && m.m_id() == _m_id;
	}
	
	public void first_to_sit(Table table) {
		_sit_at = table;
	}
	
	public Table get_table() {
		return _sit_at;
	}
	
	public List<Customer> get_pointers() {
		return _pointers;
	}

}
