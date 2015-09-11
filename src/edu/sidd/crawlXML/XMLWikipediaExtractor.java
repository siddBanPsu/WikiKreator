package edu.sidd.crawlXML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import weka.classifiers.trees.RandomForest;

import cc.mallet.topics.TopicInferencer;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import edu.sidd.ExtractLabels.LabelContentExtractor;
import edu.sidd.TopicVectors.PassageClassifier;
import edu.sidd.TopicVectors.TopicModelGenerator;

public class XMLWikipediaExtractor {

	String dirForWikiDumps="inputFiles/rawFiles/categoryXML";
	static String dirForProcessedWikiXml="inputFiles/rawFiles/processedXML";
	static String urlStoreDir="inputFiles/rawFiles/urlStore";
	static String dirForProcessedWikiTxt="inputFiles/rawFiles/processedTxt";
	public int getAllArticlesUnderCategory(String mainCategoryName) throws Exception
	{
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		//webClient.getOptions().
		HtmlPage page = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport&" +
				"addcat=&catname="+mainCategoryName+"&wpDownload=");
		System.out.println(page.getUrl());
		
		File folderForDumps=new File(dirForWikiDumps+"/"+mainCategoryName+"-xml");
		//FileUtils.deleteQuietly(folderForDumps);
		Thread.sleep(2000);
		folderForDumps.mkdirs();
		HtmlForm form = page.getForms().get(0);
		System.out.println(form.getAttribute("method"));
		System.out.println(page.getTitleText());
		//HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Export").get(0);
		//Retrive the page names
		List<String> categoryList=new ArrayList<String>();
		categoryList.add(mainCategoryName);
		List<String> articleNameList=new ArrayList<String>();
		//get content from textarea
		String textArea=(page.getElementByName("pages").asText());
		String[] lines = textArea.split(System.getProperty("line.separator"));
		for(String line:lines)
		{
			if(line.startsWith("Category:") && !line.contains("stub"))
			{
				categoryList.add(line.split(":")[1]);
			}
			else
			{
				articleNameList.add(line);
			}
		}

		System.out.println(categoryList.size());
		categoryList=searchCategoriesUntilDepth(categoryList);
		System.out.println("Printing categories:"+categoryList);
		System.out.println("Exploring "+categoryList.size()+" categories.......");
		extractArticles(categoryList, folderForDumps.getAbsolutePath());//Uncomment this if new run
		//System.out.println(is);
		File folderForProcessedDumps=new File(dirForProcessedWikiXml+"/"+mainCategoryName+"-xml");
		FileUtils.deleteQuietly(folderForProcessedDumps);
		Thread.sleep(2000);
		folderForProcessedDumps.mkdirs();
		File[] xmlFiles=folderForDumps.listFiles();
		for(File f:xmlFiles)
		{
			//System.out.println("PROCESSING XML FILE:"+f.getAbsolutePath());
			processWikiDumpWithPython(f.getAbsolutePath(),
					folderForProcessedDumps.getAbsolutePath()+
					"/"+f.getName().replaceAll("xml", "")+"txt"
										);
		}
		
