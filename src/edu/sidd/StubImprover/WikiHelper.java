package edu.sidd.StubImprover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

import jtopia.JtopiaUsage;

import org.apache.commons.io.FileUtils;
import weka.classifiers.trees.RandomForest;
import edu.sidd.LexRank.SummarizeWebData;
import edu.sidd.Objects.TopicInferenceObject;
import edu.sidd.Objects.URLContentObject;
import edu.sidd.TopicVectors.PassageClassifier;
import edu.sidd.TopicVectors.TopicModelGenerator;
import edu.sidd.crawlXML.URLCrawlerForTopic;
import edu.sidd.crawlXML.WikiExportSupporter;

public class WikiHelper {

	public static String stubDirectory="inputFiles/outputWikiStubs";

	public List<String> getTitlesInGUI(String dirPath)
	{
		List<String> titles=new ArrayList<String>();
		File[] folder=new File(dirPath).listFiles();
		new File(dirPath).setReadable(true);
		//System.out.println(folder.hashCode());
		for(File f:folder)
		{
			String title=f.getName().split("\\.")[0].replaceAll("_", " ");
			//System.out.println(title);
			titles.add(title);

		}

		return titles;
	}

	public static void main(String[] args) throws Exception {
		WikiHelper wh=new WikiHelper();
		//WikiHelper.runFirst();
		//System.out.println(getAppVersion());
		//System.out.println(System.getProperty("user.dir"));
		//System.out.println(wh.getTitlesInGUI(stubDirectory).toString());
		WikiExportSupporter wes=new WikiExportSupporter();
		wes.getAllArticlesUnderCategory("Sleep_disorders");

	}

	//Use this for loading from properties file: dir.config
	public static String getSpecificProperty(String propertyName) throws IOException{

	    String versionString = null;

	    //to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;

	    //the base folder is ./, the root of the main.properties file  
	    String path = "./dir.config";

	    //load the file handle for main.properties
	    file = new FileInputStream(path);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();

	    //retrieve the property we are intrested, the app.version
	    versionString = mainProperties.getProperty(propertyName);

	    return versionString;
	}
	
	
	public static String getIntroContent(String title) throws IOException
	{
		String stubFile=title.replaceAll(" ", "_");
		//System.out.println(ResourcesPlugin.getWorkspace().getRoot());
		File f=new File(stubDirectory+"/"+stubFile+".txt");
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
	static PassageClassifier pc=null;
	static TopicInferenceObject tio=null;
	static RandomForest rfClassifier=null;
	static SummarizeWebData swd=null;
//	public static void runFirst() throws Exception
//	{
////		String dir="C:\\Work\\Projects\\WikipediaTemplate"+
////				"\\codes\\Sweble\\";
////		System.setProperty("user.dir", dir); 
//		//System.setProperty("user.dir", dir);
//		URLCrawlerForTopic.loadStanfordComponents();
//		//CompressionModel.loadCplex();
//		//SentenceCompressor.loadModels();
//		pc=new PassageClassifier();
//		tio=TopicModelGenerator.getTopicInferenceObject();
//		rfClassifier=pc.trainClassifier();
//		swd=new SummarizeWebData();
//		swd.loadStanfordComponents();
//		JtopiaUsage.loadComponents();
//	}
	
	public static List<String> retriveKeyWords(String introString) throws Exception
	{
		return JtopiaUsage.getKeyPhrasesUsingJtopia(introString);
	}
	

	public static void generateStubContent(String articleTitle, 
			List<String> keyphraseList) throws Exception
	{
	
		
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		System.err.println("Creating stub on.."+articleTitle);
		String stubTitle=articleTitle;
		String introString=getIntroContent(stubTitle);
		if(introString==null) return;
			
		
		String query = PassageClassifier.generateQuery(keyphraseList,stubTitle);
		System.out.println("Google Query:"+query);
		HashMap<String,List<String>> urlTexts=StubDocumentSearcher.getURLAndTexts(query);
		HashMap<String, List<URLContentObject>> urlContentLabelled=pc.testInstances(rfClassifier, tio, urlTexts);
		System.out.println(urlContentLabelled);

		if(urlContentLabelled.size()>0)
		{
			//String systemGeneratedStubs = null;
			File f=new File("createdStubs"+"/"+stubTitle.replaceAll(" ", "_")+".txt");
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(introString+"\n\n");
			for(String sectionName:urlContentLabelled.keySet())
			{
				//String content=swd.generateSnippetForStub
				//		(SummarizeWebData.attachIds(urlContentLabelled.get(sectionName)), query,
				//				sectionName);
				String content ="Hello";
				if(content.length()>30)
				{
					bw.write(content+"\n\n");
				}
			}
			bw.close();
		}

	}
	
	public static String readFromFile(String filePath) throws IOException
	{
		String s=FileUtils.readFileToString(new File(filePath));
		return s;
	}

}
