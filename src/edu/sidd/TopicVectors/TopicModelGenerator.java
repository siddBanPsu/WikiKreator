package edu.sidd.TopicVectors;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import edu.sidd.Objects.TopicInferenceObject;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class TopicModelGenerator {

	static String ARFFDir="inputFiles/ARFFFiles";

	public static void main(String[] args) throws Exception {

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File("inputFiles/stoplists/en.txt"), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );

		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File("inputFiles/corpus.diseases.txt")), "UTF-8");
		instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
				3, 2, 1)); // data, label, name fields

		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		//  Note that the first parameter is passed as the sum over topics, while
		//  the second is 
		int numTopics = 40;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.1, 0.01);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		//  statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only, 
		//  for real applications, use 1000 to 2000 iterations)
		//model.setNumIterations(numIterations)
		model.setNumIterations(2000);
		model.estimate();

		// Show the words and topics in the first instance

		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = instances.getDataAlphabet();

		//		FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
		//		LabelSequence topics = model.getData().get(0).topicSequence;

		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		//		for (int position = 0; position < tokens.getLength(); position++) {
		//			out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
		//		}
		//		System.out.println(out);

		// Estimate the topic distribution of the first instance, 
		//  given the current Gibbs state.
		double[] topicDistribution = model.getTopicProbabilities(0);

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

			out = new Formatter(new StringBuilder(), Locale.US);
			out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
			int rank = 0;
			while (iterator.hasNext() && rank < 5) {
				IDSorter idCountPair = iterator.next();
				out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}
			System.out.println(out);
		}

		// Create a new instance with high probability of topic 0
		//		StringBuilder topicZeroText = new StringBuilder();
		//		Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();
		//
		//		int rank = 0;
		//		while (iterator.hasNext() && rank < 5) {
		//			IDSorter idCountPair = iterator.next();
		//			topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
		//			rank++;
		//		}

		Instances       data;
		FastVector atts=getAttributes(numTopics, getDistinctLabels(instances));
		// Create a new instance named "test instance" with empty target and source fields.
		//InstanceList testing = new InstanceList(instances.getPipe());
		//System.out.println("Data:"+instances.get(500).get());
		data=new Instances("Training", atts, 0);
		//System.out.println(data.numAttributes() +" attributes....");
		TopicInferencer inferencer = model.getInferencer();
		//StringBuilder sb=new StringBuilder();
		for(int j=0;j<instances.size();j++)
		{
			double[] vals = new double[data.numAttributes()]; //new data
			double[] testProbabilities=inferencer.getSampledDistribution(instances.get(j), 10, 1, 5);
			for(int k=0;k<testProbabilities.length;k++)
			{
				//sb.append(testProbabilities[k]+",");
				vals[k]=testProbabilities[k];
			}

			vals[data.numAttributes()-1]=getListIndex(getDistinctLabels(instances),instances.get(j).getName().toString());
			//sb.append(instances.get(j).getName()+"\n");
			data.add(new Instance(1.0,vals));
		}

		//System.out.println(sb.toString());
		File f=new File(ARFFDir+"/Sections_"+numTopics+".ARFF");
		f.createNewFile();
		FileWriter fw=new FileWriter(f);
		BufferedWriter bw=new BufferedWriter(fw);
		bw.write(data.toString());
		bw.close();

		//		double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
		//		System.out.println("0\t" + testProbabilities[0]);
	}

	static String baseDir="C:\\Work\\Projects\\WikipediaTemplate\\"
			+ "codes\\Sweble\\WikipediaTemplateGenerator\\";

	public static TopicInferenceObject getTopicInferenceObject() throws Exception
	{
		TopicInferenceObject tio=new TopicInferenceObject();
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File(baseDir+"inputFiles/stoplists/en.txt"), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );


		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(baseDir+"inputFiles/corpus.diseases.txt")), "UTF-8");
		instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
				3, 2, 1));

		int numTopics = 40;
		double alpha=(double)numTopics * 0.01;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, alpha, 0.01);


		//##Currently using a saved topic model##
		//		
		//		model.addInstances(instances);
		//
		//		// Use two parallel samplers, which each look at one half the corpus and combine
		//		//  statistics after every iteration.
		//		model.setNumThreads(6);
		//
		//		// Run the model for 50 iterations and stop (this is for testing only, 
		//		//  for real applications, use 1000 to 2000 iterations)
		//		//model.setNumIterations(2000); //Please uncomment this
		//		model.setNumIterations(2000);
		//		model.estimate();
		//		model.write(new File(baseDir+"/"+"LDAModel.bin"));
		//##Currently using a saved topic model##
		model=ParallelTopicModel.read(new File(baseDir+"/"+"LDAModel.bin"));
		Instances       data;
		FastVector atts=getAttributes(numTopics, getDistinctLabels(instances));
		// Create a new instance named "test instance" with empty target and source fields.
		//InstanceList testing = new InstanceList(instances.getPipe());
		//System.out.println("Data:"+instances.get(500).get());
		data=new Instances("Training", atts, 0);
		//System.out.println(data.numAttributes() +" attributes....");
		TopicInferencer inferencer = model.getInferencer();
		//StringBuilder sb=new StringBuilder();
		System.out.println("Num attributes:"+data.numAttributes());
		for(int j=0;j<instances.size();j++)
		{
			double[] vals = new double[data.numAttributes()]; //new data
			double[] testProbabilities=inferencer.getSampledDistribution(instances.get(j), 100, 1, 5);
			for(int k=0;k<testProbabilities.length;k++)
			{
				//sb.append(testProbabilities[k]+",");
				vals[k]=testProbabilities[k];
			}

			vals[data.numAttributes()-1]=getListIndex(getDistinctLabels(instances),instances.get(j).getName().toString());
			//System.out.println("Label:"+instances.get(j).getName().toString());
			//vals[data.numAttributes()-1]=
			//		data.attribute(data.numAttributes()-1).
			//		addStringValue(instances.get(j).getName().toString());
			//sb.append(instances.get(j).getName()+"\n");
			//System.out.println(vals[39]);
			data.add(new Instance(1.0,vals));
		}

		//System.out.println(sb.toString());

		//		double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
		//		System.out.println("0\t" + testProbabilities[0]);

		tio.setInstances(instances);
		tio.setTopicInferencer(inferencer);
		File f=new File(baseDir+ARFFDir+"/Sections_"+numTopics+".ARFF");
		f.createNewFile();
		FileWriter fw=new FileWriter(f);
		BufferedWriter bw=new BufferedWriter(fw);
		bw.write(data.toString());
		bw.close();


		return tio;

	}
	
	public static TopicInferencer getBestTopicModelInferer(String categoryName, int numTopics) throws Exception
	{
		File dirLDA=new File("inputFiles/rawFiles/LDA-models/"+categoryName+"-LDA");
		ParallelTopicModel model = ParallelTopicModel.read(new File(dirLDA.getAbsolutePath()+
				"/"+categoryName+"-"+numTopics+".LDAModel.bin"));
		TopicInferencer inferencer = model.getInferencer();
		System.err.println("Loaded Topic Inferencer");
		return inferencer;
	}
	

	//generate topic model
	public static void generateTopicModels(String categoryName) throws Exception
	{
		File dirLDA=new File("inputFiles/rawFiles/LDA-models/"+categoryName+"-LDA");
		dirLDA.mkdirs();
		File dirARFF=new File("inputFiles/rawFiles/ARFF-files/"+categoryName+"-ARFF");
		dirARFF.mkdirs();
		TopicInferenceObject tio=new TopicInferenceObject();
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File("lib/stoplists/en.txt"), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );


		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File("inputFiles/rawFiles/corpus/"+categoryName+".corpus.txt")), "UTF-8");
		instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
				3, 2, 1));

		int[] numTopics = {10,20,30,40,50,60,70,80,90,100};
		for(int numTopic:numTopics)
		{
			double alpha=(double)numTopic * 0.01;
			ParallelTopicModel model = new ParallelTopicModel(numTopic, alpha, 0.01);

			//##Currently using a saved topic model##
			//		
			model.addInstances(instances);
			// Use two parallel samplers, which each look at one half the corpus and combine
			//  statistics after every iteration.
			model.setNumThreads(6);
			// Run the model for 50 iterations and stop (this is for testing only, 
			//  for real applications, use 1000 to 2000 iterations)
			//model.setNumIterations(2000); //Please uncomment this
			model.setNumIterations(2000);
			model.estimate();
			model.write(new File(dirLDA.getAbsolutePath()+"/"+categoryName+"-"+numTopic+".LDAModel.bin"));
			//##Currently using a saved topic model##
			//model=ParallelTopicModel.read(new File(baseDir+"/"+"LDAModel.bin"));
			Instances       data;
			FastVector atts=getAttributes(numTopic, getDistinctLabels(instances));
			// Create a new instance named "test instance" with empty target and source fields.
			//InstanceList testing = new InstanceList(instances.getPipe());
			//System.out.println("Data:"+instances.get(500).get());
			data=new Instances("Training", atts, 0);
			//System.out.println(data.numAttributes() +" attributes....");
			TopicInferencer inferencer = model.getInferencer();
			//StringBuilder sb=new StringBuilder();
			
			//System.out.println("Num attributes:"+data.numAttributes());
			for(int j=0;j<instances.size();j++)
			{
				double[] vals = new double[data.numAttributes()]; //new data
				double[] testProbabilities=inferencer.getSampledDistribution(instances.get(j), 100, 1, 5);
				for(int k=0;k<testProbabilities.length;k++)
				{
					//sb.append(testProbabilities[k]+",");
					vals[k]=testProbabilities[k];
				}

				vals[data.numAttributes()-1]=getListIndex(getDistinctLabels(instances),instances.get(j).getName().toString());
				//System.out.println("Label:"+instances.get(j).getName().toString());
				//vals[data.numAttributes()-1]=
				//		data.attribute(data.numAttributes()-1).
				//		addStringValue(instances.get(j).getName().toString());
				//sb.append(instances.get(j).getName()+"\n");
				//System.out.println(vals[39]);
				data.add(new Instance(1.0,vals));
			}

			//System.out.println(sb.toString());

			//		double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
			//		System.out.println("0\t" + testProbabilities[0]);

			tio.setInstances(instances);
			tio.setTopicInferencer(inferencer);
			File f=new File(dirARFF+"/"+categoryName+"-"+numTopic+".ARFF");
			f.createNewFile();
			FileWriter fw=new FileWriter(f);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(data.toString());
			bw.close();
		}

			//return tio;

		}


		private static int getListIndex(List<String> list, String current)
		{
			for(String s: list)
			{
				if(s.contentEquals(current))
				{
					return list.indexOf(s);
				}
			}

			return 100;
		}


		public static List<String> getDistinctLabels(InstanceList il)
		{
			List<String> list=new ArrayList<String>();
			for(int j=0;j<il.size();j++)
			{
				if(!list.contains(il.get(j).getName()))
				{
					list.add(il.get(j).getName().toString());
				}

			}
			Collections.sort(list);
			return list;
		}

		public static FastVector getAttributes(int numTopics, List<String> labels)
		{
			FastVector      atts;
			atts = new FastVector();
			//atts.addElement(new Attribute("dialogueID", (FastVector) null))
			for(int i=0;i<numTopics;i++)
			{
				atts.addElement(new Attribute("Topic_"+i));
			}

			FastVector classValues = new FastVector(labels.size());
			Collections.sort(labels);
			for(String s:labels)
			{
				classValues.addElement(s);
			}
			//classValues.addElement("neg");
			atts.addElement(new Attribute("Class",classValues));
			return atts;
		}

	}