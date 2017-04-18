package org.stoevesand.brain.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.util.StringUtils;

public class AudioServlet extends HttpServlet {

	private static Logger log = LogManager.getLogger(AudioServlet.class);

	private static final long serialVersionUID = 1L;

	private static final int METHOD_POST = 0;
	private static final int METHOD_GET = 1;

	private StringUtils cp = new StringUtils();

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
			// log.debug(scl.substring(1));
			StringBuffer buffer = new StringBuffer(scl.substring(1));

			System.out.println("Audio Servlet started.");

			String username = nextToken(buffer);
			String pass = nextToken(buffer);
			String s_itemID = nextToken(buffer);

			System.out.println("user: " + username);
			System.out.println("pass: " + pass);
			System.out.println("item: " + s_itemID);

			if (method == METHOD_POST)
				storeFile(request, s_itemID);
			else
				sendFile(response, s_itemID);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void sendFile(HttpServletResponse response, String s_itemID) {
		System.out.println("send file.");
		File f = new File("c:\\tmp\\audio\\audio_" + s_itemID + ".wav");
		//File f = new File("c:\\tmp\\audio\\ding.wav");
		try {
			OutputStream os = response.getOutputStream();
			FileInputStream fis = new FileInputStream(f);

			byte[] buffer = new byte[1024];
			int len = fis.read(buffer);
			while (len != -1) {
				os.write(buffer,0,len);
				len = fis.read(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void storeFile(HttpServletRequest request, String s_itemID) {
		try {
			InputStream in = request.getInputStream();

			byte[] buf = new byte[1024];
			int num = in.read(buf);

			File f = new File("c:\\tmp\\audio\\audio_" + s_itemID + ".wav");

			FileOutputStream fos = new FileOutputStream(f);

			while (num != -1) {
				fos.write(buf, 0, num);
				num = in.read(buf);
			}

			// BufferedReader r = new BufferedReader(new InputStreamReader(in));
			// StringBuffer buf = new StringBuffer();
			// String line;
			// while ((line = r.readLine()) != null) {
			// buf.append(line);
			// }
			String s = buf.toString();
			System.out.println(s);

			fos.close();

			// log.debug("name : " + username);
			// User user =
			// BrainSystem.getBrainSystemNoFaces().getBrainDB().getUser(username);

			// if ((user != null) && (user.getPassword().equals(pass))) {
			// String action = nextToken(buffer);
			// } else {
			// response.getOutputStream().print("<error>unknown user</error>");
			// }

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
				// log.debug("SBnt: " + buffer.toString());
			} else {
				ret = buffer.toString();
				buffer = new StringBuffer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
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
