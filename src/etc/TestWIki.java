package etc;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.bliki.api.*;

public class TestWIki {

//public static void main(String[] args) throws MalformedURLException {
//	
//	String[] listOfTitleStrings = {"Eric Khoo Heng-Pheng" };
//    User user = new User("", "", "http://en.wikipedia.org/w/api.php");
//    user.login();
//    List<Page> listOfPages = user.queryContent(listOfTitleStrings);
//    for (Page page : listOfPages) {
//        // print page information
//       // System.out.println(page.toString());
//       //System.out.println(page.toString());
//    System.out.println(page.getPageid());
//    System.out.println(extractUrls(page.toString()));
////       Pattern p= Pattern.compile("<ref>(.*?)</ref>");
////       Matcher matcher = p.matcher(page.toString());
////       while (matcher.find()) {
////    	      System.out.print("Start index: " + matcher.start());
////    	      System.out.print(" End index: " + matcher.end() + " ");
////    	      System.out.println(matcher.group());
////    	    }
//    }
//
//}
	public static void main(String[] args) {
		
		String r="'i (wanted) to do it.";
		r=r.replaceAll("\\(.*\\)","");
		System.out.println(r);
		if(r.matches("^[a-z0-9].*$"))
		{
			System.out.println("satis");
		}
			
		
		
	}


public static List<String> extractUrls(String input) throws MalformedURLException {
    List<String> result = new ArrayList<String>();

    Pattern pattern = Pattern.compile(
        "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" + 
        "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" + 
        "|mil|biz|info|mobi|name|aero|jobs|museum" + 
        "|travel|[a-z]{2}))(:[\\d]{1,5})?" + 
        "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" + 
        "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + 
        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" + 
        "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + 
        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" + 
        "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

    Matcher matcher = pattern.matcher(input);
    while (matcher.find()) {
    	String url=matcher.group();
    	URL resource=new URL(url);
    	System.out.println(resource.getHost());
        result.add(matcher.group());
    }

    return result;
}

}