		writeFilesInText(mainCategoryName);
		LabelContentExtractor lce=new LabelContentExtractor();
		//lce.loadURLs();
		String wikiTxtDir=dirForProcessedWikiTxt+"/"+mainCategoryName+"-txt";
		String urlFile=urlStoreDir+"/"+mainCategoryName+"-url";
		lce.createSectionContents(wikiTxtDir,urlFile, mainCategoryName);
		TopicModelGenerator.generateTopicModels(mainCategoryName);
		int bestNumTopic=PassageClassifier.getBestNumTopic(mainCategoryName);
		//System.out.println(bestNumTopic);
		//TopicInferencer inferencer=TopicModelGenerator.getBestTopicModelInferer(mainCategoryName, bestNumTopic);
		//RandomForest classifier=PassageClassifier.getRFBestClassifier(bestNumTopic, mainCategoryName);
		webClient.closeAllWindows();
		return bestNumTopic;
	}
	
	public void storeStubArticles(String mainCategoryName) throws Exception
	{
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		//webClient.getOptions().
		HtmlPage page = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport&" +
				"addcat=&catname="+mainCategoryName+"&wpDownload=");
		System.out.println(page.getUrl());
		
		File folderForDumps=new File(dirForWikiDumps+"/"+mainCategoryName+"-xml");
		//FileUtils.deleteQuietly(folderForDumps);
		Thread.sleep(2000);
		folderForDumps.mkdirs();
		HtmlForm form = page.getForms().get(0);
		System.out.println(form.getAttribute("method"));
		System.out.println(page.getTitleText());
		//HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Export").get(0);
		//Retrive the page names
		List<String> categoryList=new ArrayList<String>();
		categoryList.add(mainCategoryName);
		List<String> articleNameList=new ArrayList<String>();
		//get content from textarea
		String textArea=(page.getElementByName("pages").asText());
		String[] lines = textArea.split(System.getProperty("line.separator"));
		for(String line:lines)
		{
			if(line.startsWith("Category:") && !line.contains("stub"))
			{
				categoryList.add(line.split(":")[1]);
			}
			else
			{
				articleNameList.add(line);
			}
		}

		System.out.println(categoryList.size());
		categoryList=searchCategoriesUntilDepth(categoryList);
		System.out.println("Printing categories:"+categoryList);
		System.out.println("Exploring "+categoryList.size()+" categories.......");
		extractArticles(categoryList, folderForDumps.getAbsolutePath());//Uncomment this if new run
		//System.out.println(is);
		File folderForProcessedDumps=new File(dirForProcessedWikiXml+"/"+mainCategoryName+"-xml");
		FileUtils.deleteQuietly(folderForProcessedDumps);
		Thread.sleep(2000);
		folderForProcessedDumps.mkdirs();
		File[] xmlFiles=folderForDumps.listFiles();
		for(File f:xmlFiles)
		{
			//System.out.println("PROCESSING XML FILE:"+f.getAbsolutePath());
			processWikiDumpWithPython(f.getAbsolutePath(),
					folderForProcessedDumps.getAbsolutePath()+
					"/"+f.getName().replaceAll("xml", "")+"txt"
										);
		}
		
		writeFilesInText(mainCategoryName);
		LabelContentExtractor lce=new LabelContentExtractor();
		//lce.loadURLs();
		String wikiTxtDir=dirForProcessedWikiTxt+"/"+mainCategoryName+"-txt";
		String urlFile=urlStoreDir+"/"+mainCategoryName+"-url";
		lce.createSectionContents(wikiTxtDir,urlFile, mainCategoryName);
		//TopicModelGenerator.generateTopicModels(mainCategoryName);
		//int bestNumTopic=PassageClassifier.getBestNumTopic(mainCategoryName);
		//System.out.println(bestNumTopic);
		//TopicInferencer inferencer=TopicModelGenerator.getBestTopicModelInferer(mainCategoryName, bestNumTopic);
		//RandomForest classifier=PassageClassifier.getRFBestClassifier(bestNumTopic, mainCategoryName);
		webClient.closeAllWindows();
		//return bestNumTopic;
	}
	
	
	
	public void getAllStubsUnderCategory(String mainCategoryName) throws Exception
	{
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		HtmlPage page = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport&" +
				"addcat=&catname="+mainCategoryName+"&wpDownload=");
		System.out.println(page.getUrl());
		
		File folderForDumps=new File(dirForWikiDumps+"/"+mainCategoryName+"-xml");
		FileUtils.deleteQuietly(folderForDumps);
		Thread.sleep(2000);
		folderForDumps.mkdirs();
		HtmlForm form = page.getForms().get(0);
		System.out.println(form.getAttribute("method"));
		System.out.println(page.getTitleText());
		//HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Export").get(0);
		//Retrive the page names
		List<String> categoryList=new ArrayList<String>();
		categoryList.add(mainCategoryName);
		List<String> articleNameList=new ArrayList<String>();
		//get content from textarea
		String textArea=(page.getElementByName("pages").asText());
		String[] lines = textArea.split(System.getProperty("line.separator"));
		for(String line:lines)
		{
			if(line.startsWith("Category:"))// && !line.contains("stub"))
			{
				categoryList.add(line.split(":")[1]);
			}
			else
			{
				articleNameList.add(line);
			}
		}

		System.out.println(categoryList.size());
		//categoryList=searchCategoriesUntilDepth(categoryList);
		System.out.println("Printing categories:"+categoryList);
		System.out.println("Exploring "+categoryList.size()+" categories.......");
		extractArticles(categoryList, folderForDumps.getAbsolutePath());
		//System.out.println(is);
		File folderForProcessedDumps=new File(dirForProcessedWikiXml+"/"+mainCategoryName+"-xml");
		FileUtils.deleteQuietly(folderForProcessedDumps);
		Thread.sleep(2000);
		folderForProcessedDumps.mkdirs();
		File[] xmlFiles=folderForDumps.listFiles();
		for(File f:xmlFiles)
		{
			//System.out.println("PROCESSING XML FILE:"+f.getAbsolutePath());
			processWikiDumpWithPython(f.getAbsolutePath(),
					folderForProcessedDumps.getAbsolutePath()+
					"/"+f.getName().replaceAll("xml", "")+"txt"
										);
		}
		
		writeFilesInText(mainCategoryName);
		LabelContentExtractor lce=new LabelContentExtractor();
		//lce.loadURLs();
		String wikiTxtDir=dirForProcessedWikiTxt+"/"+mainCategoryName+"-txt";
		String urlFile=urlStoreDir+"/"+mainCategoryName+"-url";
		lce.createSectionContents(wikiTxtDir,urlFile, mainCategoryName);
