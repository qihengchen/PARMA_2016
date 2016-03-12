import java.io.*;
import java.util.*;

public class Edge implements Serializable {
	
	private String label = "";
	private Map<String, Double> features;
	private double distance;
	
	public Edge(String label) {
		this.label = label;
		this.features = new HashMap<String, Double>();
	}

	public void addFeature(String name, Double val) {
		// ensures that the feature file isn't messed up by having the same edge w/ multiple values
		// e.g., maybe the feature file had a value for both directions of d1_m1... it shouldn't at all,
		// but this is just for error checking
		if (features.containsKey(name)) {
			System.err.println("feature: " + name + " already has a value for Edge: " + this.label + "!!");
			System.exit(1);
		}
		features.put(name, val);
	}

	// sets the distance value by uniformly weighting all of the features
	public void setDistanceUniformly() {
		double ret = 0;
		for (String s : features.keySet()) {
			ret += features.get(s);
		}
		ret /= features.keySet().size();
		this.distance = ret;
	}
	
	// sets the distance value based on the passed-in weights
	public void setDistanceWeighted(List<Double> weights) {
		if (weights.size() != this.features.size()) {
			System.err.println("ERROR: must have the same # of weights as features");
			System.exit(1);
		}
		double ret = 0;
		int i=0; // iterates through each of the features (which is a HashMap, so we index this way)
		for (String s : features.keySet()) {
			ret += weights.get(i) * features.get(s);
			i++;
		}
		this.distance = ret;
	}
	

	// getter methods
	public Map<String, Double> getFeatures() {
		return this.features;
	}
	
	public double getDistance() { return this.distance; }
	
	// prints the Edge
	public String toString() {
		String ret = this.label + ":";
		for (String k : this.features.keySet()) {
			ret += (k + " (" + this.features.get(k) + ") ");
		}
		return ret;
	}

}
