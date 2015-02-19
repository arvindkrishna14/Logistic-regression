
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 
 * @author Arvind Krishna Parthasarathy
 *
 */
public class NaiveBayesHelper {

	/**
	 * Process documents in a given folder: 1.Considers each file and determines
	 * the count of each token 2.Calculates the total number of tokens
	 * 
	 * @return
	 */
	public static ArrayList<String> stopWords = new ArrayList<String>();
	public static Map<Object, Object> processDocuments(
			String trainingSetFolderPath,boolean removeStopWords, boolean doSmoothing) {

		// variable to track the total number of words
		int numTokens = 0; 

		// Map to store the processed results.
		Map<Object, Object> result = new HashMap<Object, Object>(); 

		// Map to keep track of words and its frequency

		Map<String, Integer> tokenCntMap = new HashMap<String, Integer>(); 

		File trainingSetFolder = new File(trainingSetFolderPath);
		File[] documents = trainingSetFolder.listFiles();
		for (File doc : documents) {
			try {
				
				BufferedReader bufferedReader = new BufferedReader(
						new FileReader(doc));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					String[] tokens = line.split("\\s+");
					numTokens += tokens.length;
					if(removeStopWords == false){
						for (String token : tokens) {
							token = token.replaceAll("[^a-zA-Z]", "");
							token = token.toLowerCase();
							if(!token.isEmpty()){
								if (tokenCntMap.containsKey(token)) {
									tokenCntMap.put(token, tokenCntMap.get(token)
											.intValue() + 1);
								} else {
									tokenCntMap.put(token, 1);
								}
							}
						}
					}
					else{
						for (String token : tokens) {
							token = token.replaceAll("[^a-zA-Z]", "");
							token = token.toLowerCase();
							if(!token.isEmpty() && !stopWords.contains(token)){
								if (tokenCntMap.containsKey(token)) {
									tokenCntMap.put(token, tokenCntMap.get(token)
											.intValue() + 1);
								} else {
									tokenCntMap.put(token, 1);
								}
							}
						}
					}
				}
				bufferedReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		result.put("NumTokens", numTokens);
		result.put("NumDocuments", documents.length);
		result.put("TokenCntMap", tokenCntMap);
		return result;

	}
	public static void putStopWords(String rootDir) throws IOException{
		rootDir = "stopwords.txt";
		
		BufferedReader bufferedReader = null;
		bufferedReader = new BufferedReader(new FileReader(new File(rootDir).getAbsoluteFile()));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			stopWords.add(line);
		}
		bufferedReader.close();
	}



}
