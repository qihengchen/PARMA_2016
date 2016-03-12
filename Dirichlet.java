
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.stat.StatUtils;


public class Dirichlet {
	
	double _scale = 1.0; // all gamma scales default to one
	//GammaDistribution _nomin;
	//GammaDistribution[] _denom;
	GammaDistribution[] _gammas;
	
	public Dirichlet(double[] hyperparams) {
		_gammas = new GammaDistribution[hyperparams.length];
		for (int i=0; i<hyperparams.length; i++) {
			_gammas[i] = new GammaDistribution(hyperparams[i], _scale);
		}
	}
	
	public double[] sample() {
		double[] p = new double[_gammas.length];
		for (int i=0; i<_gammas.length; i++) {
			p[i] = _gammas[i].sample();
		}
		double norm = StatUtils.sum(p);
		for (int i=0; i<_gammas.length; i++) {
			p[i] /= norm;
		}
		return p;
	}
	
	/*
	public Dirichlet(double[] hyperparams) {
		_nomin = new GammaDistribution(StatUtils.sum(hyperparams), _scale);
		for (int i=0; i<hyperparams.length; i++) {
			_denom[i] = new GammaDistribution(hyperparams[i], _scale);
		}
	}
	
	public double sample(double[] p) {
		double nomin = _nomin.density(StatUtils.sum(p));
		double denom = 1.0;
		double post = 1.0;
		for (int i=0; i<p.length; i++) {
			denom *= _denom[i].density(p[i]);
			post *= Math.pow(p[i], _hyperparams[i]-1);
		}
		return nomin * denom * post;
	}*/

}
