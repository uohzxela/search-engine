package project;

import java.util.ArrayList;

public class Page implements Comparable<Page> {
	int id;
	double score;
	String title, url, lastModified, parentLink;
	int size;
	ArrayList<String> mostFreqKeywords, childrenLinks;

	public Page(int id, double score) {
		this.id = id;
		this.score = score;
	}
	
	// deprecated
//	public Page(double score, String title, String url, String lastModified, int size,
//			ArrayList<String> mostFreqKeywords, ArrayList<String> childrenLinks,
//			String parentLink) {
//		this.score = score;
//		this.title = title;
//		this.url = url;
//		this.lastModified = lastModified;
//		this.size = size;
//		this.mostFreqKeywords = mostFreqKeywords;
//		this.childrenLinks = childrenLinks;
//		this.parentLink = parentLink;
//	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getLastModified() {
		return lastModified;
	}

	public int getSize() {
		return size;
	}

	public ArrayList<String> getMostFreqKeywords() {
		return mostFreqKeywords;
	}
	
	public ArrayList<String> getChildrenLinks() {
		return childrenLinks;
	}
	
	public String getParentLink() {
		return parentLink;
	}

	@Override
	public int compareTo(Page page2) {
		double score2 = page2.getScore();
		if (score == score2 && id == page2.getId())
			return 0;
		else if (score > score2)
			return -1;
		else
			return 1;
	}

	public double getScore() {
		return score;
	}

	public int getId() {
		return id;
	}

}
