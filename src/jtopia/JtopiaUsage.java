package jtopia;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class JtopiaUsage {
	public static Logger logger = Logger.getLogger(JtopiaUsage.class.getName());
	static String baseDir="C:\\Work\\Projects\\WikipediaTemplate"+
			"\\codes\\Sweble\\KeyWordExtractor\\";
	static TermsExtractor termExtractor =null;
	static  TermDocument topiaDoc=null;
	public static void loadComponents()
	{ //english-left3words-distsim.tagger
		Configuration.setTaggerType("stanford"); 
		Configuration.setSingleStrength(2);
		Configuration.setNoLimitStrength(2);
		// if tagger type is "openNLP" then give the openNLP POS tagger path
		//Configuration.setModelFileLocation("model/openNLP/en-pos-maxent.bin"); 
		// if tagger type is "default" then give the default POS lexicon file
		//Configuration.setModelFileLocation("model/default/english-lexicon.txt");
		// if tagger type is "stanford "
		termExtractor= new TermsExtractor();
		topiaDoc= new TermDocument();
		Configuration.setModelFileLocation("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		StanfordTagger.initialize();
	}
	
	
	public static List<String> getKeyPhrasesUsingJtopia(String text)
    {
		//for default lexicon POS tags
		//Configuration.setTaggerType("default"); 
		// for openNLP POS tagger
		//Configuration.setTaggerType("openNLP");
		//for Stanford POS tagger
		
		

     	topiaDoc = termExtractor.extractTerms(text);
		//System.out.println("Extracted terms : "+topiaDoc.getExtractedTerms());
		//System.out.println("Final Filtered Terms : "+topiaDoc.getFinalFilteredTerms());
		List<String> intKeys = new ArrayList<String>(topiaDoc.getFinalFilteredTerms().keySet());
		//System.out.println(intKeys.size());
		return intKeys;
		
    }


	public static void main(String[] args) {
		
		JtopiaUsage.loadComponents();
		System.out.println(JtopiaUsage.getKeyPhrasesUsingJtopia("To construct a Stanford CoreNLP object from a given set of properties, use StanfordCoreNLP(Properties props). This method creates the pipeline using the annotators given in the \"annotators\" property (see above for an example setting). The complete" +
				" list of accepted annotator names is listed in the first column of the table above"));
		
	}
	
}
