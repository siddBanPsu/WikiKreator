package ist.common.vector;

import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVectorFormat;
import org.apache.commons.math3.linear.SparseRealVector;



/**
 * DocVector class.
 * A vector for representing the terms and their frequencies in a document.
 * Based on http://sujitpal.blogspot.com/2011/10/computing-document-similarity-using.html
 */

public class DocVector {

    public Map<String,Integer> terms;
    @SuppressWarnings("deprecation")
	public SparseRealVector vector;

    /*Constructor
     * @terms size of term vector based on all terms in the index
     */
    public DocVector(Map<String,Integer> terms) {
        this.terms = terms;
        this.vector = new OpenMapRealVector(terms.size());
    }

    /*
     * Updates the term vector
     */
    public void setEntry(String term, double tfIdfValue) {
        //Make sure the term actually exists
        //Update the vector
        if (terms.containsKey(term)) {
            int pos = terms.get(term);
            vector.setEntry(pos, (double) tfIdfValue);
        }
    }

    //Normalizes the vector
    @SuppressWarnings("deprecation")
	public void normalize() {
        double sum = vector.getL1Norm();
        vector = (SparseRealVector) vector.mapDivide(sum);
    }

    //toString
    public String toString() {
        RealVectorFormat formatter = new RealVectorFormat();
        return formatter.format(vector);
    }

}