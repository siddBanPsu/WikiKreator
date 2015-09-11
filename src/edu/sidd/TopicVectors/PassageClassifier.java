package edu.sidd.TopicVectors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.sidd.Objects.TopicInferenceObject;
import edu.sidd.Objects.URLContentObject;
import edu.sidd.StubImprover.WikiHelper;

public class PassageClassifier {

	private static final double CONFIDENCE_THRESHOLD = 0.2;//prediction confidence
	String ARFFFile="inputFiles/ARFFFiles/Sections_40.ARFF";
	static String baseDir="C:\\Work\\Projects\\WikipediaTemplate\\"
			+ "codes\\Sweble\\WikipediaTemplateGenerator\\";
	public RandomForest trainClassifier() throws Exception
	{
		int numTopics=40;
		int seed  = 1;
		int folds = 10;
		DataSource trainSource = new DataSource(baseDir+"inputFiles/ARFFFiles/Sections_"+numTopics+".ARFF");
		Instances trainingSet = trainSource.getDataSet();
		if (trainingSet.classIndex() == -1)
			trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

		// Resample for minority class
		Resample reSample=new Resample();
		reSample.setInputFormat(trainingSet);
		//reSample.s(1);
		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
		Random rand = new Random(seed);
		trainingSet.randomize(rand);
		if (trainingSet.classAttribute().isNominal())
			trainingSet.stratify(folds);

		RandomForest classifier=new RandomForest();

		System.out.println("Training with "+classifier.getClass().getName());
		System.out.println(trainingSet.numInstances());
		//classifier.buildClassifier(trainingSet);
		// perform cross-validation
		//Object[] obj={"hello"};
		Evaluation eval = new Evaluation(trainingSet);
		//Object[] forPredictionsPrinting = {"a","10","true"};
		eval.crossValidateModel(classifier, trainingSet, 10, new Random(1), new Object[] { });
		//eval.crossValidateModel(classifier, trainingSet, 10, new Random(1));
		//		for (int n = 0; n < folds; n++) {
		//			System.out.println("Running fold:"+n);
		//			Instances train = trainingSet.trainCV(folds, n);
		//			Instances test = trainingSet.testCV(folds, n);
		//
		//			// build and evaluate classifier
		//			classifier = (RandomForest) Classifier.makeCopy(classifier);
		//			classifier.buildClassifier(train);
		//			eval.evaluateModel(classifier, test);
		//			
		//		}

		System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===\n", false));
		System.out.println(eval.toClassDetailsString()+"\n"+eval.toMatrixString()+"\n");
		classifier.buildClassifier(trainingSet);
		return classifier;

	}

