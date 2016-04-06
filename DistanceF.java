import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// distance function based on semantic features
public class DistanceF implements F{
	
	private Map<String, Edge> _edges; // = new HashMap<String, Edge>();
	
	public DistanceF() {
		try {
			String ser_path = "/Users/Qiheng/Dropbox/wna_norms.ser";
		    ObjectInputStream in = new ObjectInputStream(new FileInputStream(ser_path));
		    _edges = (Map<String, Edge>) in.readObject();
		    in.close();
		} catch(Exception e) {
		    e.printStackTrace();
		    System.exit(0);
		}
		// 1_16ecbplus.xml,9,1_18ecbplus.xml,53  1_16ecbplus.xml,9,1_18ecbplus.xml,53:wna (0.1359999999999758) 
	}
	
	// calculate distance between two mentions, used by DDCRP
	// this function might be SLOOOOWWWW
	@Override
	public double distance(Customer i, Customer j) {
		Mention mi = (Mention) i, mj = (Mention) j;
		String key1 = String.join(",", mi.doc_id(), mi.m_id(), mj.doc_id(), mj.m_id());
		String key2 = String.join(",", mj.doc_id(), mj.m_id(), mi.doc_id(), mi.m_id());
		return _edges.containsKey(key1)? _edges.get(key1).getDistance() : (_edges.containsKey(key2)? 
				_edges.get(key2).getDistance() : 0); 
	}

}
