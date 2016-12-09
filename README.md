<h1 align=center>WikiKreator Project</h1>

1. The program allows generation of Content for Wikipedia articles. 

2. The main method is in the class ExistingArticleGenerator in edu.sidd.StubImprover package. 

3. There is a properties file that needs editing -- dir.config in the base directory. 
The "categoryForTraining" property refers to the Wikipedia category from which articles would be extracted to train the classifier.
The "categoryToGenerate" property refers to the Wikipedia category, the stubs/articles of which you wish to generate.
Both the above categories can be same, in that case the software generates pages that already exist on Wikipedia.
The LDA model requires a category with enough training data to generate reasonable features.
The category name should exactly be the way it is on Wikipedia , along with the underscores (_). For example, "Science_fiction_films" and not "Science fiction films".
The code requires python 2 and 3 too for some third-party software.
Please change the python executables by changing "python2dir" and "python3dir".
Number of articles to generate is set using "numFilesToGenerate". Default is 20.
The classifier only assigns to sections based on the confidence. The min confidence can be set using "cfThreshold" parameter.

Note: The requirement for both python distributions can be avoided and I will modify the code soon. 

****************************************************************************************************************
####IMPORTANT: DO THIS BEFORE YOU PROCEED#####
4. System Requirements:
This code will run on Java 1.7
Maven is required to compile.
In addition, it requires GUROBI the optimization solver: http://www.gurobi.com/ on the machine it runs. It cannot be shipped in this software due to licensing regulations.
Place gurobi.jar in lib directory. Maven should take care of the compilation. 
We also provide a modified version of HTMLUnit with a small change in lib directory. 
For the ILP Model, a language model is also required. Use any ARPA format file. 
We used 3gram Language model from here: http://www.keithv.com/software/csr/
Download the file and put it into the lib/LM directory
Run: mvn install after the libs are included.	
******************************************************************************************************************

5. Run the main class and the code should run: The various steps it does are:
	i. Extract all the data from the category.
	ii. Get the top frequent sections -- set parameter "topFrequentSections" in dir.config (Default: 10)
	iii. Generate Topic Models and Classifiers
	iv. Look into Results folder once the text files are generated -- Results/GeneratedOutput/"categoryToGenerate"/system/
	v. The directory structure is such that it can be easily used for ROUGE evaluation.(system/models) [ROUGE is not shipped with this software]



**Citation**
If you use any components of this code, please cite:
Siddhartha Banerjee and Prasenjit Mitra, "WikiKreator: Improving Wikipedia Stubs Automatically", *In Proceedings of the 53rd Annual Meeting of the Association for Computational Linguistics (ACL) and the 7th International Joint Conference on Natural Language Processing of the AFNLP, Beijing, China, July 2015* 

Please note that all the code is research code, and hence is provided here with any real gurantees. 
