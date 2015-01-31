package project;

import java.net.URL;
import java.util.LinkedList;
import java.util.Vector;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

/**
 * Extractor class to extract Strings and URL's from a given URL. 
 *
 */
public class Extractor {
	private String resource;

	public Extractor() {
		
	}
	
	/**
	 * Extract the text from a page.
	 * 
	 * @return The textual contents of the page.
	 * @param links
	 *            if <code>true</code> include hyperlinks in output.
	 * @exception ParserException
	 *                If a parse error occurs.
	 */
	public String extractStrings(boolean links) throws ParserException {
		StringBean sb;

		sb = new StringBean();
		sb.setLinks(links);
		sb.setURL(resource);

		return (sb.getStrings());
	}
	
	public Vector<String> extractLinks(LinkedList<URL> urlList)
			throws ParserException {
		Vector<String> v_link = new Vector<String>();
		LinkBean lb = new LinkBean();
		lb.setURL(resource);
		URL[] URL_array = lb.getLinks();

		for (int i = 0; i < URL_array.length; i++) {
			v_link.add(URL_array[i].toString());
			urlList.add(URL_array[i]);
		}
		return v_link;
	}
	
    public void setUrl(String url) {
    	resource = url;
    }

}
