package org.stoevesand.brain.rest;

import java.security.Principal;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.jboss.logging.Logger;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.model.UserLessonList;
import org.stoevesand.brain.persistence.BrainDB;

@Path("/20")
public class BrainResource20 {

	private static Logger log = Logger.getLogger(BrainResource20.class);

	boolean develop = false;

	public BrainResource20() {
		develop = System.getProperty("notonto.develop") != null;
		System.out.println("DEV-MODE ist " + develop);
	}

	/**
	 * Command: /<user>/<pass>/answeritem/<answer>/<useritemid>
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 * @param prot
	 */
	@GET
	@Path("/answeritem/{answer}/{useritemid}")
	@Produces("application/json")
	public Message answer(@PathParam("answer") String answer, @PathParam("useritemid") long uiid, @Context HttpServletRequest request) {

		Header header = new Header(0, "OK", "");
		Message msg = new Message(header);

		try {
			// String answer = nextToken(buffer);
			boolean known = answer.equals("t");

			// String sid = nextToken(buffer);
			// long uiid = Long.parseLong(sid);
			BrainSystem bs = BrainSystem.getBrainSystemNoFaces();
			BrainDB db = bs.getBrainDB();
			UserItem userItem = db.getUserItem(uiid);
			User user = getUser(request);

			if (user == null) {
				// ret.append("{ error: \"Wrong user or password\"}");
				header.error_code = Header.ERROR_USER_UNKNOWN;
				header.error_msg = "WRONG USER";
				header.error_text = "Wrong user or password";
			} else if (userItem == null) {
				// System.out.println("not answerable.");
				// ret.append("{ error: \"Item not available or cannot be answered\"}");
				header.error_code = Header.ERROR_UNKNOWN;
				header.error_msg = "WRONG ITEM";
				header.error_text = "Item not available or cannot be answered";
			} else {
				if (known) {
					// System.out.println("right. (" + sid + ")");
					userItem.knowAnswer(user);
				} else {
					// System.out.println("wrong. (" + sid + ")");
					userItem.failAnswer(user);
				}
				// ret.append("{ msg: \"OK\"}");
			}
			log.debug("MSG: " + msg.toString());
			return msg;
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			header.error_code = Header.ERROR_UNKNOWN;
			header.error_msg = "EXC";
			header.error_text = e.getMessage();
			return msg;
		}
	}

	/**
	 * Command: /<user>/<pass>/nextitem/<userlessonid>
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 */
	@GET
	@Path("/nextitem/{userlessonid}")
	@Produces("application/json")
	public Message nextitem(@PathParam("userlessonid") long ulid, @Context HttpServletRequest request) {
		Header header = new Header(Header.ERROR_OK, "OK", "");
		Message msg = new Message(header);

		try {
			User user = getUser(request);
			UserItem userItem = BrainSystem.getBrainSystemNoFaces().getBrainDB().getNextUserItem(ulid);
			int opencurrent = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserLessonAvailable(ulid);
			int opentotal = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserAvailable(user);
			// System.out.println("UI: " + userItem);

			if (userItem == null) {
				header.error_code = Header.ERROR_NOITEMAVAILABLE;
				header.error_msg = "NO ITEM";
				header.error_text = "";
				header.openCurrent = 0;
				header.openTotal = opentotal;

				// ret.append("{ \"msg\": 1 ");
				// ret.append(", \"opencurrent\" : 0, \"opentotal\" : " + opentotal +
				// "}");
			} else {
				header.error_code = Header.ERROR_OK;
				header.error_msg = "OK";
				header.error_text = "";
				header.openCurrent = opencurrent;
				header.openTotal = opentotal;

				msg.addContent(userItem);
				// ret.append("{ \"useritem\" : ");
				// ret.append(userItem.toJSON(true));
				// ret.append(", \"opencurrent\" : " + opencurrent +
				// ", \"opentotal\" : " + opentotal);
				// ret.append("}");
			}
			return msg;
		} catch (Exception e) {
			header.error_code = Header.ERROR_UNKNOWN;
			header.error_msg = "EXC";
			header.error_text = e.getMessage();
			return msg;
		}
	}

	/**
	 * Command: /<user>/<pass>/lessons
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 */
	@GET
	@Path("/lessons")
	@Produces("application/json")
	public Message lessons(@Context HttpServletRequest request) {
		Header header = new Header(Header.ERROR_OK, "OK", "");
		Message msg = new Message(header);

		try {
			User user = getUser(request);

			if (user != null) {
				Vector<UserLesson> userLessons = user.getLessons();
				msg.addContent(new UserLessonList(userLessons));
			} else {
				msg.header.error_code = Header.ERROR_USER_UNKNOWN;
				msg.header.error_msg = "UNKNOWN";
			}

			return msg;
		} catch (Exception e) {
			header.error_code = Header.ERROR_UNKNOWN;
			header.error_msg = "EXC";
			header.error_text = e.getMessage();
			return msg;
		}
	}

	/**
	 * Command: /<user>/<pass>/lessons
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 */
	@GET
	@Path("/ping")
	@Produces("application/json")
	public Message ping(@Context HttpServletRequest request) {
		Header header = new Header(Header.ERROR_OK, "OK", "");
		Message msg = new Message(header);

		try {
			User user = getUser(request);
			if (user == null) {
				msg.header.error_code = Header.ERROR_USER_UNKNOWN;
				msg.header.error_msg = "USER UNKNOWN";
			}

		} catch (Exception e) {
			header.error_code = Header.ERROR_UNKNOWN;
			header.error_msg = "EXC";
			header.error_text = e.getMessage();
		}
		return msg;
	}

	private User getUser(HttpServletRequest request) throws DBException {
		String username = "";
		String password = "";
		Principal p = request.getUserPrincipal();
		if (p != null) {
			System.out.println("Get user/pass from principal");
			username = p.getName();
			password = "";
		} else {
			System.out.println("Get user/pass from request");
			username = request.getHeader("username");
			password = request.getHeader("password");
		}

		if (develop) {
			username = "jan";
			password = "jan";
		}

		User user = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUser(username, password);

		if ((user!=null) && develop)
			return user;
		
		if ((user == null) || (!user.getPassword().equals(password))) {
			user = null;
		}
		return user;
	}

}