	public static RandomForest getRFBestClassifier(int numTopics, String categoryName) throws Exception
	{
		
		int seed  = 1;
		int folds = 10;
		DataSource trainSource = new DataSource("inputFiles/rawFiles/ARFF-files/"+categoryName+"-ARFF/"
				+categoryName+"-"+numTopics+".ARFF");
		Instances trainingSet = trainSource.getDataSet();
		if (trainingSet.classIndex() == -1)
			trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

		// Resample for minority class
		Resample reSample=new Resample();
		reSample.setInputFormat(trainingSet);
		//reSample.s(1);
		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
		Random rand = new Random(seed);
		trainingSet.randomize(rand);
		if (trainingSet.classAttribute().isNominal())
			trainingSet.stratify(folds);

		RandomForest classifier=new RandomForest();

		//System.out.println("Training with "+classifier.getClass().getName());
		//System.out.println(trainingSet.numInstances());
		//classifier.buildClassifier(trainingSet);
		// perform cross-validation
		//Object[] obj={"hello"};
		Evaluation eval = new Evaluation(trainingSet);
		//Object[] forPredictionsPrinting = {"a","10","true"};
		eval.crossValidateModel(classifier, trainingSet, 10, new Random(1), new Object[] { });
		//eval.crossValidateModel(classifier, trainingSet, 10, new Random(1));
		//		for (int n = 0; n < folds; n++) {
		//			System.out.println("Running fold:"+n);
		//			Instances train = trainingSet.trainCV(folds, n);
		//			Instances test = trainingSet.testCV(folds, n);
		//
		//			// build and evaluate classifier
		//			classifier = (RandomForest) Classifier.makeCopy(classifier);
		//			classifier.buildClassifier(train);
		//			eval.evaluateModel(classifier, test);
		//			
		//		}
		File f=new File("inputFiles/rawFiles/ARFF-files/"+categoryName+
				"-ARFF/"+categoryName+"-"+numTopics+".txt");
		BufferedWriter bw2=new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(f.getAbsolutePath()),"UTF-8"));
		bw2.write(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===\n", false));
		bw2.write(eval.toClassDetailsString()+"\n"+eval.toMatrixString()+"\n");
		bw2.close();
		classifier.buildClassifier(trainingSet);
		System.err.println("Loaded classifier.....");
		return classifier;

	}

	
	
	public static double getClassifierFScore(int numTopics, String categoryName) throws Exception
	{
		//int numTopics=40;
		int seed  = 1;
		int folds = 10;
		DataSource trainSource = new DataSource("inputFiles/rawFiles/ARFF-files/"+categoryName+"-ARFF/"
						+categoryName+"-"+numTopics+".ARFF");
		Instances trainingSet = trainSource.getDataSet();
		if (trainingSet.classIndex() == -1)
			trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

		// Resample for minority class
		Resample reSample=new Resample();
		reSample.setInputFormat(trainingSet);
		//reSample.s(1);
		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
//		trainingSet=Filter.useFilter(trainingSet, reSample);
		Random rand = new Random(seed);
		trainingSet.randomize(rand);
		if (trainingSet.classAttribute().isNominal())
			trainingSet.stratify(folds);

		RandomForest classifier=new RandomForest();

		//System.out.println("Training with "+classifier.getClass().getName());
		//System.out.println(trainingSet.numInstances());
		//classifier.buildClassifier(trainingSet);
		// perform cross-validation
		//Object[] obj={"hello"};
		Evaluation eval = new Evaluation(trainingSet);
		//Object[] forPredictionsPrinting = {"a","10","true"};
		eval.crossValidateModel(classifier, trainingSet, 10, new Random(1), new Object[] { });
		//eval.crossValidateModel(classifier, trainingSet, 10, new Random(1));
		//		for (int n = 0; n < folds; n++) {
		//			System.out.println("Running fold:"+n);
		//			Instances train = trainingSet.trainCV(folds, n);
		//			Instances test = trainingSet.testCV(folds, n);
		//
		//			// build and evaluate classifier
		//			classifier = (RandomForest) Classifier.makeCopy(classifier);
		//			classifier.buildClassifier(train);
		//			eval.evaluateModel(classifier, test);
		//			
		//		}

		//System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===\n", false));
		//System.out.println(eval.toClassDetailsString()+"\n"+eval.toMatrixString()+"\n");
		return eval.weightedFMeasure();
	}
	
	
	public static int getBestNumTopic(String categoryName) throws Exception
	{
		double min=-0.005;
		int index=-10;
		for(int i=10;i<=90;i=i+10)
		{
			double value=getClassifierFScore(i,categoryName);
			//System.out.println("Value for:"+i+"/"+value);
			if(value>min)
			{
				min=value;
				index=i;
			}
		}
		return index;
	
	}
	

	public static HashMap<String, List<URLContentObject>> testInstances(RandomForest classifier, 
			TopicInferenceObject topicInferenceObject,
			HashMap<String,List<String>> urlTexts) throws Exception
			{
		//		String instanceString="The adrenals are two small glands that look like mushroom caps. " +
		//				"One adrenal gland sits above each kidney. " +
		//				"These glands are 1-2 inches long and weigh only 1.5-2.5 grams." +
		//				" Even though they are small, the adrenal glands are very important. " +
		//				"They make several hormones that are needed for well being and normal body functioning. " +
		//				"The adrenal hormones play a major role in regulating metabolism and immunity; maintaining blood pressure, body water and minerals; " +
		//				"and helping the body respond and adapt to stress.";

		//String instanceString="ymptoms of the resultant hyperthyroidism are mainly insomnia, hand tremor, hyperactivity, hair loss, excessive sweating, shaking hands, itching, heat intolerance, weight loss despite increased appetite, diarrhea, frequent defecation, palpitations, muscle weakness, and skin warmth and moistness.";
		HashMap<String, List<URLContentObject>> labelledContent=new HashMap<String, List<URLContentObject>>();
		FastVector atts=TopicModelGenerator.getAttributes(40, TopicModelGenerator.getDistinctLabels(topicInferenceObject.getInstances()));
		for (Map.Entry<String, List<String>> entry : urlTexts.entrySet()) {
			String url = entry.getKey();
			List<String> contentList = entry.getValue();

			for(String content:contentList)
			{
				Instances data = new Instances("Test", atts, 0);
				if (data.classIndex() == -1)
					data.setClassIndex(data.numAttributes() - 1);
				InstanceList testing = new InstanceList(topicInferenceObject.getInstances().getPipe());
				testing.addThruPipe(new Instance(content, null, "test instance", null));
				double[] testProbabilities = topicInferenceObject.getTopicInferencer().
						getSampledDistribution(testing.get(0), 100, 1, 5);
				double[] vals = new double[data.numAttributes()]; //
				for(int k=0;k<testProbabilities.length;k++)
				{
					//sb.append(testProbabilities[k]+",");
					vals[k]=testProbabilities[k];
				}
				data.add(new weka.core.Instance(1.0,vals));

				double pred = classifier.classifyInstance(data.instance(0));
				double[] prediction=classifier.distributionForInstance(data.instance(0));

				//output predictions
				//		    	for(int i=0; i<prediction.length; i=i+1)
				//		    	{
				//		    		System.out.println("Probability of class "+
				//	                                data.classAttribute().value(i)+
				//	                               " : "+Double.toString(prediction[i]));
				//		    	}
				//System.out.println("Confidence:"+prediction[(int)pred]);
				//System.out.println("Predicted:"+data.classAttribute().value((int) pred));

				if(prediction[(int)pred]>=CONFIDENCE_THRESHOLD)
				{
					if(labelledContent.containsKey(data.classAttribute().value((int) pred)))
					{
						List<URLContentObject> urlContentList=
								labelledContent.get(data.classAttribute().value((int) pred));
						URLContentObject uro=new URLContentObject();
						uro.setContent(content);
						uro.setURL(url);
						urlContentList.add(uro);
						labelledContent.put(data.classAttribute().value((int) pred),
								urlContentList);

					}

					else
					{
						List<URLContentObject> urlContentList=new ArrayList<URLContentObject>();
						URLContentObject uro=new URLContentObject();
						uro.setContent(content);
						uro.setURL(url);
						urlContentList.add(uro);
						labelledContent.put(data.classAttribute().value((int) pred),
								urlContentList);
					}

				}

			}
		}
		return labelledContent;

			}

	
	public static HashMap<String, List<URLContentObject>> classifyNewData(RandomForest classifier, 
			TopicInferencer topicInferencer,
			HashMap<String,List<String>> urlTexts, int bestNumTopic, String categoryName) throws Exception
			{
		double cfThreshold=Double.parseDouble(WikiHelper.getSpecificProperty("cfThreshold"));
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
		HashMap<String, List<URLContentObject>> labelledContent=new HashMap<String, List<URLContentObject>>();
		FastVector atts=TopicModelGenerator.getAttributes(bestNumTopic, TopicModelGenerator.getDistinctLabels(instances));
		for (Map.Entry<String, List<String>> entry : urlTexts.entrySet()) {
			String url = entry.getKey();
			List<String> contentList = entry.getValue();
			
			for(String content:contentList)
			{
				Instances data = new Instances("Test", atts, 0);
				if (data.classIndex() == -1)
					data.setClassIndex(data.numAttributes() - 1);
				InstanceList testing = new InstanceList(instances.getPipe());
				testing.addThruPipe(new Instance(content, null, "test instance", null));
				double[] testProbabilities = topicInferencer.
						getSampledDistribution(testing.get(0), 100, 1, 5);
				double[] vals = new double[data.numAttributes()]; //
				for(int k=0;k<testProbabilities.length;k++)
				{
					//sb.append(testProbabilities[k]+",");
					vals[k]=testProbabilities[k];
				}
				data.add(new weka.core.Instance(1.0,vals));

				double pred = classifier.classifyInstance(data.instance(0));
				double[] prediction=classifier.distributionForInstance(data.instance(0));
				
				if(prediction[(int)pred]>=cfThreshold)
				{
					if(labelledContent.containsKey(data.classAttribute().value((int) pred)))
					{
						List<URLContentObject> urlContentList=
								labelledContent.get(data.classAttribute().value((int) pred));
						URLContentObject uro=new URLContentObject();
						uro.setContent(content);
						uro.setURL(url);
						urlContentList.add(uro);
						labelledContent.put(data.classAttribute().value((int) pred),
								urlContentList);

					}

					else
					{
						List<URLContentObject> urlContentList=new ArrayList<URLContentObject>();
						URLContentObject uro=new URLContentObject();
						uro.setContent(content);
						uro.setURL(url);
						urlContentList.add(uro);
						labelledContent.put(data.classAttribute().value((int) pred),
								urlContentList);
					}

				}

			}
		}
		return labelledContent;

			}
	
	

	private static final long serialVersionUID = -5178288489778728847L;
	String stubFiles="inputFiles/outputWikiStubs";
	//static String stubFile="Absent_adrenal_gland";
	//static MauiTopicExtractor topicExtractor = new MauiTopicExtractor();
	//static MauiModelBuilder modelBuilder = new MauiModelBuilder();
	public static String generateQuery(List<String> keyPhraseList, String stubTitle)
	{
		StringBuffer sb=new StringBuffer();

		//sb.append(stubFile.replaceAll("_", " ")+" ");
		sb.append(stubTitle+" ");
		for(String s:keyPhraseList)
		{
			sb.append(s+" ");

		}
		return sb.toString();
	}

	/** Sentence compression specific stuff here!!!! **/
	
	//static String lmModelFile="data/lm_giga_5k_nvp_2gram.arpa";
	
	
