package ist.common.vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;




public class ClusterReader {

	static String generatedClusters="files/inputDir/SampleDataset/clustersGenerated";
	static String shortestPathDir="files/MSC/out";
	static int minWordsForWordGraph=8;
	private Map<String, List<String>> readClusterData(String dir) throws IOException
	{
		File[] clusterSentenceFiles=new File(dir).listFiles();
		Map<String, List<String>> clusterSentencesMap=new TreeMap<String, List<String>>();
		for(File f:clusterSentenceFiles)
		{
			String sCurrentLine;
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
			List<String> sentenceList=new ArrayList<String>();
 			while ((sCurrentLine = br.readLine()) != null) {
 				sentenceList.add(sCurrentLine);
			}
			br.close();
			clusterSentencesMap.put(f.getName(),sentenceList);
		}
		
		return clusterSentencesMap;
	}
	
	public List<String> getClusterFiles(String dir) throws IOException
	{
		File[] clusterSentenceFiles=new File(dir).listFiles();
		List<String> clusterFilePaths=new ArrayList<String>();
		for(File f:clusterSentenceFiles)
		{
			clusterFilePaths.add(f.getAbsolutePath());
		}
		
		return clusterFilePaths;
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub

		ClusterReader cr=new ClusterReader();
		List<String> clusterFilePaths=cr.getClusterFiles(generatedClusters);
		for(String s:clusterFilePaths)
		{
			cr.createShortestPaths(s, Integer.toString(clusterFilePaths.indexOf(s)));
		}
		Map<String, List<String>> shortestPathMap=cr.readShortestPaths(shortestPathDir);
		System.out.println(shortestPathMap.size());
	}
	
	
	public static void createShortestPaths(String clusterFilePath, String number) throws IOException, InterruptedException
	{
		String minWordsForGraph=Integer.toString(minWordsForWordGraph);
		ProcessBuilder processBuilder = new ProcessBuilder("C:\\Python27\\python.exe",
				"files\\MSC\\GenerateShortestPaths.py",clusterFilePath,number);
		Process p = processBuilder.start();
		p.waitFor(); //wait for python to end
		p.destroy();//KILL the process
	}

	public TreeMap<String, List<String>> readShortestPaths(String dir) throws IOException
	{
		File[] clusterSentenceFiles=new File(dir).listFiles();
		TreeMap<String, List<String>> clusterSentencesMap=new TreeMap<String, List<String>>();
		for(File f:clusterSentenceFiles)
		{
			String sCurrentLine;
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
			List<String> sentenceList=new ArrayList<String>();
 			while ((sCurrentLine = br.readLine()) != null) {
 				sentenceList.add(sCurrentLine);
			}
			br.close();
			clusterSentencesMap.put(f.getName(),sentenceList);
		}
		
		return clusterSentencesMap;
	}
	
	public static void generateCosineSimMatrix(Map<String, List<String>> shortestPathMap) throws IOException
	{
		Map<String, String> fileSentences=new TreeMap<String, String>();
		for (Map.Entry<String, List<String>> entry : shortestPathMap.entrySet()) 
		{
			List<String> candidates=entry.getValue();
			for(String s:candidates)
			{
				fileSentences.put(entry.getKey()+"~"+candidates.indexOf(s), s);
			}
		}
		CosineDocumentSimilarity cds=new CosineDocumentSimilarity();
		Map<String, Double> cosineVal=new HashMap<String, Double>();
		int i=0;
		for(Map.Entry<String, String> entry1 : fileSentences.entrySet()) 
		{
			int j=0;
			for(Map.Entry<String, String> entry2 : fileSentences.entrySet()) 
			{
				double sim=cds.getCosineSimilarity(entry1.getValue(), entry2.getValue());
				cosineVal.put(entry1.getKey()+"~"+i+"||"+entry2.getKey()+"~"+j, sim);
				j++;
			}
			System.err.println("Done with "+entry1.getKey());
			i++;
			
		}
		
		System.out.println("Size of matrix:"+cosineVal.size());
	}
	
	
	
	
}
