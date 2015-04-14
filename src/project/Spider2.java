package project;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Spider2 {

	static final int nbPagesToIndex = 1000;

	URL url;
	Database2 db;

	/* structures for the spider */
	LinkedHashSet<String> urlList; // queuing list - links not indexed
	HashSet<String> urlIndexed; // links indexed

	/* structures for the child-links */
	Hashtable<Integer, URL[]> childURLs = new Hashtable<Integer, URL[]>();

	/* structures for the indexer */
	// invertedIndexes: word -> (pageID, termfrequency)
	// body structures
	HashMap<Integer, List<Posting>> invertedIndexBody;
	// title structures
	HashMap<Integer, List<Posting>> invertedIndexTitle;

	/**
	 * Spider constructor
	 * 
	 * @param urlString
	 */
	public Spider2(String urlString) {
		try {
			this.url = new URL(urlString);
			urlIndexed = new HashSet<String>();
			urlList = new LinkedHashSet<String>();
			urlList.add(url.toString());
			invertedIndexTitle = new HashMap<Integer, List<Posting>>();
			invertedIndexBody = new HashMap<Integer, List<Posting>>();
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	/**
	 * Indexing the pages by using the Extractor class to receive the words and
	 * URL's from a given start URL. The method loops until the nbPagesToIndex
	 * is reached.
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void indexPages() throws ParserException, IOException,
			ParseException {
		db = new Database2();

		int i = 0; // number of pages indexed
		while (i < nbPagesToIndex && !urlList.isEmpty()) {
			// String urlStr = urlList.removeFirst();
			Iterator<String> itr = urlList.iterator();

			String urlStr = itr.next();
			itr.remove();
			URL obj = new URL(urlStr);
			// String urlStr = obj.toString();
			Document doc;
			try {
				doc = Jsoup
						.connect(urlStr)
						.userAgent(
								"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36")
						.get();
				System.out.println("Doc obtained");
			} catch (Exception e) {
				continue;
			}

			// try {
			// Connection connection = Jsoup
			// .connect(urlStr)
			// .userAgent(
			// "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36")
			// .timeout(100*100);
			// Connection.Response response = connection.execute();
			//
			// int statusCode = response.statusCode();
			// if(statusCode == 200) {
			// doc = connection.get();
			// }
			// else {
			// System.out.println("Status code = " + response.statusCode());
			// System.out.println("Status msg  = " + response.statusMessage());
			// continue;
			// }
			// } catch (Exception e) {
			// continue;
			// }

			URLConnection conn = obj.openConnection();
			String lastModified = conn.getHeaderField("Last-Modified");
			if (lastModified == null) {
				long seconds = conn.getDate();
				Date date = new Date(seconds);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"EEE, dd MMM yyyy HH:mm:ss zzz");
				lastModified = sdf.format(date);
			}

			if (isToBeIndexed(urlStr, lastModified, doc)) {
				// String content = extractStrings(urlStr, false);
				String content = doc.body().text();
				// String title = content.split("\n")[0];
				String title = doc.title();
				content = title + "\n" + content;
				String pageSize = conn.getHeaderField("Content-Length");

				pageSize = pageSize == null ? String.valueOf(content.length())
						: pageSize;

				int pageId = db.addUrl(urlStr); // get the page ID
				extractLinks(urlStr, pageId);
				urlIndexed.add(urlStr);

				// word frequencies and positions
				Hashtable<Integer, Posting> postingsForTitle = new Hashtable<Integer, Posting>();
				Hashtable<Integer, Posting> postingsForBody = new Hashtable<Integer, Posting>();

				// max term frequencies
				int maxTermFreqTitle = createPostings(title, pageId,
						postingsForTitle);
				int maxTermFreqBody = createPostings(content, pageId,
						postingsForBody);

				// save structures
				buildInvertedIndexTitle(pageId, postingsForTitle);
				buildInvertedIndexBody(pageId, postingsForBody);
				db.addPage(pageId, title, urlStr, lastModified, pageSize,
						postingsForTitle, maxTermFreqTitle, postingsForBody,
						maxTermFreqBody);
				i++;
			}
		}
		System.out.println();
		saveParentChildStructure();
		computeTermWeightsForInvertedIndex(invertedIndexBody, nbPagesToIndex,
				true);
		computeTermWeightsForInvertedIndex(invertedIndexTitle, nbPagesToIndex,
				false);
		db.saveInvertedIndex(invertedIndexTitle, false);

		db.saveInvertedIndex(invertedIndexBody, true);
		db.finalize();
	}

	private void computeTermWeightsForInvertedIndex(
			HashMap<Integer, List<Posting>> invertedIndex, int totalNumPages,
			boolean isBody) {

		for (Map.Entry<Integer, List<Posting>> entry : invertedIndex.entrySet()) {
			int tf, maxTf;
			double idf, df, N = totalNumPages;
			List<Posting> postingList = entry.getValue();
			df = postingList.size();
			for (Posting p : postingList) {
				int pageId = p.getPageId();
				ArrayList<Integer> positionList = p.getPositionList();
				tf = positionList.size();
				maxTf = db.getMaxTermFrequency(pageId, isBody);
				idf = Math.log(N / df) / Math.log(2);

				double termWeight = tf * idf / maxTf;
				// System.out.println("termWeight: " + termWeight);
				// if (termWeight == 0.00) {
				// System.out.println("df: " + df + ", tf: " + tf
				// + ", maxTf: " + maxTf + ", idf: " + idf);
				// System.out.println("N: " + N);
				// System.out.println("df: " + df);
				// System.out.println("N/df = " + N / df);
				// System.out.println("Math.log(totalNumPages/df) = "
				// + Math.log(N / df));
				// System.out.println("Math.log(2) = " + Math.log(2));
				// }
				p.setTermWeight(termWeight);
			}
		}
	}

	/**
	 * Return true if a URL has not been indexed or has been modified since it
	 * was indexed
	 * 
	 * @param url
	 * @param lastModified
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private boolean isToBeIndexed(String url, String lastModified, Document doc)
			throws IOException {
		try {
			if (doc == null || doc.title().equals("") || doc.body().text() == null) {
				return false;
			} 
		} catch (NullPointerException e) {
			return false;
		}
		if (!urlIndexed.contains(url)) {
			return true;
		}
		String oldLastModified = db.getUrlLastModified(url);
		if (oldLastModified != null && lastModified != null) {
			SimpleDateFormat format = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss zzz");
			Date oldDate;
			try {
				oldDate = format.parse(oldLastModified);
				Calendar cal = Calendar.getInstance(); // creates calendar
				cal.setTime(oldDate); // sets calendar time/date
				cal.add(Calendar.HOUR_OF_DAY, 3); // adds one hour
				oldDate = cal.getTime(); // returns new date object, one hour in
											// the
				// future
				Date date = format.parse(lastModified);
				if (date.after(oldDate)) {
					System.out.println("date is after old date");
					return true;
				}
			} catch (ParseException e) {
				System.out.println("date parser exception");
				return false;
			}
		}
		System.out.println("date is NOT after old date");
		return false;
	}

	/**
	 * Receives a string of words from a page, and updates the word frequency
	 * parameter.
	 * 
	 * @param content
	 *            String of the words on page
	 * @param pageId
	 *            Is not used?
	 * @param postings
	 *            the word frequency Hashtable
	 * @return The maximum term frequency on the page.
	 * @throws IOException
	 */
	private int createPostings(String content, int pageId,
			Hashtable<Integer, Posting> postings) throws IOException {
		StopStem stopStem = new StopStem("stopwords.txt");
		String[] words = content.split("\\s+");
		// position of first word start at 1
		int position = 1;
		// we consider that there is a least one word in the page
		int maxTermFrequency = 1;
		// remove stop words and get stem of the current word
		for (String w : words) {
			if (w.length() > 0) {
				if (!stopStem.isStopWord(w)) {
					String stemmedWord = stopStem.stem(w);
					// System.out.println("stemmed word: " + stemmedWord);
					int wordId = db.addWord(stemmedWord);
					Posting posting = postings.get(wordId);
					// compute the new term frequency of the word

					if (posting != null) {
						posting.appendPositionList(position);
						int freq = posting.getPositionList().size();

						// compute max term frequency
						if (freq > maxTermFrequency)
							maxTermFrequency = freq;
					} else {
						// first elem: pageID
						// second elem: position of the first occurrence of the
						// current word
						posting = new Posting(pageId, position);
						postings.put(wordId, posting);
					}
				}
			}
			position++;
		}
		return maxTermFrequency;
	}

	/**
	 * Build partial inverted index (body) and document frequency structures to
	 * save in the Database
	 * 
	 * @param pageId
	 * @param postingsForTitle
	 * @throws IOException
	 */
	private void buildInvertedIndexTitle(int pageId,
			Hashtable<Integer, Posting> postingsForTitle) throws IOException {
		buildStructures(pageId, postingsForTitle, invertedIndexTitle);
	}

	/**
	 * Build partial inverted index (body) and document frequency structures to
	 * save in the Database
	 * 
	 * @param pageId
	 * @param postingsForBody
	 * @throws IOException
	 */
	private void buildInvertedIndexBody(int pageId,
			Hashtable<Integer, Posting> postingsForBody) throws IOException {
		buildStructures(pageId, postingsForBody, invertedIndexBody);
	}

	/**
	 * Update the invertedIndex and document frequency
	 * 
	 * @param pageId
	 * @param postings
	 *            (word frequencies of the pageId)
	 * @param invertedIndex
	 * @throws IOException
	 */
	private void buildStructures(int pageId,
			Hashtable<Integer, Posting> postings,
			HashMap<Integer, List<Posting>> invertedIndex) throws IOException {
		Enumeration<Integer> enumKey = postings.keys();
		while (enumKey.hasMoreElements()) {
			int wordId = enumKey.nextElement();
			Posting currPosting = postings.get(wordId);
			buildInvertedIndex(pageId, wordId, currPosting, invertedIndex);
		}
	}

	/**
	 * Update the related inverted index
	 * 
	 * @param pageID
	 * @param wordId
	 * @param tf
	 * @param invertedIndex
	 *            (body or title)
	 */
	private void buildInvertedIndex(int pageID, int wordId,
			Posting postingToBeAdded,
			HashMap<Integer, List<Posting>> invertedIndex) {
		List<Posting> postingList;
		if (!invertedIndex.containsKey(wordId)) {
			postingList = new ArrayList<Posting>();
			invertedIndex.put(wordId, postingList);
		} else {
			postingList = invertedIndex.get(wordId);
		}
		postingList.add(postingToBeAdded);
	}

	/**
	 * Extracts links in a URL, update child-links structures
	 * 
	 * @param resource
	 * @return
	 * @throws ParserException
	 * @throws IOException
	 */
	private void extractLinks(String resource, int parentId)
			throws ParserException, IOException {
		LinkBean lb = new LinkBean();
		lb.setURL(resource);
		URL[] URL_array = lb.getLinks();
		if (URL_array!=null && URL_array.length != 0) {
			childURLs.put(parentId, URL_array);
			for (int i = 0; i < URL_array.length; i++) {
				String url = URL_array[i].toString();
				if (url.contains("nus") && !url.contains("zh-hans") && !url.contains("zh-hant") &&  !urlIndexed.contains(url)) {
					urlList.add(url);
				}
			}
		}
	}

	/**
	 * When indexing is over, save all links which have a subset of children
	 * indexed
	 * 
	 * @throws IOException
	 */
	private void saveParentChildStructure() throws IOException {
		Set<Integer> keys = childURLs.keySet();
		for (int pageId : keys) {
			URL[] links = childURLs.get(pageId);
			HashSet<Integer> children = new HashSet<Integer>();
			for (int i = 0; i < links.length; i++) {
				int childId = db.getUrlId(links[i].toString());
				if (childId != -1) {
					children.add(childId);
					int parentId = db.getParentId(childId);
					if (parentId == -1)
						db.saveParentId(childId, pageId);
				}
			}
			db.saveChildrenIds(pageId, children);
		}
	}
}
