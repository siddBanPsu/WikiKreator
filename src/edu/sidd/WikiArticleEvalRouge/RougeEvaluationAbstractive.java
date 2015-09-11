package edu.sidd.WikiArticleEvalRouge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * 
 * This is a java class that aids in ROUGE Evaluation
 * Used files from http://kavita-ganesan.com/
 * The folders can be placed anywhere: Here placed under lib directory: prepare4rouge, RELEASE(ROUGE) and 
 * rouge2csv for ROUGE txt to csv results (better readability)
 * 
 * @author sub253
 *
 */

public class RougeEvaluationAbstractive {

	public static String ROUGEDIR="ROUGE";
	public static String ROUGESystemFolder=ROUGEDIR+"/prepare4rouge/system";
	public static String ROUGEgoldFolder=ROUGEDIR+"/prepare4rouge/gold";
	static String systemWikiFolder="Results/wikiFilesSystemGenerated-bak2";
	static String origWikiFolder="Results/wikiFilesCleaned";
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void createInputsForPrepare4Rouge(String inputSystemFolder, String systemId)
	{

		File[] listOfClusters=(new File(inputSystemFolder)).listFiles();
		System.out.println(listOfClusters.length);
		FileUtils.deleteQuietly(new File(ROUGESystemFolder));
		//FileUtils.deleteQuietly(new File(goldFolder));
		new File(ROUGESystemFolder).mkdir();
		//new File(goldFolder).mkdir();
		new File(ROUGESystemFolder+"/"+systemId).mkdir();
		for(File f:listOfClusters)
		{	
			//System.out.println(f.getName());
			if(f.isDirectory())
			{
				continue;
			}
			String wikiPage=f.getName().split("\\.")[0];
			//wikiPage=wikiPage.replaceAll("_", "");
			wikiPage = wikiPage.replaceAll("[^A-Za-z0-9\\[\\]]", "");
			//	System.out.println(meetingId);
			File source = new File(f.getAbsolutePath());
			File desc = new File(ROUGESystemFolder+"/"+systemId+"/"+wikiPage+"."+systemId+".system");
			try {
				FileUtils.copyFile(source, desc);
			} catch (IOException e) {
				e.printStackTrace();
			}

			//System.out.println(goldFile.getName());
		}

		//System.out.println("===============");

	}

	private List<String> getArticleTitles()
	{
		List<String> titleList=new ArrayList<String>();
		File[] allFiles=new File(origWikiFolder).listFiles();
		for(File f:allFiles)
		{
			//System.out.println(f.getName().split("\\.")[0]);
			titleList.add(f.getName().split("\\.")[0]);
		}
		return titleList;
	}



	private void generateModelSummaries() throws IOException
	{

		List<String> wikiArticleTitles=getArticleTitles();
		FileUtils.deleteQuietly(new File(ROUGEgoldFolder));
		new File(ROUGEgoldFolder).mkdir();
		//new File(systemFolder+"/"+systemId).mkdir();
		for(String title:wikiArticleTitles)
		{
			File[] listGoldFiles=getModelSummaries(origWikiFolder, title.trim());
			//System.out.println(listGoldFiles.length);
			//title=title.replaceAll("_", "");
			title = title.replaceAll("[^A-Za-z0-9\\[\\]]", "");
			new File(ROUGEgoldFolder+"/"+title).mkdir();
			int i=0;
			for(File goldFile:listGoldFiles)
			{	i++;
			File sourceGold = new File(goldFile.getAbsolutePath());
			File descGold = new File(ROUGEgoldFolder+"/"+title+"/"+title+"."+i+".gold");
			try {
				FileUtils.copyFile(sourceGold, descGold);
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
		}
	}

	private void generateRougeReadableFiles() throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = new ProcessBuilder("perl",
				"ROUGE/prepare4rouge/prepare4rouge-simple.pl");
		Process p = processBuilder.start();
		p.waitFor(); //wait for python to end
		p.destroy();

	}


	private void runROUGEEvaluation() throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = 
				new ProcessBuilder("perl",
						"ROUGE/RELEASE/runROUGE-test.pl");
		//processBuilder.redirectOutput(new File("test.txt"));
		Process p = processBuilder.start();
		p.waitFor(); //wait for python to end
		p.destroy();


	}

	private void generateEasyReadableFiles() throws IOException, InterruptedException
	{
		FileUtils.deleteQuietly(new File("RESULT_ROUGE.csv"));
		ProcessBuilder processBuilder = 
				new ProcessBuilder("perl",
						"ROUGE/rouge2csv/rouge2csv.pl","Results/ROUGE_RESULTS.txt","Results/ROUGE-CSV/RESULT_testIds");
		//processBuilder.redirectOutput(new File("test.txt"));
		Process p = processBuilder.start();
		p.waitFor(); //wait for python to end
		p.destroy();


	}

	private File[] getModelSummaries(String modelFolder, String clusterName){
		File modelDir, arrSum[];
		modelDir=new File(modelFolder);
		//System.out.println("Cluster Name:"+clusterName);
		arrSum=modelDir.listFiles(new WildCardFilter(clusterName+"*"));
		if(arrSum==null || arrSum.length==0){
			clusterName=clusterName.substring(0,clusterName.length()-1);
			arrSum = modelDir.listFiles(new WildCardFilter(clusterName + "*"));
		}
		return arrSum;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		//System.out.println("Hello");

		RougeEvaluationAbstractive hw=new RougeEvaluationAbstractive();
		System.out.println(hw.getArticleTitles());

		//List<String> allFolders=hw.getAllSystems(systemWikiFolder);
		//		List<String> allFolders=new ArrayList<String>();
		//		allFolders.add("Results/AbstractiveSummaries/output_all");
		//System.out.println("Total number of systems:"+allFolders.size());
		hw.generateModelSummaries();
		//		int count=0;
		//		for(String s:allFolders)
		//		{
		//			count++;
		File dir=new File(systemWikiFolder);
		hw.createInputsForPrepare4Rouge(dir.getAbsolutePath(),"1");
		hw.generateRougeReadableFiles();
		hw.runROUGEEvaluation();
		hw.generateEasyReadableFiles();
		//System.out.println("####Done with "+count +" "+"systems####~~"+s);
		//		}
		//	}


	}
}


