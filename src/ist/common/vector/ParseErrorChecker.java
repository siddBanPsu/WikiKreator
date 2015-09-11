package ist.common.vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class ParseErrorChecker {

	static String s="I am available";
	
	private final static String PCG_MODEL = "lib/englishPCFG.ser.gz"; 
	static String fileDir="files/inputDir/DPP";
	public static void main(String[] args) throws IOException {
		
		LexicalizedParser parser = LexicalizedParser.loadModel();
		File[] files=new File(fileDir).listFiles();
		int num=0;
		double score=0.0;
		for(File f:files)
		{
			if(f.isDirectory())
				continue;
			BufferedReader br=new BufferedReader(new FileReader(f.getAbsolutePath()));
			String line="";
			while((line=br.readLine())!=null)
			{
				StringTokenizer st=new StringTokenizer(line, "\\.");
				while(st.hasMoreTokens())
				{
					score=score+parser.parse(st.nextToken()).score();
					num++;
				}
				
			}
			System.out.println(score+" for "+f.getName());
			br.close();
			
		}
		
		System.out.println(score+"/"+num);
		
		System.out.println(parser.parse(s).score());

		
	}
	
	
}
