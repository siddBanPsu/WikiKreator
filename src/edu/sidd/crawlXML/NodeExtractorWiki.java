package edu.sidd.crawlXML;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

public class NodeExtractorWiki {

	public static void main(String argv[]) throws IOException, InterruptedException {

		File file = new File("test/wiki-stubs.txt");
		File outFiles=new File("outputWikiStubs");
		FileUtils.deleteQuietly(outFiles);
		outFiles.mkdir();
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
		BufferedWriter bw = null;
		File URLMapper=new File("url-stubs.txt");
		URLMapper.createNewFile();
		//FileWriter fw2=new FileWriter(URLMapper.getAbsoluteFile());
		BufferedWriter bw2=new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(URLMapper.getAbsolutePath()),"UTF-8"));
		for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
			//String tagname = ((Element) n).getAttribute("sectionName");
			//System.out.println(n.getNodeName()+n.getAttributes());
			String title="";
			if(((Element) n).getNodeName().contentEquals("doc"))
			{
				String titleValue = ((Element) n).getAttribute("title");
				title=titleValue;
				System.out.println("Title level:"+title);
				titleValue=titleValue.replace("^\\.+", "").replaceAll("[\\\\/:*?\"<>|]", "");
				writeFile=new File(outFiles+"/"+titleValue.replaceAll(" ", "_")+".txt");
				writeFile.createNewFile();
				//Thread.sleep(100);
				fw=new FileWriter(writeFile.getAbsoluteFile());
				bw=new BufferedWriter(fw);
				System.out.println("Title is:"+titleValue);
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
				String sectionName = ((Element) n).getAttribute("sectionName");
				//System.out.println(bw.toString());
				fw=new FileWriter(writeFile.getAbsoluteFile(),true);
				bw=new BufferedWriter(fw);
				//System.out.println(sectionName + "=" + getFirstLevelTextContent((Element) n));
				//bw.append(sectionName + "||" + getFirstLevelTextContent(n)+"\n");
				bw.append(getFirstLevelTextContent(n));
				bw.close();
				//continue;
			}

			//bw.flush();

		}
		bw2.close();
		bw.close();
		//bw.close();

	}


	public static void writeFilesInText(String categoryName) throws IOException, InterruptedException 
	{
		File[] processedFiles = new File("inputFiles/rawFiles/processedXML/"+categoryName+"-xml").listFiles();
		new File("inputFiles/rawFiles/urlStore/").mkdirs();
		File URLMapper=new File("inputFiles/rawFiles/urlStore/"+categoryName+"-url");
		URLMapper.createNewFile();
		File outTxtDir=new File("inputFiles/rawFiles/processedTxt/"+categoryName+"-txt");
		FileUtils.deleteQuietly(outTxtDir);
		outTxtDir.mkdirs();
		BufferedWriter bw2=new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(URLMapper.getAbsolutePath()),"UTF-8"));
		List<String> titleTracker=new ArrayList<String>();
		for(File file:processedFiles)
		{
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
			NodeIterator iterator = traversal.createNodeIterator(domDoc.getDocumentElement(), 
					NodeFilter.SHOW_ELEMENT, null, true);
			File writeFile=null;
			FileWriter fw = null;
			BufferedWriter bw=null;
			URLMapper.createNewFile();
			//FileWriter fw2=new FileWriter(URLMapper.getAbsoluteFile());

			for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
				//String tagname = ((Element) n).getAttribute("sectionName");
				//System.out.println(n.getNodeName()+n.getAttributes());
				String title="";
				if(((Element) n).getNodeName().contentEquals("doc"))
				{
					String titleValue = ((Element) n).getAttribute("title");
					title=titleValue;
					//System.out.println("Title level:"+title);
					titleValue=titleValue.replace("^\\.+", "").replaceAll("[\\\\/:*?\"<>|]", "");
					if(titleTracker.contains(titleValue))
						continue;
					else
						titleTracker.add(titleValue);
					writeFile=new File(outTxtDir+"/"+titleValue.replaceAll(" ", "_")+".txt");
					writeFile.createNewFile();
					//Thread.sleep(100);
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

				//bw.flush();

				//bw.close();
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
}



