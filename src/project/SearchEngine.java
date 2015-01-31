package project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {
	static Hashtable<Integer, Double> scoresBody = new Hashtable<Integer, Double>();
	static Hashtable<Integer, Double> magnitudesBody = new Hashtable<Integer, Double>();
	static Hashtable<Integer, Double> scoresTitle = new Hashtable<Integer, Double>();
	static Hashtable<Integer, Double> magnitudesTitle = new Hashtable<Integer, Double>();

	Database2 db;
	StopStem stopStem;

	/**
	 * Constructor
	 */
	public SearchEngine() {
		try {
			db = new Database2();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stopStem = new StopStem("stopwords.txt");
	}

	/**
	 * The search method used by either the Main class or the web interface to
	 * get the results
	 * 
	 * @param rawQuery
	 * @return
	 */
	public SortedSet<Page> search(String rawQuery) {
		Set<Integer> freeTextResults = new TreeSet<Integer>();
		Set<Integer> phraseResults = new TreeSet<Integer>();
		SortedSet<Page> results = new TreeSet<Page>();
		
		scoresTitle.clear();
		scoresBody.clear();
		magnitudesBody.clear();
		magnitudesTitle.clear();

		ArrayList<String> freeTextQuery = new ArrayList<String>();
		ArrayList<String> phraseQuery = new ArrayList<String>();
		parseQuery(rawQuery, freeTextQuery, phraseQuery);
		boolean isBody = true;
		if (freeTextQuery.size() != 0) {
			retrieveFreeTextResults(freeTextQuery, freeTextResults);
		}
		if (phraseQuery.size() != 0) {
			retrievePhraseResults(phraseQuery, phraseResults);
		}

		freeTextResults.addAll(phraseResults);
		int queryLength = freeTextQuery.size() + phraseQuery.size();
		normalizeSimilarityScores(freeTextResults, results, queryLength);
		retrieveMoreInforForResults(results);

		return results;
	}

	private void retrieveMoreInforForResults(SortedSet<Page> results) {
		for (Page p : results) {
			db.augmentPageInfo(p);
		}
	}

	private void normalizeSimilarityScores(Set<Integer> freeTextResults,
			SortedSet<Page> results, int queryLength) {
		int numPages = 0;
		double bodyScoreWeight = 0.2, titleScoreWeight = 200;
		for (Integer pageId : freeTextResults) {
			if (numPages == 50)
				return;

			Double magnitudeBody = magnitudesBody.get(pageId);
			Double scoreBody = scoresBody.get(pageId);

			Double normalizedScoreBody = scoreBody
					/ (Math.sqrt(queryLength) * (magnitudeBody));
			Double normalizedScoreTitle;

			Double magnitudeTitle = magnitudesTitle.get(pageId);
			Double scoreTitle = scoresTitle.get(pageId);
			if (scoreTitle == null || magnitudeTitle == null) {
				normalizedScoreTitle = 0.00;
			} else {
				normalizedScoreTitle = scoreTitle
						/ (Math.sqrt(queryLength) * (magnitudeTitle));
			}

			double score = Double.isNaN(bodyScoreWeight * normalizedScoreBody) ? titleScoreWeight
					* normalizedScoreTitle
					: titleScoreWeight * normalizedScoreTitle + bodyScoreWeight
							* normalizedScoreBody;
			results.add(new Page(pageId, score));
			numPages++;
		}
	}

	private void retrievePhraseResults(ArrayList<String> phraseQuery,
			Set<Integer> phraseResults) {
		List<Set<Integer>> partialResultsList = new ArrayList<Set<Integer>>();
		for (String term : phraseQuery) {
			Set<Integer> partialResults = new TreeSet<Integer>();
			retrievePagesAndComputeRanking(term, partialResults);
			partialResultsList.add(partialResults);
		}

		// intersection of partial results
		// rawPhraseResults now contain docs that have all terms in the phrase

		Set<Integer> rawPhraseResults = partialResultsList.get(0);
		for (int i = 1; i < partialResultsList.size(); i++) {
			boolean isRetained = rawPhraseResults.retainAll(partialResultsList
					.get(i));
		}

		// algorithm to check if selected docs have queries in correct order
		// step 1: get posting lists of query terms for each doc
		// step 2: extract out positions of each term and put them in a separate
		// list of lists
		// step 3: we don't touch the first list, but substract i-1 from every
		// element in the ith list
		// step 4: we take the intersection of the lists. if it's not empty, we
		// conclude the doc is matching doc

		boolean isBody = true;
		for (Integer pageId : rawPhraseResults) {
			List<ArrayList<Integer>> positionLists = new ArrayList<ArrayList<Integer>>();
			// step 1 and 2
			for (String term : phraseQuery) {
				if (!stopStem.isStopWord(term)) {
					int wordId = db.getWordId(stopStem.stem(term));
					if (wordId != -1) {
						List<Posting> postingList = db.getPostingList(isBody,
								wordId);
						for (Posting p : postingList) {
							if (pageId == p.getPageId()) {
								positionLists.add(p.getPositionList());
							}
						}
					} else {
						System.out.println("No pages found.");
					}
				}
			}
			if (positionLists.size() == 0)
				return;
			// step 3
			for (int i = 0; i < positionLists.size(); i++) {
				ArrayList<Integer> posList = positionLists.get(i);
				for (int k = 0; k < posList.size(); k++) {
					posList.set(k, posList.get(k) - i);
				}
			}

			// step 4; step 3 & 4 can possibly be combined into one loop
			ArrayList<Integer> intersectedPosList = positionLists.get(0);
			for (int i = 1; i < positionLists.size(); i++) {
				intersectedPosList.retainAll(positionLists.get(i));
			}
			// page with all phrase query terms in correct order will be added
			// to phrase results
			if (!intersectedPosList.isEmpty()) {
				phraseResults.add(pageId);
			}
		}

	}

	private void retrievePagesAndComputeRanking(String term,
			Set<Integer> partialResults) {
		boolean isBody = true;
		if (!stopStem.isStopWord(term)) {
			int wordId = db.getWordId(stopStem.stem(term));
			if (wordId != -1) {
				computeScores(partialResults, isBody, wordId);
				computeScores(partialResults, !isBody, wordId);
			} else {
				System.out.println("No pages found.");
			}
		}
	}

	private void computeScores(Set<Integer> partialResults, boolean isBody,
			int wordId) {
		Hashtable<Integer, Double> scores = isBody ? scoresBody : scoresTitle;
		Hashtable<Integer, Double> magnitudes = isBody ? magnitudesBody
				: magnitudesTitle;
		List<Posting> postingList = db.getPostingList(isBody, wordId);
		if (postingList == null)
			return;
		for (Posting p : postingList) {
			Integer pageId = p.getPageId();
			partialResults.add(pageId);
			double termWeight = p.getTermWeight();
			if (scores.get(pageId) == null) {
				scores.put(pageId, 0.00);
			}
			scores.put(pageId, scores.get(pageId) + termWeight);
			if (magnitudes.get(pageId) == null) {
				magnitudes.put(pageId, 0.00);
			}
			magnitudes.put(pageId,
					magnitudes.get(pageId) + Math.pow(termWeight, 2));
		}
	}

	private void retrieveFreeTextResults(ArrayList<String> freeTextQuery,
			Set<Integer> freeTextResults) {
		for (String term : freeTextQuery) {
			retrievePagesAndComputeRanking(term, freeTextResults);
		}
	}

	private void parseQuery(String rawQuery, ArrayList<String> freeTextQuery,
			ArrayList<String> phraseQuery) {
		rawQuery = rawQuery.toLowerCase();
		rawQuery = extractPhrases(rawQuery, phraseQuery);
		extractFreeText(rawQuery, freeTextQuery);

	}

	private void extractFreeText(String rawQuery,
			ArrayList<String> freeTextQuery) {
		String[] terms = rawQuery.split("\\s+");
		for (String t : terms) {
			if (t.trim().length() > 0) {
				freeTextQuery.add(t);
				System.out.println("Free text query: " + t);
			}
		}
	}

	private static String extractPhrases(String rawQuery,
			ArrayList<String> phraseQuery) {
		// regex for double quotes
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(rawQuery);
		while (m.find()) {
			String quotedString = m.group();
			rawQuery = rawQuery.replace(quotedString, "");
			quotedString = quotedString.replace("\"", "");
			String[] terms = quotedString.split("\\s+");
			for (String t : terms) {
				if (!t.equals("\"") && (t.trim().length() > 0)) {
					System.out.println("Phrase query: " + t);
					phraseQuery.add(t);
				}
			}
		}
		return rawQuery;
	}
}
