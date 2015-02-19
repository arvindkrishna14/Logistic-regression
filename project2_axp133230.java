import java.io.IOException;

/**
 * 
 * @author Arvind Krishna Parthasarathy
 *
 */
public class project2_axp133230 {
	public static void main(String[] args) throws IOException{
		LRClass lr = new LRClass();
		LRClass lr2 = new LRClass();
		LRClass lr3 = new LRClass();
		System.out.println("Naive Bayes Classification : Without removal of stop words \n");
		NaiveBayesClassifier.runNaiveBayesClassifier(false, false);
		System.out.println("\n");
		System.out.println("Naive Bayes Classification : After removal of stop words \n");
		NaiveBayesClassifier.runNaiveBayesClassifier(true, false);
		System.out.println("\n\n");
	
		System.out.println("Logistic Regression : Without removal of stop words \n");
		lr.logisticRegression(false, false);
		System.out.println("\n");
		System.out.println("Logistic Regression : After removal of stop words \n");
		lr2.logisticRegression(true, false);
		System.out.println("\n\n");
		
		
		
		System.out.println("Smoothing using Feature Selection (Extra Credit)\n");
		System.out.println("\n\nNaive Bayes without removal of stop words");
		NaiveBayesClassifier.runNaiveBayesClassifier(false, true);
	
		System.out.println("\n\nLogistic Regression without removal of stop words");
		lr3.logisticRegression(false,true);
		
	}
}
