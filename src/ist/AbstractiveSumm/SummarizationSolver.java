package ist.AbstractiveSumm;

//import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import edu.sidd.Objects.ListPathObject;
import edu.sidd.Objects.PathObject;
import edu.sidd.Objects.URLContentObject;
import edu.sidd.StubImprover.WikiHelper;
import edu.sidd.crawlXML.URLCrawlerForTopic;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import ist.common.vector.ClusterReader;
import ist.common.vector.CosineDocumentSimilarity;
import ist.common.vector.DocVector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;


import junit.framework.Assert;

import org.apache.commons.io.FileUtils;


public class SummarizationSolver {

	private static final double MIN_COSINE_SIM_THRESHOLD = 0.2;
	private static final double INIT_LIST_SIM_THRESHOLD = 0.9;
	private static final int MAX_COMMAS = 4;

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws GRBException 
	 */
//	private void formulateProblem(LMReader lmReader) throws IOException, InterruptedException, GRBException
//	{
//		ClusterReader cr=new ClusterReader();
//		FileUtils.deleteQuietly(new File(shortestPathDir));
//		Thread.sleep(5000);
//		new File(shortestPathDir).mkdir();
//		List<String> clusterFilePaths=cr.getClusterFiles(generatedClusters);
//		for(String s:clusterFilePaths)
//		{
//			cr.createShortestPaths(s, Integer.toString(clusterFilePaths.indexOf(s)));
//		}
//		Map<String, List<String>> shortestPathMap=cr.readShortestPaths(shortestPathDir);
//		System.out.println(shortestPathMap.size());
//		TreeMap<String, String> fileSentences=new TreeMap<String, String>();
//		Map<String, Integer> docToIndexMapper=new HashMap<String, Integer>();
//		int index=0;
//		for (Map.Entry<String, List<String>> entry : shortestPathMap.entrySet()) 
//		{
//			List<String> candidates=entry.getValue();
//			for(String s:candidates)
//			{
//				fileSentences.put(entry.getKey()+"~"+candidates.indexOf(s), s);
//				//System.out.println(entry.getKey()+"~"+candidates.indexOf(s));
//				docToIndexMapper.put(entry.getKey()+"~"+candidates.indexOf(s), index);
//				index++;
//			}
//		}
//
//		System.out.println(index);
//		//DocVector[] docsLucene=CosineDocumentSimilarity.getCosineSimilarityMatrix(fileSentences);
//		//System.out.println("Number of docs:"+docsLucene.length);
//		//createILPProblem(shortestPathMap,lmReader, docsLucene);
//
//	}

	public static StanfordCoreNLP loadCoreNLP()
	{
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos");//, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		return pipeline;
	}

	static StanfordCoreNLP pipeline=null;
	public static void loadStanfordComponents()
	{
		pipeline=loadCoreNLP();
	}
	
	static LMReader lMReader=null;
	public static void loadLM() throws IOException
	{
		lMReader=new LMReader();
		lMReader.readLanguageModel();
	}
	
	public static String getFirstSentence(String introString)
	{
		Annotation document = new Annotation(introString);
		// run all Annotators on this text
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		return sentences.get(0).toString();
		
	}

	public static String generateSectionSummary(List<URLContentObject> urlContentObjectList,
			String query) throws IOException, InterruptedException, GRBException
	{
		//first for each section generate shortest paths
		List<String> sentenceList=new ArrayList<String>();
		List<String> urlList=new ArrayList<String>();
		StringBuffer safeString=new StringBuffer();
		for(URLContentObject uco:urlContentObjectList)
		{
			Annotation document = new Annotation(uco.getContent());
			// run all Annotators on this text
			pipeline.annotate(document);
			// these are all the sentences in this document
			// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			for(CoreMap sentence: sentences)
			{
				StringBuffer sentenceString=new StringBuffer();
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					// this is the text of the token
					String word = token.get(TextAnnotation.class);
					if(word.startsWith("-"))
						continue;
					// this is the POS tag of the token
					String pos = token.get(PartOfSpeechAnnotation.class);
					sentenceString.append(word+"/"+pos+" ");
				}
				safeString.append(sentence.toString()+" ");
				sentenceList.add(sentenceString.toString().trim());
			}
			//sentenceList.add(uco.getContent());
			urlList.add(uco.getURL());
		}

