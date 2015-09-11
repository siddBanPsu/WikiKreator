package edu.sidd.LexRank;

import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVectorFormat;
import org.apache.commons.math3.linear.SparseRealVector;

public class DocVector {
	public Map<String,Integer> terms;
	public SparseRealVector vector;

	public DocVector(Map<String,Integer> terms) {
		this.terms = terms;
		this.vector = new OpenMapRealVector(terms.size());
	}

	public void setEntry(String term, double tfIdf) {
		if (terms.containsKey(term)) {
			int pos = terms.get(term);
			vector.setEntry(pos, (double) tfIdf);
		}
	}

	public void normalize() {
		double sum = vector.getL1Norm();
		vector = (SparseRealVector) vector.mapDivide(sum);
	}

	public String toString() {
		RealVectorFormat formatter = new RealVectorFormat();
		return formatter.format(vector);
	}
}
