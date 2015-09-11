package edu.sidd.StubImprover;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLAnchorElement;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;

public class WikiEditCalculator {
	static String wikiUserName="MightyPepper";
	String ucContributionsURL="https://en.wikipedia.org/w/index.php?title=Special:Contributions"+
	"/"+wikiUserName+"&offset=&limit=1000";
	
	private List<String> getEditedLinks() throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		WebClient browser=new WebClient(BrowserVersion.CHROME);
		browser.getOptions().setJavaScriptEnabled(false);
		browser.getOptions().setThrowExceptionOnFailingStatusCode(false);
		browser.getOptions().setThrowExceptionOnScriptError(false);
		HtmlPage rawpage;
		rawpage = browser.getPage(ucContributionsURL);
		List<?> elementList = rawpage.getByXPath("//a[@class='mw-contributions-title']");
		elementList=elementList.subList(0, elementList.size()-10); //remove 10 initial edits: for eligibility
		List<String> articlesEdited=new ArrayList<String>();
		for(int i=0;i<elementList.size();i++)
		{
			HtmlAnchor result = (HtmlAnchor) elementList.get(i);
			String value=(result.getAttributeNode("title").getNodeValue());
			if(!articlesEdited.contains(value) && !value.startsWith("Wiki")
					&& !value.startsWith("User talk"))
			{
				articlesEdited.add(value);
			}
		}
		browser.closeAllWindows();
		return articlesEdited;

	}
	
	private void getEditedList() throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		List<String> articleListContributed=getEditedLinks();
		WebClient browser=new WebClient(BrowserVersion.CHROME);
		browser.getOptions().setJavaScriptEnabled(false);
		browser.getOptions().setThrowExceptionOnFailingStatusCode(false);
		browser.getOptions().setThrowExceptionOnScriptError(false);
		String wikiURL="https://en.wikipedia.org/w/index.php?title=";
		for(String s:articleListContributed)
		{//*[@id="pagehistory"]/li[2]
			String finalURL=wikiURL+s.replaceAll(" ", "_")+"&action=history";
			//System.out.println(finalURL);
			HtmlPage rawpage= browser.getPage(wikiURL+s.replaceAll(" ", "_")+"&action=history");
			@SuppressWarnings("unchecked")
			List<DomNode> totalEditsUsers =(List<DomNode>) rawpage.getByXPath("//span[@class='history-user']");
			//System.out.println(getFirstEditIndex(totalEditsUsers));
			@SuppressWarnings("unchecked")
			List<DomNode> sizeArticles = (List<DomNode>) rawpage.getByXPath("//span[@class='history-size']");
			int currentSize=getContentSize(sizeArticles,0);
			int beforeEditSize=getContentSize(sizeArticles, getFirstEditIndex(totalEditsUsers)+1);
			//System.out.println("Change in Size:"+(currentSize-beforeEditSize));
			int changeInSize=(currentSize-beforeEditSize);
			System.out.println(finalURL+"||"+changeInSize+"||"+getFirstEditIndex(totalEditsUsers));
		}
	
	
	}
	
	private static int getContentSize(List<DomNode> sizeArticles, int index)
	{
		int i=0;
		String sizeInBytes=sizeArticles.get(index).getChildNodes().get(0).asText();
		Pattern pattern = Pattern.compile("\\((.*?) bytes");	
		//System.out.println(sizeInBytes);
		Matcher m=pattern.matcher(sizeInBytes);
		if(m.find())
		{
			String bytes=m.group(1).replaceAll(",","");
			i=Integer.parseInt(bytes);
		}
		return i;
	}
	
	private static int getFirstEditIndex(List<DomNode> totalEditsUsers)
	{
		int i=0;
		for(DomNode d:totalEditsUsers)
		{
			String collaboratorName=d.getChildNodes().get(0).asText();
			if(collaboratorName.contentEquals(wikiUserName))
			{
				i=totalEditsUsers.indexOf(d);
			}
			
		}
		return i;
		
	}
	
	
	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		
		WikiEditCalculator wec=new WikiEditCalculator();
		//List<String> articleList=wec.getEditedLinks();
		//System.out.println(articleList.size());
		wec.getEditedList();
	}
	
}
