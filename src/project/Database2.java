package project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class Database2 {
	int currPageId;
	int currWordId;
	RecordManager recman;

	public static final String invertedIndexBody_DB = "invertedIndexBody";
	public static final String invertedIndexTitle_DB = "invertedIndexTitle";
	public static final String URLtoPageID_DB = "URLtoPageID";
	public static final String pageIDtoURL_DB = "pageIDtoURL";
	public static final String wordToWordID_DB = "wordToWordID";
	public static final String wordIDtoWord_DB = "wordIDToWord";
	public static final String parentLink_DB = "parentLink";
	public static final String childrenLinks_DB = "childrenLinks";
	// contains the information of each page and their forward indexes (body and
	// title)
	public static final String pageTable_DB = "pageTable";

	// indexes of the page elements in the pageTable vector
	private static final int indexTitle = 0;
	private static final int indexURL = 1;
	private static final int indexLastModified = 2;
	private static final int indexSize = 3;
	private static final int indexMaxTermFrequencyTitle = 4;
	private static final int indexWordFreqTitle = 5;
	private static final int indexMaxTermFrequency = 6;
	private static final int indexWordFreq = 7;

	HTree invertedIndexBody;
	HTree invertedIndexTitle;
	HTree URLtoPageID;
	HTree pageIDtoURL;
	HTree wordToWordID;
	HTree wordIDtoWord;
	HTree parentLink;
	HTree childrenLinks;
	HTree pageTable;

	/**
	 * Database Constructor. Loading all the tables in the database.
	 * @throws IOException 
	 */
	public Database2() throws IOException {
		currPageId = 0;
		currWordId = 0;
			String path = "/home/hvingelby/workspace/comp4321-search-engine/";
			recman = RecordManagerFactory.createRecordManager(path+"projectRM");

			System.out.println();
			pageTable = loadDatabase(pageTable_DB);
			invertedIndexBody = loadDatabase(invertedIndexBody_DB);
			invertedIndexTitle = loadDatabase(invertedIndexTitle_DB);
			URLtoPageID = loadDatabase(URLtoPageID_DB);
			pageIDtoURL = loadDatabase(pageIDtoURL_DB);
			wordToWordID = loadDatabase(wordToWordID_DB);
			wordIDtoWord = loadDatabase(wordIDtoWord_DB);
			parentLink = loadDatabase(parentLink_DB);
			childrenLinks = loadDatabase(childrenLinks_DB);
			System.out.println();
	}

	/**
	 * Receives the recid for the specified database name and returns an HTree
	 * instance of it.
	 * 
	 * @param dbName
	 * @return HTree
	 * @throws IOException
	 */
	private HTree loadDatabase(String dbName) throws IOException {
		long recid = recman.getNamedObject(dbName);
		if (recid != 0) {
			System.out.println(dbName + " loaded");
			return HTree.load(recman, recid);
		} else {
			HTree htree = HTree.createInstance(recman);
			recman.setNamedObject(dbName, htree.getRecid());
			return htree;
		}
	}

	/**
	 * Adds an URL to the database and assigns a pageId for the URL.
	 * 
	 * @param url
	 * @return pageId for the added URL
	 * @throws IOException
	 */
	public int addUrl(String url) throws IOException {
		Object pageId = URLtoPageID.get(url);
		if (pageId == null) {
			currPageId++;
			URLtoPageID.put(url, currPageId); // url -> pageID
			pageIDtoURL.put(currPageId, url); // pageID -> url
			return currPageId;
		} else {
			return (int) pageId;
		}
	}

	/**
		* Returns the id of an indexed URL, -1 if not indexed
		* 
		* @param url
		* @return
		* @throws IOException
		*/
	public int getUrlId(String url) throws IOException {
		try {
			if (URLtoPageID.get(url) != null) {
				return (int) URLtoPageID.get(url);
			}
			return -1;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
		* Returns the parent id of a page, -1 if not assigned
		* 
		* @param url
		* @return
		* @throws IOException
		*/
	public int getParentId(int id) throws IOException {
		if (parentLink.get(id) != null) {
			return (int) parentLink.get(id);
		}
		return -1;
	}
	
	public String getPageUrl(int pageId) {
		try {
			Vector<Object> pageVector = (Vector<Object>) pageTable.get(pageId);
			return pageVector == null ? "" : (String) pageVector.get(indexURL);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getUrlLastModified(String url) throws IOException {
		int id = (int) URLtoPageID.get(url);
		Vector<Object> page = (Vector<Object>) pageTable.get(id);
		return (String) page.get(indexLastModified);
	}

	/**
	 * Adds a word to the database and assigns a wordId for the word.
	 * 
	 * @param word
	 * @return
	 * @throws IOException
	 */
	public int addWord(String word) throws IOException {
		Object wordId = wordToWordID.get(word);
		if (wordId == null) {
			currWordId++;
			wordToWordID.put(word, currWordId); // url -> pageID
			wordIDtoWord.put(currWordId, word); // pageID -> url
			return currWordId;
		} else {
			return (int) wordId;
		}
	}

	public int getWordId(String word) {
		try {
			Object wordId = wordToWordID.get(word);
			return wordToWordID.get(word) == null ? -1 : (int) wordId;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

	}
	
	public String getWord(int wordId) {
		try {
			String word = (String) wordIDtoWord.get(wordId);
			return word == null ? "" : word;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Adds a page to the pageTable database. A page is represented by the
	 * parameters, which is stored in the database as a vector.
	 * 
	 * @param pageId
	 * @param title
	 * @param url
	 * @param lastModified
	 * @param size
	 * @param postingsForBody
	 * @param childrenLinks
	 * @param maxTermFrequency
	 * @throws IOException
	 */
	public void addPage(int pageId, String title, String url,
			String lastModified, String size,
			Hashtable<Integer, Posting> postingsForTitle,
			int maxTermFrequencyTitle,
			Hashtable<Integer, Posting> postingsForBody, int maxTermFrequency)
			throws IOException {
		Vector<Object> val = new Vector<Object>();
		val.add(title);
		val.add(url);
		val.add(lastModified);
		val.add(size);
		val.add(maxTermFrequencyTitle);
		val.add(postingsForTitle);
		val.add(maxTermFrequency);
		val.add(postingsForBody);
		pageTable.put(pageId, val);
		System.out.println("Page " + pageId + " added: " + title);
	}
	
	// TODO Maybe create a class called Page for easier retrieval by getter and setter methods instead of hardcoded index via vector list
	public int getMaxTermFrequency(int pageId, boolean isBody) {
		try {
			Vector<Object> page = (Vector<Object>) pageTable.get(pageId);
			return isBody ? (int) page.get(indexMaxTermFrequency) : (int) page.get(indexMaxTermFrequencyTitle);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public void augmentPageInfo(Page p) {
		try {
			int pageId = p.getId();
			Vector<Object> pageVector = (Vector<Object>) pageTable.get(pageId);
			String title = (String) pageVector.get(indexTitle);
			String url = (String) pageVector.get(indexURL);
			String lastModified = (String) pageVector.get(indexLastModified);
			String sizeStr = (String) pageVector.get(indexSize);
			int size = (Integer) Integer.parseInt(sizeStr);
			Hashtable<Integer, Posting> postings = (Hashtable<Integer, Posting>) pageVector.get(indexWordFreq);
			ArrayList<String> mostFreqKeywords = sortWordsAccordingToFreq(postings);
			int parentPageId = getParentId(pageId);
			String parentLink = getPageUrl(parentPageId);
			HashSet<Integer> childrenPageIds = getChildrenIds(pageId);
			ArrayList<String> childrenLinks = new ArrayList<String>();
			if (childrenPageIds != null) {
				for(Integer pId : childrenPageIds) {
					childrenLinks.add(getPageUrl(pId));
				}
			}
			p.title = title;
			p.url = url;
			p.lastModified = lastModified;
			p.size = size;
			p.mostFreqKeywords = mostFreqKeywords;
			p.parentLink = parentLink;
			p.childrenLinks = childrenLinks;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<String> sortWordsAccordingToFreq(Hashtable<Integer, Posting> postings) {
		List<Map.Entry> list = new ArrayList<Map.Entry>(postings.entrySet());
		Collections.sort(list, new Comparator<Map.Entry>() {
			public int compare(Map.Entry e1, Map.Entry e2) {
				Posting p1 = (Posting) e1.getValue();
				Posting p2 = (Posting) e2.getValue();
				Integer freq1 = p1.getPositionList().size();
				Integer freq2 = p2.getPositionList().size();
				return freq2.compareTo(freq1);
			}
		});
	
		ArrayList<String> mostFreqKeywords = new ArrayList<String>();
		for(int i=0; i < list.size(); i++) {
			Integer wordId = (Integer) list.get(i).getKey();
			String word = getWord(wordId);
			if (word.trim().length() == 0) 
				continue;
			Posting p = (Posting) list.get(i).getValue();
			String freq = String.valueOf(p.getPositionList().size());
			mostFreqKeywords.add(word + " " + freq);
			if (mostFreqKeywords.size() == 5) 
				break;
		}
		return mostFreqKeywords;
	}

	/**
	 * @param body
	 *            specifies whether the body of the pages should be returned or
	 *            not.
	 * @return HTree of the inverted index table
	 */
	private HTree getInvertedIndexTable(boolean body) {
		if (body)
			return invertedIndexBody;
		else
			return invertedIndexTitle;
	}

	public List<Posting> getPostingList(boolean isBody, int wordId) {
		try {
			HTree invertedIndex = getInvertedIndexTable(isBody);
			List<Posting> postingList = (List<Posting>) invertedIndex.get(wordId);
			return postingList;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Print the related concept of the structure (body or title)
	 * 
	 * @param body
	 * @return
	 */
	private String printRelatedStructure(boolean body) {
		if (body)
			return "(body)";
		else
			return "(title)";
	}

	/**
	 * Runs through the keys of the given invertedIndex, and saves the
	 * invertedIndex in the wordTable.
	 * 
	 * @param invertedIndex
	 * @param body
	 * @throws IOException
	 */
	public void saveInvertedIndex(
			HashMap<Integer, List<Posting>> invertedIndex, boolean body)
			throws IOException {
		HTree invertedIndexDb = getInvertedIndexTable(body);
		for (Entry<Integer, List<Posting>> entry : invertedIndex.entrySet()) {
			Integer wordId = entry.getKey();
			Object postingList = entry.getValue();
			invertedIndexDb.put(wordId, postingList);
		}
		System.out.println("Inverted index saved "
				+ printRelatedStructure(body));
	}

	/**
	 * Save parent id of a link
	 * 
	 * @param link
	 * @param parent
	 * @throws IOException
	 */
	public void saveParentId(int link, int parent) throws IOException {
		parentLink.put(link, parent);
	}


	/**
	 * Save children ids of link
	 * 
	 * @param parent
	 * @param children
	 * @throws IOException
	 */
	public void saveChildrenIds(int parent, HashSet<Integer> children)
			throws IOException {
		childrenLinks.put(parent, children);
	}
	
	public HashSet<Integer> getChildrenIds(int pageId) {
		try {
			return (HashSet<Integer>) childrenLinks.get(pageId);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void finalize() throws IOException {
		recman.commit();
		recman.close();
	}
	
	

	/* DISPLAY */
	/**
	 * Prints all the entries in the pageTable and stores them in a
	 * StringBuilder. Using helper methods to print and build StringBuilder:
	 * <ul>
	 * <li>printTitle()</li>
	 * <li>printURL()</li>
	 * <li>printModifDateAndSize()</li>
	 * <li>printWordFreq()</li>
	 * <li>printChildLinks()
	 * <li>printLineBreak()</li>
	 * </ul>
	 * 
	 * @return StringBuilder of all the entries in pageTable
	 * @throws IOException
	 */
	public StringBuilder getPages() throws IOException {
		FastIterator itr = pageTable.values();
		Vector<Vector<Object>> pages = new Vector<Vector<Object>>();
		int i = 1; // TODO: why ?????
		Vector<Object> p = (Vector<Object>) pageTable.get(i++);
		StringBuilder sb = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");
		while (p != null) {
			printTitle(p, i-1, sb, lineSeparator);
			printUrl(p, sb, lineSeparator);
			printModifDateAndSize(p, sb, lineSeparator);
			printWordFreq((Hashtable<Integer, Posting>) p.get(indexWordFreq),
					sb, i -1, lineSeparator);
			printChildLinks(i - 1, sb, lineSeparator);
			printLineBreak(sb, lineSeparator);
			p = (Vector<Object>) pageTable.get(i++);
		}
		return sb;
	}

	private void printLineBreak(StringBuilder sb, String lineSeparator) {
		System.out.println();
		sb.append(lineSeparator);
		System.out
				.println("----------------------------------------------------------------------------------------");
		sb.append("----------------------------------------------------------------------------------------");
		System.out.println();
		sb.append(lineSeparator);
	}

	private void printTitle(Vector<Object> p, int pageId, StringBuilder sb,
			String lineSeparator) {
		System.out.println("Page " + pageId + ": " + p.get(indexTitle));
		sb.append("Page " + pageId + ": " + p.get(indexTitle));
		sb.append(lineSeparator);
	}

	private void printUrl(Vector<Object> p, StringBuilder sb,
			String lineSeparator) {
		System.out.println(p.get(indexURL));
		sb.append(p.get(indexURL));
		sb.append(lineSeparator);
	}

	private void printModifDateAndSize(Vector<Object> p, StringBuilder sb,
			String lineSeparator) {
		System.out.println(p.get(indexLastModified) + ", " + p.get(indexSize));
		sb.append(p.get(indexLastModified) + ", " + p.get(indexSize));
		sb.append(lineSeparator);
	}

	private void printWordFreq(Hashtable<Integer, Posting> hashtable,
			StringBuilder sb, int pageId, String lineSeparator) throws IOException {
		Iterator<Entry<Integer, Posting>> itr = hashtable.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<Integer, Posting> entry = itr.next();
			int wordId = entry.getKey();
			String word = ((String) wordIDtoWord.get(wordId)).replaceAll(
					"(\\r|\\n|\\t)", "");
			Posting p = entry.getValue();
			System.out.print(word + " " + p.getPositionList().size() + ";");
			sb.append(word + " " + p.getPositionList().size() + ";");
			System.out.print(" ");
			sb.append(" ");
		}
		System.out.println();
		sb.append(lineSeparator);
	}

	private void printChildLinks(int linkId, StringBuilder sb,
			String lineSeparator) throws IOException {
		HashSet<Integer> childLinks = (HashSet<Integer>) childrenLinks
				.get(linkId);
		if (childLinks != null) { // TODO: correct this
			for (int link : childLinks) {
				String url = (String) pageIDtoURL.get(link);
				System.out.println(url);
				sb.append(url);
				sb.append(lineSeparator);
			}
		}
	}
}
