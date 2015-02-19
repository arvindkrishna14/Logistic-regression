import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Arvind Krishna Parthasarathy
 *
 */
public class LRClass {
	private final  int num_iterations = 120;
	private final  String HAM = "ham";
	private final  String SPAM = "spam";
	public HashMap<String , Integer> hamWordCount;

	String rootDir;
	private  boolean removeStopWords = false;
	private  boolean doSmoothing = false; 
	private  Map<String, Map<String, Integer>> hamFileWordMap;
	private  Map<String, Map<String, Integer>> spamFileWordMap;
	private  Map<String, Double> tokenWeightMap;
	private  ArrayList<String> wordList;
	private  int hamFileCount;
	private  int spamFileCount;
	private  int totalFileCount;
	private  Set<String> wordSet;
	private  double[] tokenWeights;
	private  double[] probArray;
	private  ArrayList<String> hamFiles;
	private  ArrayList<String> spamFiles;
	private  double[] dWeights;
	private  ArrayList<String> allFiles = new ArrayList<String>();
	private  Map<String, String> categoryFile = new HashMap<String, String>();
	private  double learningRate = 0.025;
	private  double regConstant = 0.6;

	/**
	 * Method to run logistic regression algorithm
	 * @param stopWords
	 * @param smoothing
	 * @throws IOException
	 */
	public void logisticRegression(boolean stopWords, boolean smoothing) throws IOException{
		removeStopWords = stopWords;
		doSmoothing = smoothing;
		rootDir ="dataset";
		String hamTrainingSetFolderPath = rootDir + File.separator + "train"
				+ File.separator + HAM;
		String spamTrainingSetFolderPath = rootDir + File.separator + "train"
				+ File.separator + SPAM;
		FileUtils.putStopWords(rootDir);


		hamFiles = FileUtils.getListofFiles(hamTrainingSetFolderPath);
		spamFiles = FileUtils.getListofFiles(spamTrainingSetFolderPath);
		tokenWeightMap = new HashMap<String, Double>();
		allFiles.addAll(hamFiles);
		for(String hamFile:hamFiles){
			categoryFile.put(hamFile, "HAM");
		}
		allFiles.addAll(spamFiles);
		for(String spamFile:spamFiles){
			categoryFile.put(spamFile, "SPAM");
		}
		hamFileWordMap = new HashMap<String, Map<String, Integer>>();
		spamFileWordMap = new HashMap<String, Map<String, Integer>>();
		populateData(hamFiles, spamFiles);
		classifyAccuracy();
	}

