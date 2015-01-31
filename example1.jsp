<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="project.*" %>


<HTML>
<!-- I am HTML Comment -->
<%-- I am JSP Comment --%>
<BODY>

<% java.util.Date date = new java.util.Date(); %>
<% Database2 db = new Database2();%>
<% SearchEngine se = new SearchEngine(); %>

Hello!  The time is now <%= date %> <br>

<%
    out.println( "<BR>Your machine's address is " );
    out.println( request.getRemoteHost());

    out.println("<BR> <BR>");
    //out.println(db.isCorrectLoaded());

    out.println(db.getUrlId("http://www.ust.hk/"));

	//SortedSet<Page> results = se.search("prize");
    //sb.setLinks(false);
	//sb.setURL("http://www.ust.hk/");
	//out.println( sb.getStrings());
%>




</BODY>
</HTML>