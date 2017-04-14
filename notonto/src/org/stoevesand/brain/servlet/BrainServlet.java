package org.stoevesand.brain.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.model.UserLesson;

public class BrainServlet extends HttpServlet {

	private static Logger log = LogManager.getLogger(BrainServlet.class);

	private static final long serialVersionUID = 1L;

	private static final int METHOD_POST = 0;
	private static final int METHOD_GET = 1;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("post: " + request.getPathInfo());
		doCommand(request, response, METHOD_POST);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("get: " + request.getPathInfo());
		doCommand(request, response, METHOD_GET);
	}

	public void doCommand(HttpServletRequest request, HttpServletResponse response, int method) throws ServletException, IOException {

		response.setCharacterEncoding("utf-8");
		// ret.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		try {
			String scl = request.getPathInfo();
			//log.debug(scl.substring(1));
			StringBuffer buffer = new StringBuffer(scl.substring(1));

			String sprot = nextToken(buffer).toLowerCase();
			int prot = "json".equals(sprot) ? 0 : 1;

			//System.out.println("S: " + sprot + " - " + prot);

			String username = nextToken(buffer);
			String pass = nextToken(buffer);

			//log.debug("name : " + username);
			User user = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUser(username, pass);

			if ((user != null) && (user.getPassword().equals(pass))) {
				String action = nextToken(buffer);
				if (action.equals("lessons")) {
					sendLessons(response, user, buffer, prot);
				} else if (action.equals("nextitem")) {
					sendItem(response, user, buffer, prot);
				} else if (action.equals("answeritem")) {
					doAnswerItem(request, response, user, buffer, prot);
				} else {
					response.getOutputStream().print("<error>unknown command</error>");
				}
			} else {
				response.getOutputStream().print("<error>unknown user</error>");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String nextToken(StringBuffer buffer) {
		String ret = "";
		try {
			int pos = buffer.indexOf("/");
			if (pos >= 0) {
				ret = buffer.substring(0, pos);
				buffer.delete(0, pos + 1);
				//log.debug("SBnt: " + buffer.toString());
			} else {
				ret = buffer.toString();
				buffer = new StringBuffer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Command: /<user>/<pass>/answer/<useritemid>
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 * @param prot
	 */
	private void doAnswerItem(HttpServletRequest request, HttpServletResponse response, User user, StringBuffer buffer, int prot) {
		StringBuffer ret = new StringBuffer();

		try {
			String answer = nextToken(buffer);
			boolean known = answer.equals("t");

			String sid = nextToken(buffer);
			long id = Long.parseLong(sid);

			if (prot == 1) { // XML
				ret.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			}

			UserItem userItem = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserItem(id);
			if (userItem == null) {
				//System.out.println("not answerable.");
				ret.append("{ error: \"Item not available or cannot be answered\"}");
			} else {

				if (known) {
					//System.out.println("right. (" + sid + ")");
					userItem.knowAnswer();
				} else {
					//System.out.println("wrong. (" + sid + ")");
					userItem.failAnswer();
				}
				ret.append("{ msg: \"OK\"}");

			}
			log.debug("RET: " +ret.toString());
			response.getWriter().print(ret.toString());
		} catch (Exception e) {
			error(response, e);
		}
	}

	public String checkAnswerText(UserItem userItem, String answerText) throws DBException {
		String ret = "wrong";

		log.debug("api_checkAnswerText:" + answerText);

		if (userItem.checkAnswerText(answerText)) {
			ret = "correct";
		} else {
			ret = BrainSystem.findLongestCommonString(userItem.getItem(), answerText);
		}
		return ret;
	}

	/**
	 * Command: /<user>/<pass>/nextitem/<userlessonid>
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 */
	private void sendItem(HttpServletResponse response, User user, StringBuffer buffer, int prot) {
		StringBuffer ret = new StringBuffer();

//		if (user!=null) {
//			log.debug("EXT: " + user.getName());
//		}
		
		try {
			if (prot == 1) { // XML
				ret.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			}
			String sid = nextToken(buffer);
			long ulid = Long.parseLong(sid);
			UserItem userItem = BrainSystem.getBrainSystemNoFaces().getBrainDB().getNextUserItem(ulid);
			int opencurrent = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserLessonAvailable(ulid);
			int opentotal = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserAvailable(user);
			//System.out.println("UI: " + userItem);

			if (userItem == null) {
				ret.append("{ \"msg\": 1 ");
				ret.append(", \"opencurrent\" : 0, \"opentotal\" : "+opentotal+"}");
			} else {
				if (prot == 0) { // JSON
					ret.append("{ \"useritem\" : ");
					ret.append(userItem.toJSON(true));
					ret.append(", \"opencurrent\" : "+opencurrent+", \"opentotal\" : "+opentotal);
					ret.append("}");
				} else {
					ret.append(userItem.toXML(true));
				}
			}
			response.getWriter().print(ret.toString());
		} catch (Exception e) {
			error(response, e);
		}
	}

	/**
	 * Command: /<user>/<pass>/lessons
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 */
	private void sendLessons(HttpServletResponse response, User user, StringBuffer buffer, int prot) {
		StringBuffer ret = new StringBuffer();

		try {
			Vector<UserLesson> userLessons = user.getLessons();

			if (prot == 0) { // JSON
				ret.append("{ \"userlessons\" : [ ");
			} else {
				ret.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				ret.append("<UserLessons>");
			}

			Iterator<UserLesson> it = userLessons.iterator();
			while (it.hasNext()) {
				if (prot == 0) { // JSON
					ret.append(it.next().toJSON());
					if (it.hasNext())
						ret.append(", ");
				} else {
					ret.append(it.next().toXML());
				}
			}

			if (prot == 0) { // JSON
				ret.append(" ] }");
			} else {
				ret.append("</UserLessons>");
			}

			response.getWriter().print(ret.toString());
		} catch (Exception e) {
			error(response, e);
		}
	}

	private void error(HttpServletResponse response, Exception e) {
		try {
			log.debug("API_ERROR: " + e.toString());
			e.printStackTrace();
			response.getWriter().print("ERROR:");
			response.getWriter().print(e.toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
