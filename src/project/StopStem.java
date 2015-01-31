package project;

import java.io.*;

public class StopStem {
    private Porter porter;
    private java.util.HashSet<String> stopWords;

    /** 
     * Constructor
     * @param stopWordFile
     */
	public StopStem(String stopWordFile) {
		super();
		porter = new Porter();
		stopWords = new java.util.HashSet<String>();

		try {
			FileReader file = new FileReader(stopWordFile);
			BufferedReader br = new BufferedReader(file);

			String line = br.readLine();
			while (line != null) {
				stopWords.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param str	Word to be stemmed.
	 * @return 		Stemmed word based on Porters algoritm.
	 */
	public String stem(String str) {
		return porter.stripAffixes(str);
	}
	
	/**
	 * 
	 * @param str 	Word to check.
	 * @return		Whether the word is stopword based on the given txt file with stopwords.
	 */
    public boolean isStopWord(String str) {
    	return stopWords.contains(str);
    }
}
