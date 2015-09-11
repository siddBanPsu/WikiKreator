package edu.sidd.StubImprover;

import ist.AbstractiveSumm.SummarizationSolver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import weka.classifiers.trees.RandomForest;
import cc.mallet.topics.TopicInferencer;

import jtopia.Configuration;
import jtopia.JtopiaUsage;


import edu.sidd.Objects.URLContentObject;
import edu.sidd.TopicVectors.PassageClassifier;
import edu.sidd.TopicVectors.TopicModelGenerator;
import edu.sidd.crawlXML.URLCrawlerForTopic;
import edu.sidd.crawlXML.XMLWikipediaExtractor;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class ExistingArticleGenerator {

	static String dirForWikiDumps="inputFiles/rawFiles/categoryXML";
	static String dirForProcessedWikiXml="inputFiles/rawFiles/processedXML";
	static String urlStoreDir="inputFiles/rawFiles/urlStore";
	static String dirForProcessedWikiTxt="inputFiles/rawFiles/processedTxt";
	static String resultsDir="Results/GeneratedOutput";

	private void chooseRandomArticlesToCreate(String categoryName,
			int bestNumTopic, TopicInferencer inferencer, RandomForest classifier) throws Exception
			{
		File[] getAllFiles=new File(dirForProcessedWikiTxt+"/"+categoryName+"-txt").listFiles();
		//System.out.println(getAllFiles.length);
		int numGen=Integer.parseInt(WikiHelper.getSpecificProperty("numFilesToGenerate"));
		System.out.println("Value of numgen:"+numGen);
		//System.exit(0);
		if(getAllFiles.length<numGen)
		{
			System.err.println("Num of articles in category is less than the number of articles you asked to generate");
			numGen=getAllFiles.length;
		}

		File outFolder=new File(resultsDir+"/"+categoryName);
		FileUtils.deleteQuietly(outFolder);
		Thread.sleep(2000);
		outFolder.mkdirs();
		Random t = new Random();

		List<Integer> randomList=new ArrayList<Integer>();
		for (int c = 1; c <= 1000; c++) {
			//if(randomList.size()==numGen)
			//	break;
			int randomNum=t.nextInt(getAllFiles.length-1);
			System.err.println(randomNum);
			if(!randomList.contains(randomNum))
				randomList.add(randomNum);
			//System.out.println(t.nextInt(getAllFiles.length));
		}

		//System.out.println("RandomList:"+randomList);
		List<File> selectedFiles=new ArrayList<File>();
		//selectedFiles.add(new File(dirForProcessedWikiTxt+"/"+categoryName+"-txt"+"/"+"Dermatochalasis.txt"));

		//Uncomment when not running single files
		for(Integer i:randomList)
		{
			//System.out.println(i);
			System.out.println(selectedFiles.size()+"/"+numGen);
			if(selectedFiles.size()==numGen)
			{	
				break;
			}

			if(getAllFiles[i].exists())
			{
				//System.out.println(i+"/"+getAllFiles[i].getAbsolutePath());
				int lines=Files.readAllLines(Paths.get(getAllFiles[i].getAbsolutePath()),
						Charset.defaultCharset()).size();
				if(lines>=5 && !getAllFiles[i].getName().toLowerCase().contains("list"))
				{
					selectedFiles.add(getAllFiles[i]);
				}

			}

		}

		System.err.println("Selected Files Size:"+selectedFiles.size());

		new File(outFolder+"/"+"models").mkdirs();
		new File(outFolder+"/"+"system").mkdirs();
		for(File f:selectedFiles)
		{
			String intro=getIntroductionString(f);
			if(intro==null)
				continue;
			String getFirstSentence=SummarizationSolver.getFirstSentence(intro);
			List<String> keyphraseList=ExistingArticleGenerator.retriveKeyWords(getFirstSentence);
			//System.out.println(keyphraseList);
			if(keyphraseList.size()>1)
			{
				keyphraseList=keyphraseList.subList(0, 1);
			}
			String articleTitle=f.getName().replaceAll(".txt", "").replaceAll("_", " ");
			//String query = PassageClassifier.generateQuery(keyphraseList,articleTitle+" "+categoryName.replaceAll("_", " "));
			String query = PassageClassifier.generateQuery(keyphraseList,articleTitle);
			System.out.println("Google Query:"+query);
			HashMap<String,List<String>> urlTexts=StubDocumentSearcher.getURLAndTexts(query);
			HashMap<String, List<URLContentObject>> urlContentLabelled=
					PassageClassifier.classifyNewData(classifier, inferencer, urlTexts, bestNumTopic,
							categoryName);


			if(urlContentLabelled.size()>0)
			{
				FileUtils.copyFile(f, new File(outFolder+"/"+"models"+"/"+f.getName()));
				File systemGenSummary=new File(outFolder+"/"+"system"+"/"+f.getName());
				systemGenSummary.createNewFile();
				FileWriter fw = new FileWriter(systemGenSummary.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("Introduction||"+intro+"\n");
				for(String sectionName:urlContentLabelled.keySet())
				{
					//String content=swd.generateSnippet(SummarizeWebData.attachIds(urlContentLabelled.get(sectionName)), query,
					//	sectionName);
					System.err.println(sectionName+" has "+urlContentLabelled.get(sectionName).size() +"content");
					//					for(int i=0;i<urlContentLabelled.get(sectionName).size();i++)
					//					{
					//						System.out.println("Content"+i+"/"+urlContentLabelled.get(sectionName).get(i).getContent());
					//					}

					//String summaryContent=SummarizationSolver.generateSectionSummary(urlContentLabelled.get(sectionName), query);
					String summaryContent=SummarizationSolver.
							generateSectionSummaryWithAllPaths(urlContentLabelled.get(sectionName), query);
					if(summaryContent.trim().length()>30)
					{
						bw.write(sectionName+"||"+summaryContent+"\n");
					}


				}
				bw.close();
			}


			//System.out.println(urlContentLabelled.get("Awards").get(0).getContent());
			//SummarizationSolver.generateSectionSummary(urlContentLabelled.get("Biography"));

		}

		System.out.println(selectedFiles.size());

			}

	public static List<String> retriveKeyWords(String introString) throws Exception
	{
		//StringTokenizer st=new StringTokenizer(introString);

		return JtopiaUsage.getKeyPhrasesUsingJtopia(introString);
	}

	private String getIntroductionString(File f) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
		String sCurrentLine;
		while ((sCurrentLine = br.readLine()) != null) {
			String[] components=sCurrentLine.split(Pattern.quote("||"));
			if(components[0].contentEquals("Introduction") && components.length==2)
			{
				br.close();
				return components[1];
			}
		}

		br.close();
		return null;
	}

//	public static void main(String[] args) throws Exception {
//		JtopiaUsage.loadComponents();
//		// Configuration.setModelFileLocation("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
//		URLCrawlerForTopic.loadStanfordComponents();
//		//SummarizationSolver.loadCoreNLP();
//		SummarizationSolver.loadLM();
//		SummarizationSolver.loadStanfordComponents();
//		ExistingArticleGenerator eag=new ExistingArticleGenerator();
//		//eag.chooseRandomArticlesToCreate("Diseases_and_disorders",40);
//		int bestTopic=40;
//		String catTrain="Science_fiction_films";
//		TopicInferencer inferencer=TopicModelGenerator.getBestTopicModelInferer(catTrain, bestTopic);
//		RandomForest classifier=PassageClassifier.getRFBestClassifier(bestTopic, catTrain);
//
//		eag.chooseRandomArticlesToCreate(catTrain,bestTopic,inferencer,classifier);
//		//eag.chooseRandomArticlesToCreate("American_mathematicians",20);
//		//eag.chooseRandomArticlesToCreate("Information_technology_companies_of_the_United_States",50);
//	}

		public static void main(String[] args) throws Exception {
			
			java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
			XMLWikipediaExtractor xwe=new XMLWikipediaExtractor();
			int bestTopic=xwe.getAllArticlesUnderCategory(WikiHelper.getSpecificProperty("categoryForTraining"));
			JtopiaUsage.loadComponents();
		    URLCrawlerForTopic.loadStanfordComponents();
	//	    //SummarizationSolver.loadCoreNLP();
		    SummarizationSolver.loadLM();
		    SummarizationSolver.loadStanfordComponents();
		    //Load the topic model
			//int bestTopic=40;
		    TopicInferencer inferencer=TopicModelGenerator.getBestTopicModelInferer(WikiHelper.getSpecificProperty("categoryForTraining"), bestTopic);
			RandomForest classifier=PassageClassifier.getRFBestClassifier(bestTopic, WikiHelper.getSpecificProperty("categoryForTraining"));
			ExistingArticleGenerator eag=new ExistingArticleGenerator();
			xwe.storeStubArticles(WikiHelper.getSpecificProperty("categoryToGenerate"));
			eag.chooseRandomArticlesToCreate(WikiHelper.getSpecificProperty("categoryToGenerate"),bestTopic,
					inferencer,classifier);
			
		}


}
