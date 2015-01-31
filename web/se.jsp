  <%@page contentType="text/html; charset=UTF-8"%>
  <%@page import="org.json.simple.JSONObject"%> 
  <%@page import="java.util.*" %>
  <%@page import="java.io.*" %>
  <%@page import="project.*" %>


  <%
    String inputStr = request.getParameter("query");

    JSONObject query_response = new JSONObject();
    JSONObject results = new JSONObject();
    SearchEngine se = new SearchEngine();
    
    SortedSet<Page> seresponse = se.search(inputStr);

    if (seresponse.isEmpty()) {
      query_response.put("success", false);
      query_response.put("query", inputStr);
    } else {
      int i = 0;
      for (Page p : seresponse) {
        JSONObject result = new JSONObject();
        JSONObject keywords = new JSONObject();
        JSONObject children_links = new JSONObject();
        
        // Loop through the keywords
        int h = 0;
        for (String k : p.getMostFreqKeywords()) {
          keywords.put("keyword"+h, k);
          h++;
        }

        // Loop throgh the children links
        int j = 0;
        for (String l : p.getChildrenLinks()) {
          children_links.put("children"+j, l);
          j++;
        }

        result.put("score", p.getScore());
        result.put("title", p.getTitle());
        result.put("url", p.getUrl());
        result.put("last_modified", p.getLastModified());
        result.put("size", p.getSize());
        result.put("keywords", keywords);
        result.put("parent_links", p.getParentLink());
        result.put("children_links", children_links);

        results.put(i, result);
        i++;
      }

      query_response.put("res", results);
      query_response.put("success", true);
      query_response.put("query", inputStr);
    }
    out.print(query_response);
    out.flush();
  %>