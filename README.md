## Search Engine 

A search engine written in Java, implemented with web crawler and indexer. Supports title-matching mechanism and phrase queries (e.g. "hong kong").

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