		System.err.println(sentenceList.size() + " content retained......");
		//write sentences POS Tagged
		File f=new File("lib/MSC/out/section_sentences_tagged.txt");
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
		for(String s:sentenceList)
		{
			bw.write(s+"\n");

		}
		bw.close();
		//System.out.println(f.getPath());
		createShortestPaths(f.getPath());
		
		//createSentenceImportances();
		List<String> shortestPaths=readShortestPaths("lib\\MSC\\out\\shortestPaths.txt");
		int initSize=shortestPaths.size();
		if(shortestPaths.size()==0)
			return safeString.toString();
		//List<Double> trScores=readSentenceScores("lib\\MSC\\out\\scoresTextRank.txt");
		List<Double> pathScores=generateTFIDFscores(shortestPaths, query);
		shortestPaths=filterPaths(pathScores,shortestPaths);
		System.err.println("Number of zero paths:"+shortestPaths.size()+" out of "+initSize);
		pathScores=generateTFIDFscores(shortestPaths, query);
		List<PathObject> pathObjectList=createPathObjectWithScores(shortestPaths,pathScores);
		pathObjectList=sortPathObjects(pathObjectList);
//		System.out.println("Initial paths:"+pathObjectList.size());
		List<String> finalPaths=obtainNonRedundantSet(pathObjectList);
		//filter shortest paths
		generateFinalShortPaths(finalPaths);
		pathScores=generateTFIDFscores(finalPaths, query);
		createTRScores();
		//List<Double> trScores=readSentenceScores("lib\\MSC\\out\\scoresTextRank.txt");
//		if(shortestPaths.size()==0)
//			return safeString.toString();
//		Assert.assertEquals(shortestPaths.size(), pathScores.size());
//		List<PathObject> pathObjectList=createPathObjectWithScores(shortestPaths,pathScores);
//		pathObjectList=sortPathObjects(pathObjectList);
//		System.out.println("Initial paths:"+pathObjectList.size());
//		List<String> finalPaths=obtainNonRedundantSet(pathObjectList);
//		System.out.println("Remaining paths:"+finalPaths.size());
//		createFinalPathsImportances();
		DocVector[] docsLucene=CosineDocumentSimilarity.getCosineSimilarityMatrix(finalPaths);
		//System.out.println("Matrix:"+docsLucene);
		//String summaryString=createILPProblem(finalPaths, lMReader, docsLucene);
		//####Trying global and local informativeness
		String summaryString="";
		//String summaryString=createILPProblem(finalPaths, lMReader, docsLucene, pathScores);
		//System.out.println("Summary:"+summaryString);
		return summaryString;
	}
	
	
	public static String generateSectionSummaryWithAllPaths(List<URLContentObject> urlContentObjectList,
			String query) throws IOException, InterruptedException, GRBException
	{
		//first for each section generate shortest paths
		List<String> sentenceList=new ArrayList<String>();
		List<String> urlList=new ArrayList<String>();
		StringBuffer safeString=new StringBuffer();
		List<String> origList=new ArrayList<String>();
		for(URLContentObject uco:urlContentObjectList)
		{
			Annotation document = new Annotation(uco.getContent());
			// run all Annotators on this text
			pipeline.annotate(document);
			// these are all the sentences in this document
			// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			for(CoreMap sentence: sentences)
			{
				StringBuffer sentenceString=new StringBuffer();
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					// this is the text of the token
					String word = token.get(TextAnnotation.class);
					if(word.startsWith("-L")||word.startsWith("-R"))
						continue;
					// this is the POS tag of the token
					String pos = token.get(PartOfSpeechAnnotation.class);
					sentenceString.append(word+"/"+pos+" ");
				}
				safeString.append(sentence.toString()+" ");
				sentenceList.add(sentenceString.toString().trim());
				origList.add(sentence.toString());
			}
			//sentenceList.add(uco.getContent());
			urlList.add(uco.getURL());
		}

		System.err.println(sentenceList.size() + " content retained......");
		//write sentences POS Tagged
		File f=new File("lib/MSC/out/section_sentences_tagged.txt");
		f.createNewFile();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
		for(String s:sentenceList)
		{
			bw.write(s+"\n");
		}
		bw.close();
		//System.out.println(f.getPath());
		createPathsandTxtRankScores(f.getPath());
		
		//createSentenceImportances();
		List<String> shortestPaths=readShortestPaths("lib\\MSC\\out\\shortestPaths.txt");
		int initSize=shortestPaths.size();
		if(initSize==0)
			return safeString.toString();
		List<Double> trScores=readSentenceScores("lib\\MSC\\out\\scoresTextRank.txt");
		List<Double> tfIdfScores=generateTFIDFscores(shortestPaths, query);
		List<Integer> pathsToRemove=identifyVeryCloseSentencesToOrig(origList, shortestPaths);
		ListPathObject lpo=getFinalPathObjectList(tfIdfScores, shortestPaths, trScores, pathsToRemove);
		shortestPaths=lpo.getPathList();
		trScores=lpo.getTxRankScoreList();
		tfIdfScores=lpo.getTfIdfscoreList();
		
		System.out.println(shortestPaths.size()+"/"+trScores.size()+"/"+tfIdfScores.size());
		
		//shortestPaths=filterPaths(tfIdfScores,shortestPaths);
		//System.err.println("Number of zero paths:"+shortestPaths.size()+" out of "+initSize);
		//pathScores=generateTFIDFscores(shortestPaths, query);
		//List<PathObject> pathObjectList=createPathObjectWithScores(shortestPaths,pathScores);
		//pathObjectList=sortPathObjects(pathObjectList);
