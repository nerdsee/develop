<%@ page session="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.stoevesand.brain.*"%>
<%@ page import="org.stoevesand.brain.auth.*"%>
<%@ page import="org.stoevesand.brain.model.*"%>

<%
	CryptUtils cu = CryptUtils.getInstance();

	String username = request.getParameter("username");
	String pass = StringUtils.getParameter(request.getQueryString(), "pass");

	String password = cu.decrypt(pass);
	//System.out.println("JSP: password:" + password);

	//System.out.println("JSP: requ:" + request.getQueryString());
	//System.out.println("JSP: user:" + username);
	//System.out.println("JSP: pass:" + password);

	User user = null;
	try {
		if ((username != null) && (username.length() > 0))
			user = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUser(username, password);
	} catch (Exception e) {
	}

	if (user == null) {
%>
<%@page import="org.stoevesand.util.StringUtils"%>
<%@page import="org.stoevesand.util.CryptUtils"%>
<jsp:forward page="/iphone/login.jsp?msg=e" />
<%
	} else {
%>
<%
	StringBuffer ret = new StringBuffer();
		String sulid = "";
		String suiid = "";

		//long now=System.currentTimeMillis();
		//String now="";

		boolean load = request.getParameter("load") != null;

		try {

			String action = request.getParameter("action");
			if (action != null) {
				sulid = request.getParameter("ulid");
				long ulid = Long.parseLong(sulid);
				suiid = request.getParameter("uiid");
				long uiid = Long.parseLong(suiid);
				UserItem userItem = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserItem(user, uiid);

				if (userItem != null) {
					if (action.equals("y")) {
						userItem.knowAnswer(user);
					} else {
						userItem.failAnswer(user);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			sulid = request.getParameter("ulid");
			long ulid = Long.parseLong(sulid);
			UserItem userItem = BrainSystem.getBrainSystemNoFaces().getBrainDB().getNextUserItem(user, ulid);
			if (userItem != null) {
				UserLesson userLesson = BrainSystem.getBrainSystem().getBrainDB().getUserLesson(user, ulid);
				int type = userLesson.getLesson().getLessonType();
				boolean showPinyin = userLesson.getConfig().getShowPinyin();

				out.write("<div id=\"question\" class=\"front face\">");

				if (type == 0) {
					out.write("<div class=\"q1\">");
					out.write(userItem.getItem().getFormattedText());
					out.write("</div>");
				} else {

					out.write("<div class=\"q2 center\">" + userItem.getItem().getFormattedText() + "</div>");

					if (showPinyin) {
%>
<div class="pinyin center"><%=userItem.getItem().getComment()%></div>
<%
	}
				}
%>
<table cellpadding="10" width="100%">
	<tr>
		<td><a class="whiteButton" onClick="answer()">Answer &gt;</a></td>
	</tr>
</table>
</div>
<div id="answer" class="back face">
<%
	if (!showPinyin) {
%>
<div class="pinyin center"><%=userItem.getItem().getComment()%></div>
<%
	}

				Vector<Answer> answers = userItem.getItem().getAnswers();
				boolean createList = answers.size() > 1;
				if (createList)
					out.write("<ul class=\"a1\">");
				for (Answer answer : answers) {
					String style = "answer";
					if (answer.getType() == 2)
						style = "fontpin";
					if (answer.getType() == 1) {
						//System.out.println("L: " + answer.getText().length());
						style = answer.getText().length() > 2 ? "fontsym2" : "fontsym1";
					}
					if (createList) {
						out.write("<li class=\"" + style + "\">" + answer.getText() + "</li>");
					} else {
						out.write("<div class=\"q1 " + style + "\">" + answer.getText() + "</div>");
					} // if
				} // for
				if (createList)
					out.write("</ul>");
%>
<div class="msg font1">Did you know the answer?</div>
<table width="100%" cellpadding="10">
	<tr>
		<td width="50%"><a class="whiteButton" onClick="nextQuestion(<%=ulid%>,<%=userItem.getId()%>,'y','<%=username%>','<%=pass%>')">Yes</a></td>
		<td width="50%"><a class="grayButton" onClick="nextQuestion(<%=ulid%>,<%=userItem.getId()%>,'n','<%=username%>','<%=pass%>')">No</a></td>
	</tr>
</table>
<table cellpadding="10" width="100%">
	<tr>
		<td><a class="grayButton" onClick="question()">&lt; Question</a></td>
	</tr>
</table>
</div>
<%
	} else {
				UserLesson userLesson = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserLesson(user, ulid);
				String timeToNext = userLesson.getNextUserItemTimeDiff();
%>
<div id="question" class="front face">
<div class="font1 msg">Currently no questions available.</div>
<div class="font1 msg">Next Question is available in:<br> <%=timeToNext%></div>
<table cellpadding="10" width="100%">
	<tr>
		<td><a class="grayButton" onClick="location.reload()">Reload</a></td>
	</tr>
</table>
</div>
<div id="answer" class="back face">
</div>
<%
	}
		} catch (Exception e) {
			e.printStackTrace();
		}
%>
<%
	}
%>