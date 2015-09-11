package edu.sidd.crawlXML;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.sidd.StubImprover.WikiHelper;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
public class URLCrawlerForTopic {

	private static final int numResults = 20;
	private static final int MAX_COMMAS = 3;
	String searchQuery="diabetes";
	static String[] blackList={"google","wikipedia","facebook","amazon","linkedin"};
	static int SENTENCE_LENGTH_CONSTRAINT=8;

	//static String[] pronounListToAvoid={"i","you","yours","your","yourself","yourselves","he",
	//	"she","my","her","his","http","www","we","our","me","mine","myself"};

	static List<String> pronounListToAvoid=null;
			
	
	public static List<String> readPronouns(String file) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		List<String> scoreList=new ArrayList<String>();
		String sCurrentLine="";
		while ((sCurrentLine = br.readLine()) != null) {
				{
				scoreList.add(sCurrentLine);
				}
		}
		br.close();
		
		return scoreList;
	}
	
	
	public static List<String> searchGoogle(String query) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException
	{
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	
		List<String> URLList=new ArrayList<String>();
		//java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		//		BrowserVersionFeatures[] bvf = new BrowserVersionFeatures[1];
		//	    bvf[0] = BrowserVersionFeatures.HTMLIFRAME_IGNORE_SELFCLOSING;
		//	    BrowserVersion bv = new BrowserVersion(
		//	            BrowserVersion.CHROME.getApplicationName(), 
		//	            "5.0 (Windows; en-US)","Chrome",
		//	            (float) 3.6, bvf);

		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		//webClient.setCssErrorHandler(new SilentCssErrorHandler());
		String finalQuery="https://www.google.com/search?num="+numResults +
				"&q="+query;
		System.out.println(finalQuery);
		HtmlPage page = webClient.getPage(finalQuery);
		Thread.sleep(2000);
		String xmlString=page.asXml();
		File URLMapper=new File("searchResult.xml");
		URLMapper.createNewFile();
		BufferedWriter bw2=new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(URLMapper.getAbsolutePath()),"UTF-8"));
		bw2.write(xmlString);
		bw2.close();
		Document domDoc =null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			//ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
			domDoc = docBuilder.parse(URLMapper);
		} catch (Exception e) {
			e.printStackTrace();
		}

		DocumentTraversal traversal = (DocumentTraversal) domDoc;
		NodeIterator iterator = traversal.createNodeIterator(domDoc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);

		for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
			//String tagname = ((Element) n).getAttribute("sectionName");
			//System.out.println(n.getNodeName());
			//			String title="";
			if(n.getNodeName().contentEquals("h3"))
			{
				//if(n.getAttributes().getNamedItem("class").getNodeValue().contentEquals("class=r")
				String j=((Element)n).getAttribute("class");
				//System.out.println(j);
				if(j.contentEquals("r"))
				{
					Node a=n.getChildNodes().item(1);
					String url=((Element) a).getAttribute("href");
					if(!(url.contains("google")||url.contains("wikipedia")||url.contains("amazon")
							||url.contains("amazon"))
							&& url.trim().length()>0)
					{
						URLList.add(url);
					}
				}
				//System.out.println(n.getAttributes().getNamedItem("class"));
				//System.out.println(n.getChildNodes().item(1).getAttributes().getNamedItem("href"));
			}
		}
		System.out.println(URLList);
		webClient.closeAllWindows();
		return URLList;

	}

	static StanfordCoreNLP pipeline=null;
	public static void loadStanfordComponents()
	{
		pipeline=URLCrawlerForTopic.loadCoreNLP();
	}

	public static HashMap<String,List<String>> cleanText(String query) throws BoilerpipeProcessingException, FailingHttpStatusCodeException, IOException, InterruptedException
	{
		List<String> urlList=searchGoogle(query);
		//System.setProperty("jsse.enableSNIExtension", "false");
		//java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.setJavaScriptTimeout(2000);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setTimeout(150000);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);


		HashMap<String,List<String>> extractTextMap=new HashMap<String, List<String>>();
		//StanfordCoreNLP pipeline=loadCoreNLP();
		List<String> urlVisited=new ArrayList<String>();
		for(String s:urlList)
		{
			if(s.contains("url="))
			{
				Matcher m=Pattern.compile("url=(.*)").matcher(s);
				
				if(m.find())
				{
					s=m.group(1).trim();
				}
			}
			
			if(!s.trim().startsWith("http"))
							continue;
			
			URL aURL = new URL(s);
			if(urlVisited.contains(aURL.getHost()))
				continue;
			urlVisited.add(aURL.getHost());
			if(s.length()<5)
				continue;
			System.err.println("URL Link:"+s);

			int status =0;
			try
			{
				status=webClient.getPage(s).getWebResponse()
						.getStatusCode();
			}
			catch(Exception e)
			{
				continue;
			}
			System.out.println("Web response status:"+status);
			//webClient.getOptions().setJavaScriptEnabled(false);
			if(status!=200)
				continue;
			Page rawpage;
			try{
				rawpage = webClient.getPage(s);
			}
			catch(Exception e)
			{
				continue;
			}
			
			//System.out.println("Encoding:"+page.getInputEncoding());
			//System.out.println(page.asText());
			if(rawpage.isHtmlPage())
			{
				HtmlPage page;
				try
				{
					page=webClient.getPage(s);
				}
				catch(Exception e)
				{
					continue;
				}
			
				//WebRequest webRequest = new WebRequest(new URL(s));
				//webRequest.setCharset("UTF-8");

				//page = webClient.getPage(webRequest);
				//System.out.println("Encoding:"+page.getPageEncoding());
				// is.setCharacterStream(new StringReader((page.asXml())));
				// NOTE: Use ArticleExtractor unless DefaultExtractor gives better results for you
				String text = ArticleExtractor.INSTANCE.getText(page.asXml());
				//System.out.println("WEbsite:"+text);
				List<String> paraList=getfilteredText(pipeline,text);
				extractTextMap.put(s,paraList);
				
				//page.cleanUp();
			}
			//rawpage.cleanUp();
			//}
			//webClient.closeAllWindows();
		}
		List<WebWindow> windows = webClient.getWebWindows();
	    for (WebWindow wd : windows) {
	        // wd.getThreadManager().interruptAll();
	        wd.getJobManager().removeAllJobs();
	    }
	    webClient.closeAllWindows();
		
		//webClient.closeAllWindows();
		System.out.println("Remaining Windows:"+webClient.getWebWindows().size());
		return extractTextMap;


	}

	public static StanfordCoreNLP loadCoreNLP()
	{
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit");//, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		return pipeline;
	}

	private static List<String> getfilteredText(StanfordCoreNLP stanfordCoreNLP, String textFromWebsite) throws IOException
	{
		String[] lines= textFromWebsite.split("[\\r\\n]+");
		pronounListToAvoid=readPronouns(WikiHelper.getSpecificProperty("pronouns"));
		System.out.println("Num lines:"+lines.length);
		List<String> cleanParagraphs=new ArrayList<String>();
		for(String line:lines)
		{
			Annotation document = new Annotation(line);
			// run all Annotators on this text
			stanfordCoreNLP.annotate(document);
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);

			if(sentences.size()>=1)
			{
				StringBuffer sb=new StringBuffer();
				for(CoreMap sentence: sentences) {
					// traversing the words in the current sentence
					// a CoreLabel is a CoreMap with additional token-specific methods
					int indicator=0;
					int lengthSentence=sentence.get(TokensAnnotation.class).size();
					if(lengthSentence<=SENTENCE_LENGTH_CONSTRAINT
							|| sentence.toString().toLowerCase().contains("www")
							|| sentence.toString().toLowerCase().contains("http"))
						continue;
					for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
						// this is the text of the token
						String word = token.get(TextAnnotation.class);

						if(pronounListToAvoid.contains(word.toLowerCase()))
						{
							indicator=1;
							break;
						}
					}
					if(indicator!=1 && sentence.toString().endsWith("."))
					{
						String finalString=sentence.toString().replaceAll("\\(.*\\)","");
						finalString=finalString.replaceAll("\\[.*\\]","");
						finalString=sentence.toString().replaceAll("\\s+", " ").trim();
						//byte ptext[] = finalString.getBytes(ISO_8859_1); 
						//finalString = new String(ptext, UTF_8); 
						//finalString=finalString.replaceAll("\\[*\\]", "").trim();
						finalString=finalString.replaceAll("\\?", "").trim();

						//String finalString2=new Stri
						sb.append(finalString+" ");
					}
				}
				if(sb.toString().trim().length()>0)
				{
					//System.out.println("Buffer:"+sb.toString().replaceAll("\\?", "").trim());
					String obtainedString=sb.toString().replaceAll("\\?", "").trim();
//					int commas = 0;
//					for(int i = 0; i < obtainedString.length(); i++) {
//						if(obtainedString.charAt(i) == ',') commas++;
//					}
//					for(int i = 0; i < obtainedString.length(); i++) {
//						if(obtainedString.charAt(i) == ';') commas++;
//					}
//					for(int i = 0; i < obtainedString.length(); i++) {
//						if(obtainedString.charAt(i) == ':') commas++;
//					}
//					for(int i = 0; i < obtainedString.length(); i++) {
//						if(obtainedString.charAt(i) == '.') commas++;
//					}
					
					Pattern p=Pattern.compile("[^A-Za-z0-9 ]");
					Matcher m=p.matcher(obtainedString);
					int numNonAlpha=0;
					while(m.find())
					{	
						//System.out.println(m.group());
						numNonAlpha++;
					}
					//System.out.println(num);
					if(numNonAlpha<=MAX_COMMAS)
					{
						cleanParagraphs.add(obtainedString);
					}
				}
			}
		}
		StanfordCoreNLP.clearAnnotatorPool();
		return cleanParagraphs;


	}
}
