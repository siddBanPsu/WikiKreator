package edu.sidd.ExtractLabels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import edu.sidd.Objects.ArticleDao;
import edu.sidd.StubImprover.WikiHelper;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class LabelContentExtractor {

	String wikiFilesCleanedDir="inputFiles/outputWikiFiles";
	static String urlArticlesMapper="inputFiles/url.txt";
	String[] sectionsRemove={"External links","See also",
			"References","Further reading", "Introduction","Bibliography", "Further Reading",
			"See Also", "External Links"};
	//String[] renameCombinations={"Signs and symptoms||Symptoms","Symptoms and signs||Symptoms",
	//		"Treatments||Treatment","Cause||Causes"};

	//static int topSections=10;
	
	
	private List<ArticleDao> createArticleObjectList(String wikiTxtDir, String urlFile) throws IOException
	{
		File[] files=new File(wikiTxtDir).listFiles(); 
		HashMap<String, String> articleURLs=LabelContentExtractor.loadURLs(urlFile);
		//System.out.println(articleURLs);
		List<ArticleDao> articleDaoList=new ArrayList<ArticleDao>();
		for(File f:files)
		{
			String sCurrentLine;
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
			ArticleDao aDao=new ArticleDao();
			aDao.setArticleName(f.getName().replaceAll("_", " ").split("\\.")[0]);
			aDao.setArticleURL(articleURLs.get(aDao.getArticleName()));
			//System.out.println(aDao.getArticleURL());
			//System.out.println(aDao.getArticleName());
			HashMap<String, String> topicContent=new HashMap<String, String>();
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				String[] components=sCurrentLine.split(Pattern.quote("||"));
				//				for(String rename:renameCombinations)
				//				{
				//					if(components[0].trim().contentEquals(rename.split(Pattern.quote("||"))[0]))
				//					{
				//						components[0]=rename.split(Pattern.quote("||"))[1];
				//					}
				//
				//				}

				if(components.length>1 && components[1].trim().length()>0)
				{
					topicContent.put(components[0], components[1].trim());
				}
			}
			aDao.setSectionContentList(topicContent);
			articleDaoList.add(aDao);
			br.close();
		}
		return articleDaoList;
	}


	public HashMap<String, List<String>> createSectionContents(String wikiTxtDir,String urlFile,
			String categoryName) throws IOException
			{
		//File[] files=new File(wikiFilesCleanedDir).listFiles(); //read the files one by one
		List<ArticleDao> articleDaoList=createArticleObjectList(wikiTxtDir,urlFile);
		//System.out.println(articleDaoList.size());
		HashMap<String, List<String>> topicTexts=new HashMap<String, List<String>>();
		for(ArticleDao aDao:articleDaoList)
		{
			//retrieve the various sections
			Iterator<Entry<String, String>> entries = aDao.getSectionContentList().entrySet().iterator();
			while (entries.hasNext()) {
				Entry<String,String> thisEntry = entries.next();
				String sectionName = thisEntry.getKey();
				String content = thisEntry.getValue();

				if(topicTexts.containsKey(sectionName))
				{
					List<String> currentList=topicTexts.get(sectionName);
					currentList.add(content);
					topicTexts.put(sectionName, currentList);
				}
				else
				{
					List<String> newList=new ArrayList<String>();
					newList.add(content);
					topicTexts.put(sectionName, newList);

				}
			}
		}

		topicTexts=sortByValue(topicTexts);
		for(String removekey:sectionsRemove)
		{
			topicTexts.remove(removekey);
		}
		int topSections=Integer.parseInt(WikiHelper.getSpecificProperty("topFrequentSections"));
		if(topSections==0)
				topSections=10;
//		for (Entry<String, List<String>> entry : topicTexts.entrySet())
//		{
//			System.out.println(entry.getKey() + "/" + entry.getValue().size());
//		}
		//System.err.println("####################");
		topicTexts=topKMap(topicTexts, topSections);
//		for (Entry<String, List<String>> entry : topicTexts.entrySet())
//		{
//			System.out.println(entry.getKey() + "/" + entry.getValue().size());
//		}
		writeCorpus(topicTexts, categoryName);
		return topicTexts;
			}


	public static HashMap<String, List<String>> topKMap(HashMap<String, List<String>>  baseMap, int K)
	{

		List<Map.Entry<String, List<String>>> entryList = new ArrayList<Entry<String, List<String>>>(baseMap.entrySet());
		HashMap<String, List<String>> newMap=new HashMap<String, List<String>>();
		for (Map.Entry<String, List<String>> entry : entryList.subList(0, K)) {
			String sectionName=entry.getKey();
			List<String> list = entry.getValue();
			// Display list of people in City
			newMap.put(sectionName, list);
		}
		return newMap;
	}

	/**
	 * TODO: Keep it for later if tokezining otheruse dont use corenlp if I feel one sentence classification is desired
	 * 
	 * @param sortedMap
	 * @throws IOException
	 */
	private void writeCorpus(HashMap<String, List<String>> sortedMap, String categoryName) throws IOException
	{
		//Properties props = new Properties();
		//props.put("annotators", "tokenize,ssplit");//, pos, lemma, ner");
		//StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		File corpusDir=new File("inputFiles/rawFiles/corpus");
		corpusDir.mkdirs();
		File corpus=new File(corpusDir+"/"+categoryName+".corpus.txt");
		//File corpusWeka=new File("inputFiles/corpus.diseases.arff");
		corpus.createNewFile();
		//corpusWeka.createNewFile();
		FileWriter fw=new FileWriter(corpus.getAbsoluteFile());
		BufferedWriter bw=new BufferedWriter(fw);
		//FileWriter fw2=new FileWriter(corpusWeka.getAbsoluteFile());
		//BufferedWriter bw2=new BufferedWriter(fw2);
		for (Entry<String, List<String>> entry : sortedMap.entrySet())
		{
			//System.out.println(entry.getKey() + "/" + entry.getValue().size());
			for(String s:entry.getValue())
			{
				bw.write(entry.getKey().replaceAll(" ", "_")+"\t"+"X"+"\t"+s+"\n");
				//String resultString = s.replaceAll("[^\\p{L}\\p{Nd}]+", " ");
				//bw2.write(entry.getKey()+",'"+resultString+"'\n");
			}


		}
		//bw2.close();
		bw.close();
	}


	public static HashMap<String, List<String>> sortByValue( HashMap<String, List<String>> map )
	{
		List<Map.Entry<String, List<String>>> list = 
				new LinkedList<Map.Entry<String, List<String>>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<String, List<String>>>()
				{
			public int compare( Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2 )
			{
				if(o1.getValue().size() < o2.getValue().size())
					return 1;
				else if(o1.getValue().size() == o2.getValue().size())
					return 0;
				else
					return -1;
			}
				} );
		HashMap<String, List<String>> result = new LinkedHashMap<String, List<String>>();
		for (Map.Entry<String, List<String>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}


	private static HashMap<String, String> loadURLs(String urlFile) throws IOException
	{
		File file=new File(urlFile); //pass urlArticlesMapper later on
		BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
		String sCurrentLine;
		HashMap<String, String> articleUrl=new HashMap<String, String>();
		while ((sCurrentLine = br.readLine()) != null) {
			//System.out.println(sCurrentLine);
			String[] components=sCurrentLine.split(Pattern.quote("||"));
			articleUrl.put(components[0], components[1]);
		}
		br.close();
		System.out.println("URLs loaded: "+articleUrl.size());
		return articleUrl;

	}

	public static void main(String[] args) throws IOException {

		LabelContentExtractor lce=new LabelContentExtractor();
		//lce.loadURLs();
		String wikiTxtDir="inputFiles/rawFiles/processedTxt/Sleep_disorders-txt";
		String urlFile="inputFiles/rawFiles/urlStore/Sleep_disorders-url";
		String categoryName="Sleep_disorders";
		lce.createSectionContents(wikiTxtDir,urlFile, categoryName);

	}

}
