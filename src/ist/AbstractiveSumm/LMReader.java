package ist.AbstractiveSumm;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import research.lib.MyBerkeleyLm;
import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.cache.ArrayEncodedCachingLmWrapper;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.sidd.StubImprover.WikiHelper;

public class LMReader {

	/**
	 * @param args
	 */
	//static String LMFile="lib/LM/lm_giga_64k_vp_3gram.arpa";//3-grams
	//static String LMFile="lib/LM/lm_giga_20k_nvp_3gram.arpa";//3-grams
	static String LMFile=null;//WikiHelper.getSpecificProperty("LM-File");//"lib/LM/lm_csr_5k_vp_2gram.arpa";//2-grams
	
	public LMReader() throws IOException
	{
		LMFile=WikiHelper.getSpecificProperty("LM_File");
		
	}
	NgramLanguageModel<String> lm;
	public void readLanguageModel()
	{
		readLmFromFile(LMFile);
	}
	
	public double getSentenceLogProb(String s) {
		List<String> sentence = new LinkedList<String>();
		for (String ss : s.split("\\s+"))
			sentence.add(ss);
		return (1/1-lm.scoreSentence(sentence)); //modified this
	}
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		LMReader lmReader=new LMReader();
		lmReader.readLanguageModel();
		String sentence1="I will go to the parlor today.";
		String sentence2="I will police parlor human this tomorrow.";
		System.out.println(1/(1-lmReader.getSentenceLogProb(sentence1)));
		System.out.println(1/(1-lmReader.getSentenceLogProb(sentence2)));
		
	}
	
	private void readLmFromFile(String lmfile) {
		System.err.println("Loading language model from " + lmfile);
		StringWordIndexer swi = new StringWordIndexer();
		NgramLanguageModel<String> ngramLm;
		if (lmfile.endsWith(".b") || lmfile.endsWith(".bi")
				|| lmfile.endsWith(".bin") || lmfile.endsWith("binary")) {
			ngramLm = LmReaders.readLmBinary(lmfile);
		} else {
			ngramLm = LmReaders
					.readArrayEncodedLmFromArpa(lmfile, false, swi);
		}
		lm = ArrayEncodedCachingLmWrapper
				.wrapWithCacheNotThreadSafe((ArrayEncodedNgramLanguageModel<String>) ngramLm);
	}

}
