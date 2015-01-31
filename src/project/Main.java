package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.SortedSet;

import javax.management.Query;

import org.htmlparser.util.ParserException;

public class Main {
	public static void main(String args[]) throws ParserException, IOException {
		System.out.println("########################");
		System.out.println("## Welcome to Search! ##");
		System.out.println("########################");
		promptUser();
	}
	
	/**
	 * Method to display the programs options and prompt the user for input.
	 */
	public static void promptUser(){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("You have the following options:");
		System.out.println();
		System.out.println("    p    --> Print the indexed pages to 'spider_result.txt'.");
		System.out.println("    i    --> Start the spider and begin indexing.");
		System.out.println("    s    --> Start a search and get results from the search engine.");
		System.out.println("    e    --> Exit");
		
		String input="";
		try {
			input = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (input.equals("p")) {
			printIndexedFiles();
			promptUser();
		} else if (input.equals("i")) {
			startSpider();
			promptUser();
		} else if (input.equals("s")) {
			// Here we can add a functionality to do a basic search?
			System.out.println("Type your query!");
			String query;
			try {
				query = br.readLine();
				performSearch(query);
			} catch (IOException e) {
				e.printStackTrace();
			}
			promptUser();
		} else if (input.equals("e")) {
			System.out.println("Bye!");
		} else {
			System.out.println("Wrong input!");
			System.out.println();
			promptUser();
		}
	}
	
	/**
	 * Print the indexed files from the DB to the formatted 'spider_result.txt'.
	 * Also prints in console
	 */
	public static void printIndexedFiles(){
			
		
		try {
			Database2 db = new Database2();
			StringBuilder sb;	
			sb = db.getPages();
			db.finalize();
			
			System.out.println("Writing to spider_result.txt...");
			String content = sb.toString();
 
			File file = new File("spider_result.txt");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public static void startSpider(){
		Spider2 spider = new Spider2("http://www.cse.ust.hk/~ericzhao/COMP4321/TestPages/testpage.htm");
		System.out.println("A new spider instance has been created.");
		System.out.println("Now crawling...");
		
		try {
			spider.indexPages();
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static void performSearch(String query){
		SearchEngine se = new SearchEngine();
		SortedSet<Page> results = se.search(query);
		System.out.println();
		System.out.println();
		try {
			for (Page p : results) {
				System.out.println("Page " + p.getId() + ": " + p.getTitle() + ", " + "score: " + p.getScore());
				System.out.println(p.getUrl());
				System.out.println(p.getLastModified() + ", " + p.getSize());
				List<String> keywords = p.getMostFreqKeywords();
				if (keywords != null) {
					for (String k : keywords) {
						System.out.print(k + "; ");
					}
				}
				System.out.println();
				System.out.println("------------- Parent link -------------");
				System.out.println(p.getParentLink());
				System.out.println("------------- Children links -------------");
				List<String> childrenLinks = p.getChildrenLinks();
				if (childrenLinks != null) {
					for (String l : childrenLinks) {
						System.out.println(l);
					}
				}
				System.out.println("----------------------------------------------------------------");
				System.out.println();
				System.out.println();
			}
		} catch (Exception e) {
			
		}
	}
}
