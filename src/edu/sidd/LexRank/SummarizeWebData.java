package edu.sidd.LexRank;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import edu.sidd.Objects.URLContentObject;
import edu.sidd.crawlXML.URLCrawlerForTopic;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class SummarizeWebData {

	static String baseDir="C:\\Work\\Projects\\WikipediaTemplate\\codes\\Sweble\\WikiGUI_v2\\";
	static int numSentencesInSummary=10;
	static double querySimilarityThreshold=0.002;
	private void createIndexWriter(List<URLContentObject> urlContentList,
			Directory ramDirectory) throws IOException
			{
		Set<String> stopWords = new HashSet<String>(FileUtils.readLines(new File(baseDir+
				"data/stopwords/stopwords_en.txt")));  
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36,stopWords);


		IndexWriter indexWriter = new IndexWriter(ramDirectory, new IndexWriterConfig(Version.LUCENE_36, analyzer));
		for(URLContentObject d:urlContentList)
		{
			Document document = new Document();
			StringReader reader=new StringReader(d.getContent());
			document.add(new Field("id", d.getId(), Field.Store.YES, Field.Index.ANALYZED,TermVector.YES));
			document.add(new Field("contents", reader, TermVector.YES));
			//document.add(new Field("id",Integer.toString(d.getAutoIncrementId()),Field.Store.YES, Field.Index.ANALYZED));
			indexWriter.addDocument(document);
			reader.close();
		}

		indexWriter.commit();
		indexWriter.close();



			}


	/**public void generateTopKList(List<URLContentObject> uclist, String query,
			String sectionName) throws Exception
	{
		Directory ramDirectory=new RAMDirectory();
		createIndexWriter(uclist,ramDirectory);
		DocVector[] docs=readIndexGenerateSentenceVectors(ramDirectory);
		double[][] similarityMatrix=generateSimilarityMatrixForLexRank(docs);
		//System.out.println(similarityMatrix[1][2]);
		LexRankResults<DummyItem> results=generateLexRankScoresForSentences(similarityMatrix);
		Map<DummyItem, Double> scoresMap=results.scores;
		Map<Integer, Double> sortedMap =new HashMap<Integer, Double>();
		for (Map.Entry<DummyItem, Double> entry : scoresMap.entrySet()) {
			DummyItem key = entry.getKey();
			double value = entry.getValue();
			//System.out.println("ID:"+key.getId());
			sortedMap.put(key.getId(), value);

		}
		sortedMap=sortByValues(sortedMap);
		System.out.println(sortedMap);
		int max = Math.min(numSentencesInSummary, sortedMap.size()); // maximum index
		List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sortedMap.entrySet());
		List<Integer> finalKeys=new ArrayList<Integer>();
		for (Map.Entry<Integer, Double> entry : entryList) {
			Integer key = entry.getKey();
			// Display list of people in City
			finalKeys.add(key);
		}
		//System.out.println(uclist.size());
		//System.out.println(sortedMap.size());
		//System.out.println(finalKeys);
		writeWikiFormatSummary(getFinalSummaryContentList(uclist, finalKeys, query),sectionName);
	}

	
	public String generateSnippet(List<URLContentObject> uclist, String query,
			String sectionName) throws Exception
	{
		Directory ramDirectory=new RAMDirectory();
		createIndexWriter(uclist,ramDirectory);
		DocVector[] docs=readIndexGenerateSentenceVectors(ramDirectory);
		double[][] similarityMatrix=generateSimilarityMatrixForLexRank(docs);
		//System.out.println(similarityMatrix[1][2]);
		LexRankResults<DummyItem> results=generateLexRankScoresForSentences(similarityMatrix);
		Map<DummyItem, Double> scoresMap=results.scores;
		Map<Integer, Double> sortedMap =new HashMap<Integer, Double>();
		for (Map.Entry<DummyItem, Double> entry : scoresMap.entrySet()) {
			DummyItem key = entry.getKey();
			double value = entry.getValue();
			//System.out.println("ID:"+key.getId());
			sortedMap.put(key.getId(), value);

		}
		sortedMap=sortByValues(sortedMap);
		System.out.println(sortedMap);
		int max = Math.min(numSentencesInSummary, sortedMap.size()); // maximum index
		List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sortedMap.entrySet());
		List<Integer> finalKeys=new ArrayList<Integer>();
		for (Map.Entry<Integer, Double> entry : entryList) {
			Integer key = entry.getKey();
			// Display list of people in City
			finalKeys.add(key);
		}
		//System.out.println(uclist.size());
		//System.out.println(sortedMap.size());
		//System.out.println(finalKeys);
		return writeGeneralSummary(getFinalSummaryContentList(uclist, finalKeys, query),sectionName);
	}

	
	public String generateSnippetForStub(List<URLContentObject> uclist, String query,
			String sectionName) throws Exception
	{
		Directory ramDirectory=new RAMDirectory();
		createIndexWriter(uclist,ramDirectory);
		DocVector[] docs=readIndexGenerateSentenceVectors(ramDirectory);
		double[][] similarityMatrix=generateSimilarityMatrixForLexRank(docs);
		//System.out.println(similarityMatrix[1][2]);
		LexRankResults<DummyItem> results=generateLexRankScoresForSentences(similarityMatrix);
		Map<DummyItem, Double> scoresMap=results.scores;
		Map<Integer, Double> sortedMap =new HashMap<Integer, Double>();
		for (Map.Entry<DummyItem, Double> entry : scoresMap.entrySet()) {
			DummyItem key = entry.getKey();
			double value = entry.getValue();
			//System.out.println("ID:"+key.getId());
			sortedMap.put(key.getId(), value);

		}
		sortedMap=sortByValues(sortedMap);
		System.out.println(sortedMap);
		int max = Math.min(numSentencesInSummary, sortedMap.size()); // maximum index
		List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sortedMap.entrySet());
		List<Integer> finalKeys=new ArrayList<Integer>();
		for (Map.Entry<Integer, Double> entry : entryList) {
			Integer key = entry.getKey();
			// Display list of people in City
			finalKeys.add(key);
		}
		//System.out.println(uclist.size());
		//System.out.println(sortedMap.size());
		//System.out.println(finalKeys);
		return writeWikiFormatSummary(getFinalSummaryContentList(uclist, finalKeys, query),sectionName);
	}

	**/
	
	
	private static List<URLContentObject> getFinalSummaryContentList(List<URLContentObject> uclist,
			List<Integer> finalKeys, String query) throws IOException
			{
		List<URLContentObject> finalURLContent=new ArrayList<URLContentObject>();
		List<String> referenceList=new ArrayList<String>();
		for(URLContentObject uco:uclist)
		{
			if(finalURLContent.size()==numSentencesInSummary)
				break;
			if(finalKeys.contains(Integer.parseInt(uco.getId())))
			{
				double querySimilarity=CosineDocumentSimilarity.getCosineSimValue(query, uco.getContent());
				if(checkRedundantContent(finalURLContent,uco.getContent())!=1
						&& querySimilarity>=querySimilarityThreshold)
				{
					if(!referenceList.contains(uco.getURL()))
					{
						finalURLContent.add(uco);
						System.out.println(uco.getContent());
						referenceList.add(uco.getURL());
					}
				}
			}
		}

		return finalURLContent;
			}

	StanfordCoreNLP pipeline=null;
	public void loadStanfordComponents()
	{
		pipeline=URLCrawlerForTopic.loadCoreNLP();
	}
	
	/**private String writeWikiFormatSummary(List<URLContentObject> finalURLContent, String sectionHeader) throws Exception
	{
		StringBuilder sb=new StringBuilder();
		sb.append("=="+sectionHeader+"==\n");
		//sb.append(sectionHeader+"||");
		
		//SentenceCompressor sentenceCompressor = new SentenceCompressor();
		SimpleSummariser summariser=new SimpleSummariser();
		for(URLContentObject uc:finalURLContent)
		{
			String summary = summariser.summarise(uc.getContent(), 10);
			Annotation doc=new Annotation(summary);
			pipeline.annotate(doc);
			List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
			int max = Math.min(7, sentences.size()); 
			sentences=sentences.subList(0, max);
			for(CoreMap sentence:sentences)
			{
				String sentenceClean=sentence.toString().
						replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").
						replaceAll("[^A-Za-z0-9 ,-\\.]", "").replaceAll("\\s+"," ").trim();
				if(sentenceClean.toString().trim().length()<10
						|| sentenceClean.toString().trim().startsWith("[")
						|| sentenceClean.toString().trim().startsWith("("))
					continue;
				//System.out.println(sentenceClean.toString());
				//str.replaceAll("\\(.*?\\) ?", "");
				StringTokenizer st = new StringTokenizer(sentenceClean.toString()," ");
			    if(st.countTokens()<=7)
			    	continue;
				String compressed=SentenceCompressor.
						getCompressedSentence(sentenceClean.toString().
								replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "")).trim();
				compressed=compressed.replaceAll("-LRB-", "(").replaceAll("-RRB-", ")");
				compressed=compressed.replaceAll("-LSB-", "[").replaceAll("-RSB-", "]");
				compressed=compressed.replace(Pattern.quote(",\\."),".");
				compressed=compressed.replaceAll(", ,","");
				compressed=compressed.replaceAll("±", "");
				sb.append(compressed+" ");
			}
			if(sb.toString().trim().length()>40)
			{
				sb.append("<ref>"+uc.getURL()+"</ref>\n");
			}
		}

		return (sb.toString().trim());

	}**/
	
	/**private String writeGeneralSummary(List<URLContentObject> finalURLContent, String sectionHeader) throws Exception
	{
		StringBuilder sb=new StringBuilder();
		//sb.append("=="+sectionHeader+"==\n");
		sb.append(sectionHeader+"||");
		StanfordCoreNLP pipeline=URLCrawlerForTopic.loadCoreNLP();
		//SentenceCompressor sentenceCompressor = new SentenceCompressor();
		SimpleSummariser summariser=new SimpleSummariser();
		for(URLContentObject uc:finalURLContent)
		{
			String summary = summariser.summarise(uc.getContent(), 10);
			Annotation doc=new Annotation(summary);
			pipeline.annotate(doc);
			List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
			int max = Math.min(7, sentences.size()); 
			sentences=sentences.subList(0, max);
			for(CoreMap sentence:sentences)
			{
				String sentenceClean=sentence.toString().
						replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").trim();
				if(sentenceClean.toString().trim().length()<10
						|| sentenceClean.toString().trim().startsWith("[")
						|| sentenceClean.toString().trim().startsWith("("))
					continue;
				System.out.println(sentenceClean.toString());
				//str.replaceAll("\\(.*?\\) ?", "");
				String compressed=SentenceCompressor.
						getCompressedSentence(sentenceClean.toString().
								replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "")).trim();
				compressed=compressed.replaceAll("-LRB-", "(").replaceAll("-RRB-", ")");
				compressed=compressed.replaceAll("-LSB-", "[").replaceAll("-RSB-", "]");
				compressed=compressed.replace(Pattern.quote(",\\."),".");
				compressed=compressed.replaceAll(", ,","");
				sb.append(compressed+" ");
			}
			//sb.append("<ref>"+uc.getURL()+"</ref>\n");
		}

		return (sb.toString());

	}
**/
	private static int checkRedundantContent(List<URLContentObject> finalURLContent, String content) throws IOException
	{
		int indicator=0;
		for(URLContentObject uc:finalURLContent)
		{
			double cosSim=CosineDocumentSimilarity.getCosineSimValue(content, uc.getContent());
			if(cosSim>=0.7)
			{
				indicator=1;
				return indicator;
			}


		}
		return indicator;
	}

	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
		List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		//LinkedHashMap will keep the keys in the order they are inserted
		//which is currently sorted on natural ordering
		Map<K,V> sortedMap = new LinkedHashMap<K,V>();

		for(Map.Entry<K,V> entry: entries){
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	private LexRankResults<DummyItem> generateLexRankScoresForSentences(double[][] similarityMatrix)
	{
		List<DummyItem> items = new ArrayList<DummyItem>();
		for (int i = 0; i < similarityMatrix.length; ++i) {
			items.add(new DummyItem(i, similarityMatrix));
		}
		//        /85:0.005367600715029187
		//178:0.003496287668654751
		LexRankResults<DummyItem> results=LexRanker.rank(items, 0.1, true);
		//System.out.println("Here"+results.scores.get(items.get(179)));
		return results;

	}


	public DocVector[] readIndexGenerateSentenceVectors(Directory ramDirectory) throws Exception {
		IndexReader reader = IndexReader.open(ramDirectory);
		// first find all terms in the index
		Map<String,Integer> terms = new HashMap<String,Integer>();
		TermEnum termEnum = reader.terms(new Term("contents"));
		int pos = 0;
		while (termEnum.next()) {
			Term term = termEnum.term();
			if (! "contents".equals(term.field())) 
				break;
			terms.put(term.text(), pos++);
		}

		//System.out.println("Number of docs:"+reader.numDocs());

		List<Integer> docIds=new ArrayList<Integer>();
		for(int i=0;i<reader.maxDoc();i++)
		{
			docIds.add(i);

		}

		DocVector[] docs = new DocVector[docIds.size()];
		int i = 0;
		for (int docId : docIds) {
			TermFreqVector[] tfvs = reader.getTermFreqVectors(docId);
			//System.out.println(tfvs.length);
			docs[i] = new DocVector(terms); 

			for (TermFreqVector tfv : tfvs) {
				String[] termTexts = tfv.getTerms();
				int[] termFreqs = tfv.getTermFrequencies();
				for (int j = 0; j < termTexts.length; j++) {
					double idfValue=getIDF(reader,termTexts[j]);
					double tfIdfValue=termFreqs[j]*idfValue;
					docs[i].setEntry(termTexts[j], tfIdfValue);
				}
			}
			docs[i].normalize();
			i++;
		}


		reader.close();
		return docs;

	}

	private double[][] generateSimilarityMatrixForLexRank(DocVector[] docs)
	{
		double[][] similarityMatrix = new double[docs.length][docs.length];
		for(int j=0;j<docs.length;j++)
		{
			for(int k=0;k<docs.length;k++)
			{

				similarityMatrix[j][k]=getCosineSimilarity(docs[j], docs[k]); 

			}


		}


		return similarityMatrix;
	}



	private double getIDF(IndexReader reader, String termName) throws IOException
	{
		return 1+Math.log(reader.numDocs()/ ((double)reader.docFreq(new Term("contents",termName))+1));
	}

	public static double getCosineSimilarity(DocVector d1, DocVector d2) {
		return (d1.vector.dotProduct(d2.vector)) /
				(d1.vector.getNorm() * d2.vector.getNorm());
	}



	public static List<URLContentObject> attachIds(List<URLContentObject> urlContentList)
	{
		List<URLContentObject> modurlContList=new ArrayList<URLContentObject>();
		for(URLContentObject urlContent:urlContentList)
		{
			URLContentObject newContent=urlContent;
			newContent.setId(Integer.toString(urlContentList.indexOf(urlContent)));
			modurlContList.add(newContent);

		}

		return modurlContList;

	}


}
