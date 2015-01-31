package project;

import java.io.Serializable;
import java.util.ArrayList;

public class Posting implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1536762923407702977L;
	int pageId;
	ArrayList<Integer> positionList;
	double termWeight;
	
	public Posting(int pageId, int pos) {
		this.pageId = pageId;
		this.positionList = new ArrayList<Integer>();
		positionList.add(pos);
	}
	
	public void setTermWeight(double termWeight) {
		this.termWeight = termWeight;
	}
	
	public double getTermWeight() {
		return termWeight;
	}
	
	public int getPageId() {
		return pageId;
	}
	
	public ArrayList<Integer> getPositionList() {
		return positionList;
	}
	
	
	public void appendPositionList(int pos) {
		positionList.add(pos);
	}
}
