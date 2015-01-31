## Search Engine 

A search engine written in Java with clean and responsive AJAX-laden interface. 
## Mechanisms and implementation

A web crawler is implemented to fetch pages recursively from a given web site and an indexer is implemented to extract keywords from a page and inserts them into an inverted file. The indexer removes all stop words from the file. It then transforms words into stems using the Porter's algorithm. 

It inserts the stems into the two inverted files: all stems extracted from the page body, together with all statistical information needed to support the vector space model, are inserted into one inverted file and all stems extracted from the page title are inserted into another inverted file. 

The indexes support phrase search such as “hong kong” in page titles and page bodies.

The JDBM library from http://jdbm.sourceforge.net/ is suggested to be used to create and manipulate the file structures for storing the inverted file and other file structures needed.

A retrieval function (or called the search engine) that compares a list of query terms against the inverted file and returns the top documents, up to a maximum of 50, to the user in a ranked order according to the vector space model.  As noted about, phrase must be supported, e.g., “hong kong” universities.

Term weighting formula is based on tfxidf/max(tf) and document similarity is based on cosine similarity measure.

Implemented a mechanism to favor matches in title. For example, a match in the title would significantly boost the rank of a page.

A web interface that accepts a user query in a text box, submits the query to the search engine, and displays the returned results to the user

## Run the search engine
To run this program please follow these steps.

1. Import project in Eclipse
   File -> Import -> Existing Projects into Workspace -> "Choose your project" -> Finish

2. Make sure the JAR's are included in the Build Path
   "Right-click on project" -> Properties -> Java Build Path -> "Add the JAR's from /lib"

3. Edit the line 66 in Database2.java to the complete path of your workspace.

4. Run the Main class. From here you will be prompted with options.
   The first thing you want to do is to start the spider by pressing "i".
   Then you can create a "spider_results.txt" files to see the database by pressing "p".

5. To use the Search Engine press "s".

## Deploy the Search Engine web interface.
Follow these steps to deploy the Search Engine web interface on a tomcat server.

1. Create a new folder in your webapps folder called se.

2. Place the libraries from /lib in "../webapps/se/WEB-INF/lib/"

3. Export the Search Engine as a jar file.
   File -> Export -> JAR file -> 
   "select all files and export the JAR file to ../webapps/se/WEB-INF/lib/ "

4. Place the files from the web folder in "../webapps/se/"

5. Restart the tomcat server and access the Search Engine webinterface on 
   http://localhost:8080/se/search.html