//	public static void main(String[] args) throws Exception {
//
//		PassageClassifier pc=new PassageClassifier();
//		TopicInferenceObject tio=TopicModel.getTopicInferenceObject();
//		RandomForest rfClassifier=pc.trainClassifier();
//		SentenceCompressor.loadModels();
//		StubDocumentSearcher sdc=new StubDocumentSearcher();
//		String stubTitle="Agalactia";
//		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
//		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
//		String introString=sdc.getIntroductionString(stubTitle);
//		sdc.loadMAUIComponents(topicExtractor, modelBuilder);
//		List<String> keyphraseList=sdc.getKeyphrases(introString, topicExtractor, modelBuilder);
//		String query = generateQuery(keyphraseList,stubTitle);
//		System.out.println("Google Query:"+query);
//		HashMap<String,List<String>> urlTexts=StubDocumentSearcher.getURLAndTexts(query);
//		HashMap<String, List<URLContentObject>> urlContentLabelled=pc.testInstances(rfClassifier, tio, urlTexts);
//		System.out.println(urlContentLabelled);
//		SummarizeWebData swd=new SummarizeWebData();
//
//		swd.generateTopKList(SummarizeWebData.attachIds(urlContentLabelled.get("Treatment")), query,"Treatment");
//
//
//	}


}
