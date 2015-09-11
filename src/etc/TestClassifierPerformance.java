package etc;

import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;

public class TestClassifierPerformance {

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
		return eval.weightedFMeasure();
	}
	
	public static void main(String[] args) throws Exception {
		String categoryName="American_mathematicians";
		
		System.err.println("Category:"+categoryName);
		for(int i=10;i<=100;i=i+10)
		{
			double score=TestClassifierPerformance.getClassifierFScore(i, categoryName);
			System.out.println(score);
		}
		
//		categoryName="Academics_by_nationality";
//		
//		System.err.println("Category:"+categoryName);
//		for(int i=10;i<=100;i=i+10)
//		{
//			double score=TestClassifierPerformance.getClassifierFScore(i, categoryName);
//			System.out.println(score);
//		}
//		
//		
//	categoryName="Information_technology_companies_of_the_United_States";
//		
//		System.err.println("Category:"+categoryName);
//		for(int i=10;i<=100;i=i+10)
//		{
//			double score=TestClassifierPerformance.getClassifierFScore(i, categoryName);
//			System.out.println(score);
//		}
//		
//	}
	}
}
