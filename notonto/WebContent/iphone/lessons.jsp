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

	String password = request.getParameter("password");
	String pass = StringUtils.getParameter(request.getQueryString(),"pass");

	//System.out.println("user: " + username);
	//System.out.println("password: " + password);
	//System.out.println("pass    : " + pass);

	if (pass != null) {
		password = cu.decrypt(pass);
	} else {
		pass = cu.encrypt(password);
	}

	User user = null;
	try {
		if ((username != null) && (username.length() > 0))
			user = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUser(username, password);
	} catch (Exception e) {
	}
%>

<%@page import="org.stoevesand.util.CryptUtils"%>
<%@page import="org.stoevesand.util.StringUtils"%>

<html>
<h:head>
<title>Notonto</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0/jquery.mobile-1.0.min.css" />
<script type="text/javascript" src="http://code.jquery.com/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="http://code.jquery.com/mobile/1.0/jquery.mobile-1.0.min.js"></script>
</h:head>
<h:body>
<div data-role="page">
<div data-role="header">
<h1>Lessons</h1>
<a href="/iphone/index.jsp" data-icon="delete">Logout</a>
</div>
<div data-role="content">

	<div class="content-primary">
		<ul data-role="listview">

	<%
				try {
				Vector<UserLesson> userLessons = user.getLessons();

				Iterator<UserLesson> it = userLessons.iterator();
				while (it.hasNext()) {
			UserLesson iul = it.next();
	%>
	<li><a href="/iphone/item.jsp?username=<%=username%>&pass=<%=pass%>&ulid=<%=iul.getId()%>"><%=iul.getLesson().getDescription()%></a></li>
	<%
			}
			} catch (Exception e) {

			}
	%>		

		</ul>
	</div><!--/content-primary -->	
</div>
</div>
</h:body>
</html>
