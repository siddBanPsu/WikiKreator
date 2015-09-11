package edu.sidd.LexRank;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class calculates the cosine similarity between two documents.
 * Somewhat based on http://sujitpal.blogspot.com/2011/10/computing-document-similarity-using.html
 * 
 * The code works as follows:
 * 1. A HashMap representing the vocabulary is built for both languages
 * 2. A DocVector is created for each document and initialized with the vocabulary
 * 3. The weight of each token in the DocVector is updated based on the frequency in the document represented by that DocVector
 * 4. The DocVectors are normalized and the cosine similarity between the two is calculated
 */


/**
 * 
 * These two classes are enough to create the cosine similarity 
 * 
 * @author sub253
 *
 */
public class CosineDocumentSimilarity {

	/**
	 * Creates a new CosineSimilarity object.
	 */
	public CosineDocumentSimilarity() {

	}

	/**
	 * Calculates the similarity between two TermFreqVectors
	 * @param vec1 the first TermFreqVector
	 * @param vec2 the second TermFreqVector
	 * @return the cosine similarity of the TermFreqVectors
	 */
	public double cosineSimilarity(TermFreqVector vec1, TermFreqVector vec2) throws IOException {

		HashMap<String,Integer> terms  = new HashMap<String,Integer>();

		//Get all of the terms and term frequencies in the two vecors
		String[] termTexts1 = vec1.getTerms();
		String[] termTexts2 = vec2.getTerms();
		int[] termFreqs1 = vec1.getTermFrequencies();
		int[] termFreqs2 = vec2.getTermFrequencies();

		//Store the terms and their positions in a hashmap - this represents the vocabulary
		int pos = 0;
		for (String term : termTexts1) {
			terms.put(term, pos++);
		}
		for (String term : termTexts2) {
			if (!terms.containsKey(term)) {
				terms.put(term, pos++);
			}
		}

		//Create vectors representing the two documents
		DocVector dv1 = new DocVector(terms);
		DocVector dv2 = new DocVector(terms);

		//Set the entries in the two documents, i.e., the term weights in the document vectors
		for (int i = 0; i < termTexts1.length; i++) {
			dv1.setEntry(termTexts1[i], termFreqs1[i]);
		}
		for (int i = 0; i < termTexts2.length; i++) {
			dv2.setEntry(termTexts2[i], termFreqs2[i]);
		}

		//Normalize
		dv1.normalize();
		dv2.normalize();

		//Return the cosine similarity of the two document vectors
		return (dv1.vector.dotProduct(dv2.vector))/(dv1.vector.getNorm() * dv2.vector.getNorm());

	}

	private static double calcCosineSimilarity(DocVector d1, DocVector d2) {
		return (d1.vector.dotProduct(d2.vector)) /
				(d1.vector.getNorm() * d2.vector.getNorm());
	}


	/**
	 * Calculates the cosine similarity between two documents.
	 * @param d1 the first document
	 * @param d2 the second document
	 * @return the cosine similarity
	 * @throws IOException
	 */
	public static String baseDir="C:\\Work\\Projects\\WikipediaTemplate\\codes\\Sweble\\WikiGUI_v2\\";
	