	/**
	 * Populates the data matrix
	 * @param hamFiles
	 * @param spamFiles
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void populateData(ArrayList<String> hamFiles, ArrayList<String> spamFiles) throws IOException{
		hamFileCount = hamFiles.size();
		spamFileCount = spamFiles.size();
		totalFileCount = hamFileCount + spamFileCount;
		System.out.println("Populating data!!!");
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> hamWords = new ArrayList<String>();
		ArrayList<String> spamWords = new ArrayList<String>();
		String hamPath = rootDir + File.separator + "train"
				+ File.separator + HAM;
		String spamPath = rootDir + File.separator + "train"
				+ File.separator + SPAM;
		Iterator it;
		for (String file : hamFiles) {
			hamWordCount = new HashMap<String, Integer>();
			hamFileWordMap.put(file, new HashMap<String, Integer>());
			hamWords = FileUtils.retrieveTokensFromFile(hamPath+File.separator+file, removeStopWords, doSmoothing);
			for(String hamWord: hamWords){
				if(hamFileWordMap.get(file).containsKey(hamWord)){
					int count = hamFileWordMap.get(file).get(hamWord);
					hamFileWordMap.get(file).put(hamWord, count+1 );
				}else{
					hamFileWordMap.get(file).put(hamWord, 1);
				}
			}
			for(String word: hamWords){
				if(hamWordCount.containsKey(word)){
					hamWordCount.put(word, hamWordCount.get(word)+1);
				}
				else{
					hamWordCount.put(word,1);
				}
			}
			if(!doSmoothing){
				words.addAll(hamWords);
			}
			else{
				Object[] b = hamWordCount.entrySet().toArray();
				Arrays.sort(b, new Comparator<Object>() {
					public int compare(Object o1, Object o2) {
						return ((Map.Entry<String, Integer>) o2).getValue().compareTo(
								((Map.Entry<String, Integer>) o1).getValue());
					}
				});
				hamWordCount = new LinkedHashMap<String, Integer>();
				for (Object e : b) {
					hamWordCount.put(((Map.Entry<String, Integer>) e).getKey(),((Map.Entry<String, Integer>) e).getValue());

				}
				it = hamWordCount.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry<String,Integer> pairs = (Map.Entry<String, Integer>)it.next();
					if(pairs.getKey().toString().length()<4){
						it.remove();
					}
				}
				words.addAll(hamWordCount.keySet());
			}

		}
		for (String file : spamFiles) {
			HashMap<String, Integer> spamWordCount = new HashMap<String, Integer>();
			spamFileWordMap.put(file, new HashMap<String, Integer>());
			spamWords = FileUtils.retrieveTokensFromFile(spamPath+File.separator+file, removeStopWords, doSmoothing);
			for(String spamWord: spamWords){
				if(spamFileWordMap.get(file).containsKey(spamWord)){
					int count = spamFileWordMap.get(file).get(spamWord);
					spamFileWordMap.get(file).put(spamWord, count+1 );
				}else{
					spamFileWordMap.get(file).put(spamWord, 1);
				}
			}
			for(String word: spamWords){
				if(spamWordCount.containsKey(word)){
					spamWordCount.put(word, spamWordCount.get(word)+1);
				}
				else{
					spamWordCount.put(word,1);
				}
			}
			if(!doSmoothing){
				words.addAll(spamWords);
			}
			else{
				Object[] b = spamWordCount.entrySet().toArray();
				Arrays.sort(b, new Comparator<Object>() {
					public int compare(Object o1, Object o2) {
						return ((Map.Entry<String, Integer>) o2).getValue().compareTo(
								((Map.Entry<String, Integer>) o1).getValue());
					}
				});
				spamWordCount = new LinkedHashMap<String, Integer>();
				for (Object e : b) {
					spamWordCount.put(((Map.Entry<String, Integer>) e).getKey(),((Map.Entry<String, Integer>) e).getValue());

				}
				it = spamWordCount.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry<String,Integer> pairs = (Map.Entry<String, Integer>)it.next();
					if(pairs.getKey().toString().length()<4){
						it.remove();
					}
				}
				words.addAll(spamWordCount.keySet());
			}

		}
		Set<String> wordsSet = new HashSet<String>();
		for(String word : words){
			wordsSet.add(word);
		}
		wordList = new ArrayList<String>(wordsSet);
		wordList.add(0, "##W0##");
		for (String token : wordList) {
			tokenWeightMap.put(token, Math.random());
		}
		tokenWeights = new double[wordList.size()];
		probArray = new double[totalFileCount];
		System.out.println("Training!!!");
		for(int i = 0; i< num_iterations; i++){
			lr();
		}

	}
	
	/**
	 * Method to calculate probabilities (1/1+e^-x)
	 */
	public  void lr(){
		int i = 0;
		for(String fileName: hamFiles){
			probArray[i] = computeProb(hamFileWordMap.get(fileName));
			i++;
		}
		for(String fileName: spamFiles){
			probArray[i] = computeProb(spamFileWordMap.get(fileName));
			i++;
		}
		updateWeights();

	}

	/**
	 * Updates the token weights
	 */
	private  void updateWeights() {
		int classValue;
		dWeights = new double[wordList.size()];
		for(int i = 0; i < wordList.size(); i++){
			for(int j = 0; j < allFiles.size(); j++){
				if(getMap(allFiles.get(j)).get(wordList.get(i))!=null){
					if(categoryFile.get(allFiles.get(j)).equals("HAM"))
						classValue = 1;
					else
						classValue = 0;
					dWeights[i] = getMap(allFiles.get(j)).get(wordList.get(i)) * (classValue - probArray[j]);
				}
			}
		}
		for(int i=0; i<dWeights.length; i++){
			tokenWeights[i] += learningRate * (dWeights[i] - (regConstant * tokenWeights[i]));
		}
	}

	
	public  Map<String, Integer> getMap (String fileName){
		if(categoryFile.get(fileName).equals("HAM")){
			return hamFileWordMap.get(fileName);
		}
		else{
			return spamFileWordMap.get(fileName);
		}
	}

