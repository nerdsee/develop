<%@ page session="false" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page import="java.util.*"%>
<%@ page import="org.stoevesand.brain.*"%>
<%@ page import="org.stoevesand.brain.auth.*"%>
<%@ page import="org.stoevesand.brain.model.*"%>

<%
	CryptUtils cu = CryptUtils.getInstance();

	String username = request.getParameter("username");
	String pass = StringUtils.getParameter(request.getQueryString(),"pass");

	//System.out.println("JSP: requ:" + request.getQueryString());
	//System.out.println("JSP: user:" + username);
	//System.out.println("JSP: pass:" + pass);

	String password = cu.decrypt(pass);
	//System.out.println("JSP: password:" + password);

	User user = null;
	try {
		if ((username != null) && (username.length() > 0))
			user = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUser(username, password);
	} catch (Exception e) {
	}
	if (user == null) {
%>
<jsp:forward page="/iphone/login.jsp?msg=e" />
<%
} else {
	
	String sulid = request.getParameter("ulid");
	String lessonName="";
	
	if (sulid!=null) {
	long ulid = Long.parseLong(sulid);

	try {
		UserLesson userLesson = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserLesson(user,ulid);
		lessonName=userLesson.getLesson().getDescription();
		int pos=lessonName.indexOf("(");
		if (pos>=0) {
			lessonName=lessonName.substring(0,pos)+"<br>"+lessonName.substring(pos);
		}
	} catch (Exception e) {
		
	}
	
	}

%>
<%@page import="org.stoevesand.util.CryptUtils"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="org.stoevesand.util.StringUtils"%>
<html>
<h:head>
<title>Notonto</title>
<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;" />
<!-- style type="text/css" media="screen">@import "/iphone/iui/iui.css";</style>  -->
<style type="text/css" media="screen">
@import "/css/iphone.css";
</style>
<script type="application/x-javascript" src="/js/iphone.js"></script>
</h:head>
<h:body>
<div class="toolbar">
<div id="pageTitle" style="margin: 0px; padding: 0px; color: white; font-size: 12px; width: 200px;"><%=lessonName%></div>
<a id="backButton" class="button" href="#"></a> <a id="toolbarButton" class="buttonBlue" href="/iphone/lessons.jsp?username=<%=username%>&pass=<%=pass%>" target="_self">Lessons</a></div>
<div id="card" class="card"><ui:include src="/iphone/itemel.jsp" /></div>
</h:body>
</html>
<%
}
%>