	public static double getCosineSimilarity(String d1, String d2) throws IOException{

		RAMDirectory ramDir = new RAMDirectory();
		FileReader fr=new FileReader(new File("lib/stoplists/en.txt"));

		//	Set<String> stopWords = new HashSet<String>(FileUtils.readLines(new File("stop-words.txt")));  
		Analyzer analyzer = new StopAnalyzer(Version.LUCENE_36, fr );
		//Index the full text of both documents
		//IndexWriter writer = new IndexWriter(ramDir, new StandardAnalyzer(Version.LUCENE_36), true, IndexWriter.MaxFieldLength.UNLIMITED);
		IndexWriter writer =new IndexWriter(ramDir, new IndexWriterConfig(Version.LUCENE_36, analyzer));
		Document doc1 = new Document();
		StringReader d1reader=new StringReader(d1);
		doc1.add(new Field("contents", d1reader, TermVector.YES));

		writer.addDocument(doc1);
		Document doc2 = new Document();
		StringReader d2reader=new StringReader(d2);

		doc2.add(new Field("contents", d2reader , TermVector.YES));
		writer.addDocument(doc2);
		//  writer.commit();
		writer.close();

		DocVector[] docs = new DocVector[2];
		//Build a term vector for each document
		IndexReader RAMreader = IndexReader.open(ramDir);
		Map<String,Integer> terms = new HashMap<String,Integer>();
		//TermEnum termEnum = RAMreader.terms(new Term("contents"));

		//System.out.println(RAMreader.numDocs());
		TermFreqVector tfvs1 = RAMreader.getTermFreqVector(0,"contents");
		TermFreqVector tfvs2 = RAMreader.getTermFreqVector(1,"contents");
		//System.out.println(tfvs1.toString());
		if(tfvs1==null || tfvs2==null)
		{
			return 0.0;
		}
			
		String[] termTexts1 = tfvs1.getTerms();

		String[] termTexts2 = tfvs2.getTerms();
		
		//Store the terms and their positions in a hashmap - this represents the vocabulary
		int pos = 0;
		for (String term : termTexts1) {
			terms.put(term, pos++);
		}
		for (String term : termTexts2) {
			if (!terms.containsKey(term)) {
				terms.put(term, pos++);
			}
		}	
		
		docs[0]=new DocVector(terms);
        docs[1]=new DocVector(terms);
		int[] termFreqs1 = tfvs1.getTermFrequencies();
		for (int j = 0; j < termTexts1.length; j++) {
			//System.out.println("termtext:"+termTexts1[j]);
			double idfValue=getIDF(RAMreader,termTexts1[j]);
			//System.out.println("idf:"+idfValue);
			double tfIdfValue=termFreqs1[j]*idfValue;
			// docs[i].setEntry(termTexts[j], termFreqs[j]);
			//System.out.println("TF IDF value "+termFreqs[j]+" "+termTexts[j]+" "+idfValue+"\t"+tfIdfValue);
			docs[0].setEntry(termTexts1[j], tfIdfValue);
		}


		int[] termFreqs2 = tfvs2.getTermFrequencies();
		for (int j = 0; j < termTexts2.length; j++) {
			double idfValue=getIDF(RAMreader,termTexts2[j]);
			double tfIdfValue=termFreqs2[j]*idfValue;
			// docs[i].setEntry(termTexts[j], termFreqs[j]);
			//System.out.println("TF IDF value "+termFreqs[j]+" "+termTexts[j]+" "+idfValue+"\t"+tfIdfValue);
			docs[1].setEntry(termTexts2[j], tfIdfValue);
		}

//
//
//
//		System.out.println(terms.toString());  
//		System.out.println(docs[0]);
//		System.out.println(docs[1]);
		RAMreader.close();
		ramDir.close();
		//        docs[0].normalize();
		//        docs[1].normalize();

		//Return the cosine similarity of the term vectors

		return calcCosineSimilarity(docs[0], docs[1]);

	}

	public static double getIDF(IndexReader reader, String termName) throws IOException
	{
		return 1+Math.log(reader.numDocs()/ ((double)reader.docFreq(new Term("contents",termName))+1));
	}


	/**
	 * Main class.
	 * Returns the cosine similarity between two documents
	 * @param args an array containing the filenames to compare
	 */
//	public static void main (String [] args) throws Exception{
//
//		String file1 = args[0];
//		String file2 = args[1];
//
//		System.out.println(new CosineDocumentSimilarity().getCosineSimilarity(file1, file2));
//
//	}

	public static double getCosineSimValue(String s1, String s2) throws IOException {

		CosineDocumentSimilarity cosineDocumentSimilarity=new CosineDocumentSimilarity();
		//return (cosineDocumentSimilarity.getCosineSimilarity("Hello rodgers tell remote what", 
		//		"great labradr homecoming lazy gear remote" ));
		//\

		return cosineDocumentSimilarity.getCosineSimilarity(s1, s2);

	}
	
	//Test for this class
public static void main(String[] args) throws IOException {
	
	CosineDocumentSimilarity cosineDocumentSimilarity=new CosineDocumentSimilarity();
	System.out.println(cosineDocumentSimilarity.getCosineSimilarity("Hello doggy", 
	"great labradr doggy" ));
}
	


}