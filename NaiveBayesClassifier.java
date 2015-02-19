
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Arvind Krishna Parthasarathy
 * 
 */
public class NaiveBayesClassifier {

	private static Map<String, Double> hamCondProbability;
	private static Map<String, Double> spamCondProbability;
	private static double hamRatio;
	private static double spamRatio;
	private static int numHamDocuments;
	private static int numSpamDocuments;
	private final static String HAM = "ham";
	private final static String SPAM = "spam";
	private static String rootDir;
	private  static boolean removeStopWords = false;
	private  static boolean doSmoothing = false; 

	/**
	 * Wrapper method to run NAive Bayes algorithm
	 * @param rootDir
	 * @throws IOException 
	 */
	public static void runNaiveBayesClassifier(boolean stopWords, boolean smoothing) throws IOException {
		rootDir = "dataset";
		removeStopWords = stopWords;
		doSmoothing = smoothing;
		hamCondProbability = new HashMap<String, Double>();
		spamCondProbability = new HashMap<String, Double>();
		NaiveBayesHelper.putStopWords(rootDir);
		train(rootDir);
		test(rootDir);
		cleanup();
	}


	private static void cleanup() {
		// TODO Auto-generated method stub
		hamCondProbability = null;
		spamCondProbability = null;
	}


	/**
	 * Trains the algorithm using the data
	 * @param rootDir
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void train(String rootDir) {

		System.out.println("Training Phase..");
		String hamTrainingSetFolderPath = rootDir + File.separator + "train"
				+ File.separator + HAM;
		String spamTrainingSetFolderPath = rootDir + File.separator + "train"
				+ File.separator + SPAM;

		Map<Object, Object> hamResult = NaiveBayesHelper
				.processDocuments(hamTrainingSetFolderPath,removeStopWords, doSmoothing);
		Map<Object, Object> spamResult = NaiveBayesHelper
				.processDocuments(spamTrainingSetFolderPath,removeStopWords, doSmoothing);

		Map<String, Integer> hamTokenCntMap = (Map<String, Integer>) hamResult
				.get("TokenCntMap");
		Map<String, Integer> spamTokenCntMap = (Map<String, Integer>) spamResult
				.get("TokenCntMap");

		if(doSmoothing){
			Iterator it = hamTokenCntMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String,Integer> pairs = (Map.Entry<String, Integer>)it.next();
				if(pairs.getKey().toString().length()<4){
					it.remove();
				}
			}
			Iterator it2 = spamTokenCntMap.entrySet().iterator();
			while(it2.hasNext()){
				Map.Entry<String,Integer> pairs2 = (Map.Entry<String, Integer>)it2.next();
				if(pairs2.getKey().toString().length()<4){
					it2.remove();
				}
			}

		}
		int numTokensHam = (Integer) hamResult.get("NumTokens");
		int numTokensSpam = (Integer) spamResult.get("NumTokens");

		numHamDocuments = (Integer) hamResult.get("NumDocuments");
		numSpamDocuments = (Integer) spamResult.get("NumDocuments");
		int totalTrainingDocs = numHamDocuments + numSpamDocuments;

		hamRatio = Math.log((double) numHamDocuments
				/ (double) (totalTrainingDocs));
		spamRatio = Math.log((double) numSpamDocuments
				/ (double) (totalTrainingDocs));

		Map<String, String> vocabulary = buildVocabulary(hamTokenCntMap,
				spamTokenCntMap);
		int vocabularySize = vocabulary.size();

		Iterator<Entry<String, String>> vocabItr = vocabulary.entrySet()
				.iterator();

		while (vocabItr.hasNext()) {
			Map.Entry pairs = (Map.Entry) vocabItr.next();
			String token = (String) pairs.getKey();
			Integer tokenFrequencyHam = hamTokenCntMap.get(token);
			int tokenFrequencyHamValue = 1;
			if (tokenFrequencyHam != null) {
				tokenFrequencyHamValue += tokenFrequencyHam.intValue();
			}

			double condProb = (double) tokenFrequencyHamValue
					/ (double) (numTokensHam + vocabularySize);
			hamCondProbability.put(token, new Double(Math.log(condProb)));

			Integer tokenFrequencySpam = spamTokenCntMap.get(token);
			int tokenFrequencySpamValue = 1;
			if (tokenFrequencySpam != null) {
				tokenFrequencySpamValue += tokenFrequencySpam.intValue();
			}

			condProb = (double) tokenFrequencySpamValue
					/ (double) (numTokensSpam + vocabularySize);
			spamCondProbability.put(token, new Double(Math.log(condProb)));
		}

	}

	/**
	 * Build the vocab set
	 * @param hamTokenCntMap
	 * @param spamTokenCntMap
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static Map<String, String> buildVocabulary(
			Map<String, Integer> hamTokenCntMap,
			Map<String, Integer> spamTokenCntMap) {
		Map<String, String> vocabulary = new HashMap<String, String>();
		Iterator<Entry<String, Integer>> hamWordCntMapItr = hamTokenCntMap
				.entrySet().iterator();
		while (hamWordCntMapItr.hasNext()) {
			Map.Entry pairs = (Map.Entry) hamWordCntMapItr.next();
			vocabulary.put((String) pairs.getKey(), null);
		}

		Iterator<Entry<String, Integer>> spamWordCntMapItr = spamTokenCntMap
				.entrySet().iterator();
		while (spamWordCntMapItr.hasNext()) {
			Map.Entry pairs = (Map.Entry) spamWordCntMapItr.next();
			vocabulary.put((String) pairs.getKey(), null);
		}
		return vocabulary;
	}

	/**
	 * Method to test the algorithm
	 * @param rootDir
	 * @return
	 * @throws IOException 
	 */
	private static void test(String rootDir) throws IOException {
		System.out.println("Testing Phase..");		
		String hamTestingSetFolderPath = rootDir + File.separator + "test"
				+ File.separator + HAM;
		String spamTestingSetFolderPath = rootDir + File.separator + "test"
				+ File.separator + SPAM;
		int numDocuments = 0;
		int numSpamDocuments = 0;
		int numHamDocuments = 0;
		int numCorrectClassification = 0;
		int numSpamCorrectClassification = 0;
		int numHamCorrectClassification = 0;
		File docFolder = new File(hamTestingSetFolderPath);
		File[] docs = docFolder.listFiles();

		numDocuments += docs.length;
		numHamDocuments= docs.length;;
		for (File doc : docs) {
			if (isDocSpam(doc) == false) {
				numCorrectClassification++;
				numHamCorrectClassification++;
			}
		}

		docFolder = new File(spamTestingSetFolderPath);
		docs = docFolder.listFiles();
		numDocuments += docs.length;
		numSpamDocuments=  docs.length;
		for (File doc : docs) {
			if (isDocSpam(doc) == true) {
				numCorrectClassification++;
				numSpamCorrectClassification++;
			}
		}

		System.out.println("Accuracy :" + 100
				* (double) numCorrectClassification / (double) numDocuments+"%");
		System.out.println("Precision :"+ 100*(double) numSpamCorrectClassification 
				/ (double) (numSpamCorrectClassification+(numHamDocuments-numHamCorrectClassification))+"%");
		System.out.println("Recall :"+ 100* (double) numSpamCorrectClassification 
				/ (double) (numSpamCorrectClassification+(numSpamDocuments-numSpamCorrectClassification))+"%");
	}

	/**
	 * Checks if a document is a spam or not
	 * @param document
	 * @return
	 * @throws IOException 
	 */
	private static boolean isDocSpam(File document) throws IOException {

		double hamScore = hamRatio;
		double spamScore = spamRatio;

		ArrayList<String> tokens = FileUtils.retrieveTokensFromFile(document
				.getAbsolutePath(), removeStopWords,doSmoothing);

		for (String token : tokens) {
			Double condProbHam = hamCondProbability.get(token);
			if (condProbHam != null) {
				hamScore += condProbHam;
			}

			Double condProbSpam = spamCondProbability.get(token);
			if (condProbSpam != null) {
				spamScore += condProbSpam;
			}
		}
		if (hamScore >= spamScore) {

			return false;
		} else {

			return true;
		}
	}

}
