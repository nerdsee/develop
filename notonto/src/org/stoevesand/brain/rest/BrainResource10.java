package org.stoevesand.brain.rest;

import java.util.Iterator;
import java.util.Vector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.logging.Logger;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.persistence.BrainDB;

@Path("/10")
public class BrainResource10 {

	private static Logger log = Logger.getLogger(BrainResource10.class);

	/**
	 * Command: /<user>/<pass>/answeritem/<answer>/<useritemid>
	 * 
	 * @param response
	 * @param user
	 * @param buffer
	 * @param prot
	 */
	@GET
	@Path("/{user}/{pass}/answeritem/{answer}/{useritemid}")
	@Produces("application/json")
	public String answer(@PathParam("user") String username, @PathParam("pass") String password, @PathParam("answer") String answer, @PathParam("useritemid") long uiid) {
		StringBuffer ret = new StringBuffer();

		try {
			// String answer = nextToken(buffer);
			boolean known = answer.equals("t");

			// String sid = nextToken(buffer);
			// long uiid = Long.parseLong(sid);
			BrainSystem bs = BrainSystem.getBrainSystemNoFaces();
			BrainDB db = bs.getBrainDB();
			UserItem userItem = db.getUserItem(uiid);
			User user = getUser(username, password);

			if (user == null) {
				ret.append("{ error: \"Wrong user or password\"}");
			} else if (userItem == null) {
				// System.out.println("not answerable.");
				ret.append("{ error: \"Item not available or cannot be answered\"}");
			} else {
				if (known) {
					// System.out.println("right. (" + sid + ")");
					userItem.knowAnswer(user);
				} else {
					// System.out.println("wrong. (" + sid + ")");
					userItem.failAnswer(user);
				}
				ret.append("{ msg: \"OK\"}");

			}
			log.debug("RET: " + ret.toString());
			return ret.toString();
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			return "{ msg: \"ERROR\"}";
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
	@Path("/{user}/{pass}/nextitem/{userlessonid}")
	@Produces("application/json")
	public String nextitem(@PathParam("user") String username, @PathParam("pass") String password, @PathParam("userlessonid") long ulid) {
		StringBuffer ret = new StringBuffer();

		try {
			User user = getUser(username, password);
			UserItem userItem = BrainSystem.getBrainSystemNoFaces().getBrainDB().getNextUserItem(ulid);
			int opencurrent = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserLessonAvailable(ulid);
			int opentotal = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserAvailable(user);
			// System.out.println("UI: " + userItem);

			if (userItem == null) {
				ret.append("{ \"msg\": 1 ");
				ret.append(", \"opencurrent\" : 0, \"opentotal\" : " + opentotal + "}");
			} else {
				ret.append("{ \"useritem\" : ");
				ret.append(userItem.toJSON(true));
				ret.append(", \"opencurrent\" : " + opencurrent + ", \"opentotal\" : " + opentotal);
				ret.append("}");
			}
			// System.out.println(ret.toString());
			return ret.toString();
		} catch (Exception e) {
			return "{ msg: \"ERROR\"}";
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
	@Path("/{user}/{pass}/lessons")
	@Produces("application/json")
	public String lessons(@PathParam("user") String username, @PathParam("pass") String password) {
		StringBuffer ret = new StringBuffer();

		try {
			User user = getUser(username, password);
			Vector<UserLesson> userLessons = user.getLessons();

			ret.append("{ \"userlessons\" : [ ");

			Iterator<UserLesson> it = userLessons.iterator();
			while (it.hasNext()) {
				ret.append(it.next().toJSON());
				if (it.hasNext())
					ret.append(", ");
			}

			ret.append(" ] }");

			return ret.toString();
		} catch (Exception e) {
			return "{ msg: \"ERROR\"}";
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
	@Path("/{user}/{pass}/lesson/{ulid}")
	@Produces("application/json")
	public String lesson(@PathParam("user") String username, @PathParam("pass") String password, @PathParam("ulid") long ulid) {
		StringBuffer ret = new StringBuffer();

		try {
			User user = getUser(username, password);
			UserLesson userLesson = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUserLesson(user, ulid);

			ret.append("{ \"userlesson\" :  ");
			ret.append(userLesson.toJSON());
			ret.append(" }");

			return ret.toString();
		} catch (Exception e) {
			return "{ msg: \"ERROR\"}";
		}
	}

	private User getUser(String username, String password) throws Exception {
		User user = BrainSystem.getBrainSystemNoFaces().getBrainDB().getUser(username, password);

		if ((user == null) || (!user.getPassword().equals(password))) {
			throw new Exception("Invalid user.");
		}
		return user;
	}

}