//		System.out.println("Initial paths:"+pathObjectList.size());
		//List<String> finalPaths=obtainNonRedundantSet(pathObjectList);
		//filter shortest paths
		//generateFinalShortPaths(finalPaths);
		//pathScores=generateTFIDFscores(finalPaths, query);
		//createTRScores();
		DocVector[] docsLucene=CosineDocumentSimilarity.getCosineSimilarityMatrix(shortestPaths);
		//System.out.println("Matrix:"+docsLucene);
		//String summaryString=createILPProblem(finalPaths, lMReader, docsLucene);
		//####Trying global and local informativeness
		String summaryString=createILPProblem(shortestPaths, lMReader, docsLucene, tfIdfScores, trScores);
		//System.out.println("Summary:"+summaryString);
		return summaryString;
	}
	
	
	
	private static void generateFinalShortPaths(List<String> finalPaths) throws IOException
	{
		File f=new File("lib\\MSC\\out\\shortestPaths.txt");
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
		for(String s:finalPaths)
		{
			bw.write(s+"\n");
		}
		bw.close();		
	
	}
	
	private static List<String> filterPaths(List<Double> pathScores,List<String> shortestPaths)
	{
		//List<Integer> list=new ArrayList<Integer>();
		List<String> finalshortestPaths=new ArrayList<String>();
		for(Double d:pathScores)
		{
			if(d.doubleValue()>0.0)
			{
				finalshortestPaths.add(shortestPaths.get(pathScores.indexOf(d)));
			}
			
		}
		return finalshortestPaths;
	}
	
	
	private static ListPathObject 
			getFinalPathObjectList(List<Double> tfIdfScores,List<String> shortestPaths, 
					List<Double> trScores, List<Integer> pathsToRemove)
	{
		//List<Integer> list=new ArrayList<Integer>();
		ListPathObject lpo=new ListPathObject();
		List<String> finalshortestPaths=new ArrayList<String>();
		List<Double> finalTfIdfScores=new ArrayList<Double>();
		List<Double> txRankScores=new ArrayList<Double>();
		int i=0;
		for(Double d:tfIdfScores)
		{
			if(d.doubleValue()>0.0 && !pathsToRemove.contains(i))
			{
				finalshortestPaths.add(shortestPaths.get(i));
				finalTfIdfScores.add(d);
				txRankScores.add(trScores.get(i));
			}
			i++;
			
		}
		lpo.setPathList(finalshortestPaths);
		lpo.setTfIdfscoreList(finalTfIdfScores);
		lpo.setTxRankScoreList(txRankScores);
		return lpo;
	}
	
	private static List<PathObject> createPathObjectWithScores(List<String> shortestPaths,List<Double> pathScores)
	{
		List<PathObject> pathObjectList=new ArrayList<PathObject>();
		int size=shortestPaths.size();
		for(int i=0;i<size;i++)
		{
			PathObject po=new PathObject();
			po.setScore(pathScores.get(i));
			po.setSentence(shortestPaths.get(i));
			pathObjectList.add(po);
		}
		return pathObjectList;
	}
	
	private static List<Integer> identifyVeryCloseSentencesToOrig(List<String> origList,List<String> shortestPaths) throws IOException
	{
		List<Integer> indicesClose=new ArrayList<Integer>();
		for(String s:shortestPaths)
		{
			int indicator=0;
			for(String orig:origList)
			{
				double sim=CosineDocumentSimilarity.getCosineSimValue(s, orig);
				if(sim>=INIT_LIST_SIM_THRESHOLD)
				{
					indicator=1;
					break;
					
				}
				
			}
			if(indicator==1)
			{
				indicesClose.add(shortestPaths.indexOf(s));
			}
			
			
		}
		
		return indicesClose;
		
		
		
	}
	
	
	
	
	private static List<Double> generateTFIDFscores(List<String> shortestPaths, String query) throws IOException
	{
		List<Double> tfIDFscores=new ArrayList<Double>();
		for(String s:shortestPaths)
		{
			
			tfIDFscores.add(CosineDocumentSimilarity.getCosineSimValue(s, query));
		}
		
		return tfIDFscores;
	}
	
	private static List<PathObject> sortPathObjects(List<PathObject> pathObjList)
	{
		Collections.sort(pathObjList, new Comparator<PathObject>() {

			@Override
			public int compare(PathObject arg0, PathObject arg1) {
				// TODO Auto-generated method stub
				if(arg0.getScore()<arg1.getScore())
				return 1;
				else
				{
					return -1;
				}
			}

		});
		return pathObjList;
	}
	
	
	private static List<String> obtainNonRedundantSet(List<PathObject> sortedPathObjList) throws IOException
	{
		List<String> finalSentenceList=new ArrayList<String>();
		List<String> allSentenceList=new ArrayList<String>();
		for(PathObject p:sortedPathObjList)
		{
			allSentenceList.add(p.getSentence());
		}
			//System.out.println(p.getScore()+"/"+p.getSentence());
		for(String s:allSentenceList)
		{
			if(finalSentenceList.size()==0)
				{
					finalSentenceList.add(s);
					continue;
				}
			List<String> tempFinal=new ArrayList<String>();
			tempFinal.add(s);
			tempFinal.addAll(finalSentenceList);
			//System.out.println("Tempfinal:"+tempFinal.size());
			DocVector[] simMatrix=CosineDocumentSimilarity.getCosineSimilarityMatrix(tempFinal);
			int indicator=0;
			for(int j=1;j<tempFinal.size();j++)
			{
				double simValue=CosineDocumentSimilarity.
						calcCosineSimilarity(simMatrix[0],simMatrix[j]);
				//System.out.println("SimilarityValue~~"+simValue+"/"+s+"||"+finalSentenceList.get(j));
				if(simValue>=INIT_LIST_SIM_THRESHOLD)
				{	
					indicator =1;
					break;
				}
			}
			
			if(indicator!=1)
			{
				finalSentenceList.add(s);
			}
			//i++;
		}
		
		File f=new File("lib/MSC/out/finalSentences.txt");
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
		for(String s:finalSentenceList)
		{
			bw.write(s+"\n");
		}
		bw.close();		
		return finalSentenceList;
	}
	
	
	
	
	public static void main(String[] args) {
		PathObject p1=new PathObject();
		p1.setScore(0.22399d);
		PathObject p2=new PathObject();
		p2.setScore(0.22444d);
		List<PathObject> pobjlist=new ArrayList<PathObject>();
		pobjlist.add(p1);
		pobjlist.add(p2);
		pobjlist=sortPathObjects(pobjlist);
		System.out.println(pobjlist.get(0).getScore());
		
		
		
	}
	
	
	
	public static List<String> readShortestPaths(String file) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		List<String> sentenceList=new ArrayList<String>();
		String sCurrentLine="";
		while ((sCurrentLine = br.readLine()) != null) {
			int commas = 0;
			for(int i = 0; i < sCurrentLine.length(); i++) {
				if(sCurrentLine.charAt(i) == ':') commas++;
				if(sCurrentLine.charAt(i) == '\'') commas++;
				if(sCurrentLine.charAt(i) == '.') commas++;
				if(sCurrentLine.charAt(i) == ';') commas++;
				if(sCurrentLine.charAt(i) == ',') commas++;
			}
			if(commas<=MAX_COMMAS && sCurrentLine.toLowerCase().matches("^[a-z0-9].*$"))
				{
					sentenceList.add(sCurrentLine);
				}
		}
		br.close();
		
		return sentenceList;
	}

	public static List<Double> readSentenceScores(String file) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		List<Double> scoreList=new ArrayList<Double>();
		String sCurrentLine="";
		while ((sCurrentLine = br.readLine()) != null) {
				{
				scoreList.add(Double.parseDouble(sCurrentLine));
				}
		}
		br.close();
		
		return scoreList;
	}
	
	public static void createPathsandTxtRankScores(String taggedFilePath) throws IOException, InterruptedException
	{
		//String minWordsForGraph=Integer.toString(minWordsForWordGraph);
		ProcessBuilder processBuilder = new ProcessBuilder(WikiHelper.getSpecificProperty("python2dir"),
				"lib\\MSC\\GenerateLocScores.py",taggedFilePath);
		Process p = processBuilder.start();
		p.waitFor(); //wait for python to end
		p.destroy();//KILL the process
	}
	
	

		public static void createShortestPaths(String taggedFilePath) throws IOException, InterruptedException
		{
			//String minWordsForGraph=Integer.toString(minWordsForWordGraph);
			ProcessBuilder processBuilder = new ProcessBuilder(WikiHelper.getSpecificProperty("python2dir"),
					"lib\\MSC\\GenerateShortestPaths.py",taggedFilePath);
			Process p = processBuilder.start();
			p.waitFor(); //wait for python to end
			p.destroy();//KILL the process
		}



		private static void createFinalPathsImportances() throws IOException, InterruptedException
		{

			ProcessBuilder processBuilder = new ProcessBuilder(WikiHelper.getSpecificProperty("python3dir"),
					"lib\\MSC\\TextRanker.py","lib\\MSC\\out\\finalSentences.txt");
			Process p = processBuilder.start();
			p.waitFor(); //wait for python to end
			p.destroy();//KILL the process


		}


		private static void createTRScores() throws IOException, InterruptedException
		{

			ProcessBuilder processBuilder = new ProcessBuilder(WikiHelper.getSpecificProperty("python3dir"),
					"lib\\MSC\\TextRanker.py","lib\\MSC\\out\\shortestPaths.txt");
			Process p = processBuilder.start();
			p.waitFor(); //wait for python to end
			p.destroy();//KILL the process


		}



