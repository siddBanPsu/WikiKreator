package etc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DownloadedContent.OnFile;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TestWebClient {

//	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {
//
////		File file=new File("tempFiles");
////		//file.mkdir();
////		
////		System.setProperty("java.io.tmpdir", 
////				file.getCanonicalPath());;
//		//System.out.println(System.getProperty(key));
//		String url="http://www.columbianeurosurgery.org/conditions/cerebral-ischemia/";
//
//		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
//		webClient.getOptions().setJavaScriptEnabled(false);
////		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
//		//webClient.getOptions().setThrowExceptionOnScriptError(false);
////		webClient.getCookieManager().setCookiesEnabled(true);
////		webClient.getOptions().setTimeout(120000);
////		webClient.getOptions().setUseInsecureSSL(true);
//		WebRequest request=new WebRequest(new URL(url));
//		//request.s
//		//webClient.getPage(webWindow, WebResponseData)
//		
//		//webClient.
//		List<String> urlVisited=new ArrayList<String>();
//		System.err.println("URL Link:"+url);
//
//		int status =0;
//		status=webClient.getPage(url).getWebResponse()
//				.getStatusCode();
//
//		System.out.println("Web response status:"+status);
//		//webClient.getOptions().setJavaScriptEnabled(false);
//		String directory="";
//	
//        LinkOption options = null;
//		//Files.getOwner(file.toPath(), options);
//        //System.out.println(file.isDirectory()+"/"+file.canWrite());
//       // boolean bval = file.setWritable(true,false);
//        String currentDirectory = System.getProperty("user.dir");
//        System.out.println(currentDirectory);
//        //System.out.println(bval);
//        HtmlPage page=webClient.getPage(url);
//        page.cleanUp();
//        System.out.println(page.asXml());
//        //Thread.sleep(10000);
//        //System.out.println(rawpage.asXml());
//
//
//	}
	

	public static void main(String[] args) {
		
		Pattern p=Pattern.compile("[^A-Za-z0-9 ]");
		
		String s="I am ikdnsnd m ||,,,....abcdefà";
		
		Matcher m=p.matcher(s);
		int num=0;
		while(m.find())
		{	
			System.out.println(m.group());
			num++;
		}
		System.out.println(num);
	}
	
	
}