	private  double computeProb(Map<String, Integer> map) {
		double z = 0.0;
		for(int i = 1; i < wordList.size();i++){
			if(map.containsKey(wordList.get(i))){
				z += tokenWeights[i]*map.get(wordList.get(i));
			}
		}
		z+= tokenWeights[0];
		z = Math.exp(-z);
		double prob = (double) 1.0 / (1.0 + z);
		return prob;

	}

	public  void classifyAccuracy() throws IOException{
		int numDocuments = 0;
		int numSpamDocuments = 0;
		int numHamDocuments = 0;
		int numCorrectClassification = 0;
		int numSpamCorrectClassification = 0;
		int numHamCorrectClassification = 0;
		int total = 0;
		System.out.println("Testing!!!");
		int correct = 0;
		String hamTestSetFolderPath = rootDir+ File.separator+"test"+File.separator+HAM;
		String spamTestSetFolderPath = rootDir+ File.separator+"test"+File.separator+SPAM;
		ArrayList<String> hamTestFiles = FileUtils.getListofFiles(hamTestSetFolderPath);
		ArrayList<String> spamTestFiles = FileUtils.getListofFiles(spamTestSetFolderPath);
		numSpamDocuments = spamTestFiles.size();
		numHamDocuments = hamTestFiles.size();
		numDocuments = numSpamDocuments + numHamDocuments;
		for(String hamTestFile:hamTestFiles){
			total++;
			double totalWeight = 0.0;
			ArrayList<String> testWords = new ArrayList<String>();
			testWords = FileUtils.retrieveTokensFromFile(hamTestSetFolderPath+File.separator+hamTestFile, removeStopWords, doSmoothing);
			HashMap<String, Integer> testMap = new HashMap<String, Integer>();
			for(String word: testWords){
				if (testMap.containsKey(word)) {
					testMap.put(word, (testMap.get(word) + 1));
				} else {
					testMap.put(word, 1);
				}
			}
			for(String testWord: testMap.keySet()){
				int x = 0;
				if((x = wordList.indexOf(testWord))>0){
					int count = testMap.get(testWord);
					double weight =tokenWeights[x];
					totalWeight+=  count * weight;	
				}

			}
			totalWeight+=tokenWeights[0];
			if(totalWeight>0.0){
				correct = correct+1;
				numHamCorrectClassification++;
				
			}
		}
		for(String spamTestFile:spamTestFiles){
			total++;
			double totalWeight = 0.0;
			ArrayList<String> testWords = new ArrayList<String>();
			testWords = FileUtils.retrieveTokensFromFile(spamTestSetFolderPath+File.separator+spamTestFile, removeStopWords, doSmoothing);
			HashMap<String, Integer> testMap = new HashMap<String, Integer>();
			for(String word: testWords){
				if (testMap.containsKey(word)) {
					testMap.put(word, (testMap.get(word) + 1));
				} else {
					testMap.put(word, 1);
				}
			}
			for(String testWord: testMap.keySet()){
				int x = 0;
				if((x = wordList.indexOf(testWord))>0){
					int count = testMap.get(testWord);
					double weight =tokenWeights[x];
					totalWeight+=  count * weight;	
				}

			}
			totalWeight+=tokenWeights[0];
			if(totalWeight<=0.0){
				correct = correct+1;
				numSpamCorrectClassification++;
			}
		}
		System.out.println("Accuracy :" + 100
				* (double) correct / (double) numDocuments+"%");
		System.out.println("Precision :"+ 100 *(double) numSpamCorrectClassification 
				/ (double) (numSpamCorrectClassification+(numHamDocuments-numHamCorrectClassification))+"%");
		System.out.println("Recall :"+ 100*(double) numSpamCorrectClassification 
				/ (double) (numSpamCorrectClassification+(numSpamDocuments-numSpamCorrectClassification))+"%");
	}

}