//	private void generateSummaries(LMReader lmReader, String mainDocTechnique,int rankingTechnique) throws Exception
//	{
//		SentenceClusterCreator scc=new SentenceClusterCreator();
//		//	scc.generateClusters(FilesDir+"/"+TOP_K_VALUE+"_K_TOP_SENTENCES");
//		FileUtils.deleteQuietly(new File(clustersDir));
//		Thread.sleep(5000);
//		new File(clustersDir).mkdir();
//		scc.generateClusters(fullTextDir, clustersDir, 3, mainDocTechnique, rankingTechnique);
//		String resultsDir="Results/"+"summaries_"+mainDocTechnique+"_"+rankingTechnique;
//		new File(resultsDir).mkdir();
//		File[] docFolders=new File(clustersDir).listFiles();
//		for(File f:docFolders)
//		{
//			//if(!f.getName().contentEquals("d31038t"))
//			//	continue;
//			ClusterReader cr=new ClusterReader();
//			FileUtils.deleteQuietly(new File(shortestPathDir));
//			Thread.sleep(2000);
//			new File(shortestPathDir).mkdir();
//			List<String> clusterFilePaths=cr.getClusterFiles(f.getAbsolutePath());
//			for(String s:clusterFilePaths)
//			{
//				cr.createShortestPaths(s, Integer.toString(clusterFilePaths.indexOf(s)));
//			}
//			TreeMap<String, List<String>> shortestPathMap=cr.readShortestPaths(shortestPathDir);
//			System.out.println(shortestPathMap.size());
//			TreeMap<String, String> fileSentences=new TreeMap<String, String>();
//			Map<String, Integer> docToIndexMapper=new HashMap<String, Integer>();
//			int index=0;
//			for (Map.Entry<String, List<String>> entry : shortestPathMap.entrySet()) 
//			{
//				if(entry.getValue().size()==0)
//					continue;
//				List<String> candidates=entry.getValue();
//
//				for(String s:candidates)
//				{
//					fileSentences.put(entry.getKey()+"~"+candidates.indexOf(s), s);
//					//System.out.println(entry.getKey()+"~"+candidates.indexOf(s));
//					docToIndexMapper.put(entry.getKey()+"~"+candidates.indexOf(s), index);
//					index++;
//				}
//			}
//
//			System.out.println(index);
//			DocVector[] docsLucene=CosineDocumentSimilarity.getCosineSimilarityMatrix(fileSentences);
//			System.out.println("Number of docs:"+docsLucene.length);
//			createILPProblem(shortestPathMap,lmReader, docsLucene, resultsDir+"/"+f.getName()+".txt");
//		}
//	}



	private static List<Double> getTextRankScores() throws NumberFormatException, IOException
	{
		File f=new File("lib\\MSC\\out\\scoresTextRank.txt");
		String sCurrentLine;
		BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
		List<Double> scores=new ArrayList<Double>();
		while ((sCurrentLine = br.readLine()) != null) {
			scores.add(Double.parseDouble(sCurrentLine));
		}
		br.close();
		return scores;
	}

	//static LexicalizedParser dependencyParser=null;

