package edu.sidd.crawlXML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;


public class WikiExportSupporter {

//		@Test
//		public void testGooglePageSearchForm() throws InterruptedException, AWTException {
//	        MechanizeAgent agent = new MechanizeAgent();
//	        AbstractDocument page = agent.get("https://en.wikipedia.org/w/index.php?title=Special:Export");
//	        Form form=page.forms().get(0);
//	        //System.out.println(form.getNode());
//	        form.get("catname").set("Diseases_and_disorders");
//	      
//	        Resource res=
//	        		agent.doRequest("https://en.wikipedia.org/w/index.php?title=Special:Export").
//	        		add("addcat","").add("catname","Diseases_and_disorders").add("wpDownload","").get();
//	        
//	        //System.out.println(res.asString());
//	        //SubmitButton button=form.findSubmitButton("addcat");
//			//sb.submit();
//	       // System.out.println(form.get("catname").getValue());
//	        //AbstractDocument res=form.submit();
//	       // form.(sb);
//	        Thread.sleep(2000);
//	        System.out.println(page.findAll("input").size());
//	        System.out.println(page.findAll("input").get(5).getAttribute("value"));
//	        Node sb=page.findAll("input").get(5);
//	        form.submit();
//	        //button.submit();
//	        System.out.println(res.getUri());
//	       // res.saveTo(new File("hello.xml"));
//	        page=agent.get(res.getUri());
//	        //System.out.println(res.asString());
//	        System.out.println("TextArea:"+page.forms().get(0).get("pages").getNode().getValue());
//	        //System.out.println(response.asString());
//	    }



	public void getAllArticlesUnderCategory(String categoryName) throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		HtmlPage page = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport&" +
				"addcat=&catname="+categoryName+"&wpDownload=");
		System.out.println(page.getUrl());
		HtmlForm form = page.getForms().get(0);
		System.out.println(form.getAttribute("method"));
		System.out.println(page.getTitleText());
		HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Export").get(0);
		//Retrive the page names
		List<String> categoryList=new ArrayList<String>();
		List<String> articleNameList=new ArrayList<String>();
		//get content from textarea
		String textArea=(page.getElementByName("pages").asText());
		String[] lines = textArea.split(System.getProperty("line.separator"));
		//System.out.println(lines.length);
		for(String line:lines)
		{
			if(line.startsWith("Category:") && !line.contains("stubs"))
			{
				categoryList.add(line);
			}
			else
			{
				articleNameList.add(line);
			}
		}

		System.out.println(categoryList.size());
		categoryList=searchCategoriesUntilDepth(categoryList);
		System.out.println("Exploring "+categoryList.size()+" categories.......");
		extractArticles(categoryList);
		System.out.println(button.getAttribute("accesskey"));
				String is = button.click().getWebResponse().getContentAsString();
				File f=new File("filename.xml");
				f.createNewFile();
				FileWriter fw = new FileWriter(f.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(is);
				bw.close();

		//System.out.println(is);
		webClient.closeAllWindows();
	}

	
	private void extractArticles(List<String> categoryList) throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		List<String> articleList=new ArrayList<String>();
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		for(String s:categoryList)
		{
			System.out.println("Running category:"+s+"/"+categoryList.indexOf(s));
			HtmlPage page = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport&" +
					"addcat=&catname="+s.split(":")[1]+"&wpDownload=");
			String textArea=(page.getElementByName("pages").asText());
			String[] lines = textArea.split(System.getProperty("line.separator"));
			for(String line:lines)
			{
				if(!line.startsWith("Category:") && !articleList.contains(line))
				{
					articleList.add(line);
				}
			}
		}
		
		System.err.println("Total articles:"+articleList.size());
		HtmlPage pageFinal = webClient.getPage("https://en.wikipedia.org/w/index.php?title=Special%3AExport");
		pageFinal.getElementByName("pages").setTextContent(listToString(articleList));
		HtmlForm form = pageFinal.getForms().get(0);
		HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Export").get(0);
		String is = button.click().getWebResponse().getContentAsString();
		File f=new File("filename2.xml");
		f.createNewFile();
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(is);
		bw.close();
		
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
						"addcat=&catname="+s.split(":")[1]+"&wpDownload=");
				String textArea=(page.getElementByName("pages").asText());
				String[] lines = textArea.split(System.getProperty("line.separator"));
				//System.out.println(lines.length);
				for(String line:lines)
				{
					if(line.startsWith("Category:"))
					{
						if(!categoryList.contains(line))
						{
							tempCatList.add(line);

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
					categoryList.addAll(tempCatList);
			}
			depth++;
		}

		return categoryList;

	}



}