//		TopicModelGenerator.generateTopicModels(mainCategoryName);
//		int bestNumTopic=PassageClassifier.getBestNumTopic(mainCategoryName);
//		System.out.println(bestNumTopic);
//		TopicInferencer inferencer=TopicModelGenerator.getBestTopicModelInferer(mainCategoryName, bestNumTopic);
//		RandomForest classifier=PassageClassifier.getRFBestClassifier(bestNumTopic, mainCategoryName);
		webClient.closeAllWindows();
	}
	
	
	
	public void writeFilesInText(String categoryName) throws IOException, InterruptedException 
	{
		File[] processedFiles = new File(dirForProcessedWikiXml+"/"+categoryName+"-xml").listFiles();
		new File(urlStoreDir).mkdirs();
		File URLMapper=new File(urlStoreDir+"/"+categoryName+"-url");
		URLMapper.createNewFile();
		File outTxtDir=new File(dirForProcessedWikiTxt+"/"+categoryName+"-txt");
		FileUtils.deleteQuietly(outTxtDir);
		Thread.sleep(2000);
		outTxtDir.mkdirs();
		BufferedWriter bw2=new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(URLMapper.getAbsolutePath()),"UTF-8"));
		List<String> titleTracker=new ArrayList<String>();
		for(File file:processedFiles)
		{
			//if(!file.getName().startsWith("Wikidump.Belizean_academics"))
			//	continue;
			//System.out.println("Processing file:"+file.getAbsolutePath());
			Document domDoc = null;
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				//ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
				domDoc = docBuilder.parse(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			DocumentTraversal traversal = (DocumentTraversal) domDoc;
			NodeIterator iterator = traversal.createNodeIterator(domDoc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
			File writeFile=null;
			FileWriter fw = null;
			BufferedWriter bw=null;
     		//FileWriter fw2=new FileWriter(URLMapper.getAbsoluteFile());
			
			for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
				//String tagname = ((Element) n).getAttribute("sectionName");
//				System.out.println(((Element)n).getNodeName()+"/"+
//						((Element) n).getAttribute("sectionName"));
				//String title="";
				if(((Element) n).getNodeName().contentEquals("doc"))
				{
					String titleValue = ((Element) n).getAttribute("title");
					//title=titleValue;
					titleValue=titleValue.replace("^\\.+", "").replaceAll("[\\\\/:*?\"<>|]", "");
					//System.out.println("Title level:"+titleValue);
					//if(titleTracker.contains(titleValue))
					//	continue;
					//else
						//titleTracker.add(titleValue);
					//System.out.println("Code is here now...."+titleValue);
					writeFile=new File(outTxtDir+"/"+titleValue.replaceAll(" ", "_")+".txt");
					writeFile.createNewFile();
					//Thread.sleep(1000);
					if(!writeFile.exists())
						Thread.sleep(1000);
					//System.out.println(writeFile.getAbsolutePath());
					fw=new FileWriter(writeFile.getAbsoluteFile());
					bw=new BufferedWriter(fw);
					//System.out.println("Title is:"+titleValue);
					bw2.write(titleValue+"||"+((Element) n).getAttribute("url")+"\n");
					bw.append("Introduction||"+n.getFirstChild().getNodeValue().trim().replaceAll("\n", " "));
					bw.close();
					//continue;
				}

				if(n.getNodeName().startsWith("h2")) //for level 2
				{
					String sectionName = ((Element) n).getAttribute("sectionName");
					//System.out.println(bw.toString());
					fw=new FileWriter(writeFile.getAbsoluteFile(),true);
					bw=new BufferedWriter(fw);
					//System.out.println(sectionName + "=" + getFirstLevelTextContent((Element) n));
					bw.append("\n"+sectionName + "||" + getFirstLevelTextContent(n)+" ");
					bw.close();
					//continue;
				}

				if(n.getNodeName().startsWith("h3"))
				{
					//String sectionName = ((Element) n).getAttribute("sectionName");
					//System.out.println(bw.toString());
					fw=new FileWriter(writeFile.getAbsoluteFile(),true);
					bw=new BufferedWriter(fw);
					//System.out.println(sectionName + "=" + getFirstLevelTextContent((Element) n));
					//bw.append(sectionName + "||" + getFirstLevelTextContent(n)+"\n");
					bw.append(getFirstLevelTextContent(n));
					bw.close();
					//continue;
				}
			}
		}
		//bw.close();
		bw2.close();

	}
	
	public static String getFirstLevelTextContent(Node node) {
		NodeList list = node.getChildNodes();
		StringBuilder textContent = new StringBuilder();
		for (int i = 0; i < list.getLength(); ++i) {
			Node child = list.item(i);
			if (child.getNodeType() == Node.TEXT_NODE)
				textContent.append(child.getTextContent().trim().replaceAll("\n", " "));
		}
		return textContent.toString();
	}
	
	private void processWikiDumpWithPython(String xmlFile, String outFile) throws InterruptedException, IOException
	{
		
		ProcessBuilder processBuilder = new ProcessBuilder("C:/Users/sub253/AppData/Local/Continuum/Anaconda3/python.exe",
				"lib/pythonThirdPartyFiles/Wikipedia-Extractor.py",xmlFile,outFile);
		Process p = processBuilder.start();
		p.waitFor(); //wait for python to end
		p.destroy();

		
	}
	
	
	public List<String> searchCategoriesUntilDepth(List<String> categoryList) throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		int depth=0;
		List<String> visitedCategories=new ArrayList<String>();
		while(depth<=0)
		{
			List<String> tempCatList=new ArrayList<String>();
			WebClient webClient = new WebClient(BrowserVersion.CHROME);
			for(String s:categoryList)
			{
				if(visitedCategories.contains(s))
					continue;
				System.err.println("Running category:"+s+"/"+categoryList.indexOf(s));
				visitedCategories.add(s);
				HtmlPage page = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport&" +
						"addcat=&catname="+s+"&wpDownload=");
				String textArea=(page.getElementByName("pages").asText());
				String[] lines = textArea.split(System.getProperty("line.separator"));
				//System.out.println(lines.length);
				for(String line:lines)
				{
					if(line.startsWith("Category:") && !line.contains("stub"))
					{
						if(!categoryList.contains(line.split(":")[1]))
						{
							tempCatList.add(line.split(":")[1]);

						}
					}
					else
					{
						//articleNameList.add(line);
					}
				}

			}
			webClient.closeAllWindows();

			for(String l:tempCatList)
			{
				if(!categoryList.contains(l))
					categoryList.add(l);
			}
			depth++;
		}

		return categoryList;

	}
	
	
	private void extractArticles(List<String> categoryList,
			String folderForXML) throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setUseInsecureSSL(true);
		for(String s:categoryList)
		{
			if(s.contains("\""))
				continue;
			s=s.replaceAll("/", "-");
			File f=new File(folderForXML+"/Wikidump."+s+".xml");
			if(f.exists())
				continue;
			//File xmlFile=new File(folderForXML);
			System.out.println("Running category:"+s+"/"+categoryList.indexOf(s));
			HtmlPage page = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport&" +
					"addcat=&catname="+s+"&wpDownload=");
			String textArea=(page.getElementByName("pages").asText());
			String[] lines = textArea.split(System.getProperty("line.separator"));
			List<String> articleList=new ArrayList<String>();
			for(String line:lines)
			{
				if(!line.startsWith("Category:") && !articleList.contains(line))
				{
					articleList.add(line);
				}
			}
			HtmlPage pageFinal = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport");
			pageFinal.getElementByName("pages").setTextContent(listToString(articleList));
			HtmlForm form = pageFinal.getForms().get(0);
			HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Export").get(0);
			String is = button.click().getWebResponse().getContentAsString();
			
					
			
			f.createNewFile();
			//FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(f.getAbsoluteFile()),"UTF-8")));
			bw.write(is);
			bw.close();
			System.err.println("Total articles:"+articleList.size()+" processed in category~~"+s);
			
		}
		
		
			
	}
	
	private String listToString(List<String> list) throws IOException
	{
		StringBuffer sb=new StringBuffer();
		File f=new File("temp.txt");
		f.createNewFile();
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for(String item:list)
		{
			bw.write(item+"\n");
			sb.append(item+"\n");
		}
		bw.close();
		return sb.toString();
	}
	
	public static void main(String[] args) throws Exception {
		
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		XMLWikipediaExtractor xwe=new XMLWikipediaExtractor();
		
		//xwe.getAllArticlesUnderCategory("Information_technology_companies_of_the_United_States");
		//xwe.getAllArticlesUnderCategory("Academics_by_nationality");
		//xwe.getAllArticlesUnderCategory("Diseases_and_disorders");
		//xwe.getAllStubsUnderCategory("Disease_stubs");
		//xwe.getAllArticlesUnderCategory("American_mathematicians");
		xwe.getAllArticlesUnderCategory("Elections_by_country");
		
	}
	
}