//	public static void main(String[] args) throws Exception {
//		// TODO Auto-generated method stub
//		SummarizationSolver ss=new SummarizationSolver();
//		LMReader lmreader=new LMReader();
//		lmreader.readLanguageModel();
//		//dependencyParser = LexicalizedParser.loadModel();
//		//ss.generateSummaries(lmreader,"COSINEAVERAGE",1);
//		//ss.generateSummaries(lmreader,"LEXRANK",1);
//		//ss.generateSummaries(lmreader,"DOCSET",1);
//		//ss.generateSummaries(lmreader,"COSINEAVERAGE",2);
//		//ss.generateSummaries(lmreader,"LEXRANK",2);
//		//ss.generateSummaries(lmreader,"DOCSET",2);
//	}

	private static String createILPProblem(List<String> shortestPaths,
			LMReader lmReader,
			DocVector[] docsLucene, List<Double> tfIdfScores,
			List<Double> trScores) throws GRBException, IOException, InterruptedException
			{
		GRBEnv    env   = new GRBEnv("mip1.log");
		env.set(GRB.DoubleParam.NodefileStart, 0.2);
		GRBModel  model = new GRBModel(env);
		
		// Create variables
		List<GRBVar> variableList=new ArrayList<GRBVar>();
		List<String> allShortestPaths=new ArrayList<String>();
		for (String s:shortestPaths) {
				GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY,  "Path_"+shortestPaths.indexOf(s));
				variableList.add(x);
				allShortestPaths.add(s);
			}
		model.update();
		//Objective function
		GRBLinExpr objFunction= new GRBLinExpr();
		for (String s:shortestPaths) {
			double lmValue=lmReader.getSentenceLogProb(s);
				//System.out.println(s);
			GRBVar p=getVariable(variableList,  "Path_"+shortestPaths.indexOf(s));
			double informationValue=trScores.get(shortestPaths.indexOf(s));
			double length=new StringTokenizer(s, " ").countTokens();
			double TFIDFscore=tfIdfScores.get(shortestPaths.indexOf(s));
				//objFunction.addTerm(length*informationValue*lmValue, p); 
				//double logLikelihoodParser=dependencyParser.parse(s).score();
				//double score=1/(1-logLikelihoodParser);

				//objFunction.addTerm(informationValue*lmValue*(1/length), p); 
			objFunction.addTerm(TFIDFscore*informationValue*lmValue, p); 
			//objFunction.addTerm(TFIDFscore*lmValue, p); 
				//System.out.println(randomValue);
		}
		model.update();
		model.setObjective(objFunction, GRB.MAXIMIZE);

		/**
		 * CONSTRAINTS
		 */
		GRBLinExpr lconstraint=new GRBLinExpr();
		for (String s:shortestPaths) {
			 //1. Max N sentences in summary
			
				GRBVar p=getVariable(variableList, "Path_"+shortestPaths.indexOf(s));
				lconstraint.addTerm(1.0, p);
			}
		model.addConstr(lconstraint, GRB.LESS_EQUAL, 10.0, "lmax_constraint"); 

		//System.out.println("Num constraints 1:"+model.getConstrs().length);

		//Redundancy constraint with threshold
		for(int i=0;i<docsLucene.length;i++)
		{
			for(int j=0;j<docsLucene.length;j++)
			{
				if(i==j) continue;
				double sim=CosineDocumentSimilarity.calcCosineSimilarity(docsLucene[i], docsLucene[j]);
				//System.err.println("Similarity value:"+sim);
				if(sim>=MIN_COSINE_SIM_THRESHOLD)
				{
					GRBLinExpr redundancyConstraint=new GRBLinExpr();
					//System.err.println("Similarity:"+i+","+j);
					GRBVar p1=variableList.get(i);
					GRBVar p2=variableList.get(j);
					redundancyConstraint.addTerm(1.0, p1);
					redundancyConstraint.addTerm(1.0, p2);
					//System.out.println(allShortestPaths.get(i)+"||"+allShortestPaths.get(j));
					model.addConstr(redundancyConstraint, GRB.LESS_EQUAL, 1.0, "redundancy~"+i+":"+j);
				}

			}
		}

		//prevent longer sentences
		for (String s:shortestPaths) {
			
				GRBVar p=getVariable(variableList, "Path_"+shortestPaths.indexOf(s));
				StringTokenizer st=new StringTokenizer(s);
				GRBLinExpr slconstraint=new GRBLinExpr();
				slconstraint.addTerm(st.countTokens(), p);
				model.addConstr(slconstraint, GRB.LESS_EQUAL, 24.0, "length_cons");
			

		}

		model.optimize();
		List<GRBVar> ilpOut=new ArrayList<GRBVar>();
		for(GRBVar g:variableList)
		{
			//System.out.println(g.get(GRB.StringAttr.VarName)
			//		+ " " +g.get(GRB.DoubleAttr.X));
			if(g.get(GRB.DoubleAttr.X)==1.0)
			{
				ilpOut.add(g);
			}

		}
		
		StringBuffer sb=new StringBuffer();
		for (String s:shortestPaths) {
				GRBVar x = getVariable(variableList, "Path_"+shortestPaths.indexOf(s));
				if(ilpOut.contains(x))
				{
					String replaced=s.replaceAll("-LRB-", "(").replaceAll("-RRB-", ")");
					replaced=replaced.trim();
					replaced=Character.toUpperCase(replaced.charAt(0)) + replaced.substring(1);
					sb.append(replaced+" ");
				}
		}
		
		model.dispose();
		return sb.toString();

			}

	private static GRBVar getVariable(List<GRBVar> variableList,String key) throws GRBException
	{
		for(GRBVar g:variableList)
		{
			String varName=g.get(GRB.StringAttr.VarName);
			if(key.contentEquals(varName))
			{
				return g;
			}
		}

		return null;
	}
}
