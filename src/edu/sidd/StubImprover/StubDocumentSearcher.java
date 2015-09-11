package edu.sidd.StubImprover;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;




import edu.sidd.crawlXML.URLCrawlerForTopic;

public class StubDocumentSearcher {

	private static final long serialVersionUID = -5178288489778728847L;
	String stubFiles="inputFiles/outputWikiStubs";
	//static String stubFile="Absent_adrenal_gland";
	//static MauiTopicExtractor topicExtractor = new MauiTopicExtractor();
	//static MauiModelBuilder modelBuilder = new MauiModelBuilder();
	public String baseDir="C:\\Work\\Projects\\WikipediaTemplate\\codes\\Sweble\\WikiGUI_v2\\";
	public String getIntroductionString(String stubTitle) throws IOException
	{
		String stubFile=stubTitle.replaceAll(" ", "_");
		File f=new File(stubFiles+"/"+stubFile+".txt");
		BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
		String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] components=sCurrentLine.split(Pattern.quote("||"));
				if(components[0].contentEquals("Introduction"))
				{
					br.close();
					return components[1];
				}
			}
		
		br.close();
		return null;
	}
	
	
		
	public static HashMap<String, List<String>> getURLAndTexts(String query) throws Exception
	{
		HashMap<String,List<String>> urlText=URLCrawlerForTopic.cleanText(query);
		return urlText;
	}
	
//	public static void main(String[] args) throws Exception {
//		StubDocumentSearcher sdc=new StubDocumentSearcher();
//		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
//		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
//		String stubFile="Activation syndrome";
//		String introString=sdc.getIntroductionString(stubFile);
//		sdc.loadMAUIComponents(topicExtractor, modelBuilder);
//		List<String> keyphraseList=sdc.getKeyphrases(introString, topicExtractor, modelBuilder);
//		String query = generateQuery(keyphraseList,stubFile);
//		System.out.println("Google Query:"+query);
//		HashMap<String,List<String>> urlText=URLCrawlerForTopic.cleanText(query);
//		System.out.println(urlText.size());
//		//search google now
//		
//		
//		
//	}
	
}
