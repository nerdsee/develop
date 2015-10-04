package org.stoevesand.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

import org.jboss.logging.Logger;

public class StringUtils {

	private static Logger log = Logger.getLogger(StringUtils.class);

	public static String replaceSubstring(String text, String label, String replace) {
		String ret = text;

		try {
			StringBuffer buf = new StringBuffer();
			int oldpos = 0;
			int pos = text.indexOf(label);
			int lablen = label.length();
			while (pos > 0) {

				buf.append(text.substring(oldpos, pos));
				buf.append(replace);

				oldpos = pos + lablen;
				pos = text.indexOf(label, pos + lablen);
			}
			buf.append(text.substring(oldpos));

			ret = buf.toString();
		} catch (Exception e) {

		}

		return ret;
	}

	public static String getParameter(String query, String param) {
		String ret = null;
		try {
			int pos1=query.indexOf(param+"=");
			if (pos1>=0) {
				int pos2=query.indexOf("&",pos1);
				if (pos2>=0)
					ret=query.substring(pos1+param.length()+1, pos2);
				else
					ret=query.substring(pos1+param.length()+1);
			}
		} catch (Exception e) {
		}
		return ret;
	}

	public static String loadFileToString(File file) {

		String ret = "";
		StringBuffer buf = new StringBuffer();

		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
			BufferedReader d = new BufferedReader(isr);

			String line = d.readLine();
			while (line != null) {
				buf.append(line);
				buf.append("\n");
				line = d.readLine();
			}
			ret = buf.toString();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: "+file.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//System.out.println(ret);

		return ret;
	}

	public double longestCommonStrings(String eingabe, String loesung, StringBuffer result) {
		Vector<Tuple> hits = new Vector<Tuple>();

		int goodchars = 0;
		int badchars = 0;

		int start1 = 0;
		int start2 = 0;

		int hitstart = 0;
		int hitend = 0;

		int end1 = 1;

		while (start1 <= eingabe.length()) {
			end1 = eingabe.length();
			while (end1 > start1 + 1) {
				String part1 = eingabe.substring(start1, end1);
				// out(part1);
				// out("S2: " + start2);
				int pos = loesung.indexOf(part1, start2);
				if (pos >= 0) {
					hitstart = start1;
					hitend = end1;
					// hitstart = pos;
					// hitend = pos + end1 - start1;

					start2 = pos + part1.length();
					start1 = end1 - 1;
					end1 = 0;
					out(part1, hitstart, hitend);
					goodchars = goodchars + hitend - hitstart;
					hits.add(new Tuple(hitstart, hitend));
				} else
					end1--;
			}
			start1++;
		}

		int pos = 0;
		Enumeration<Tuple> e = hits.elements();

		while (e.hasMoreElements()) {
			Tuple t = (Tuple) e.nextElement();
			result.append("<span class=\"res_f\">");
			String tx = eingabe.substring(pos, t.start);
			if ((tx.length() == 0) && (pos != 0))
				result.append("_");
			else
				result.append(tx);
			result.append("</span>");
			result.append("<span class=\"res_r\">");
			result.append(eingabe.substring(t.start, t.end));
			result.append("</span>");
			pos = t.end;
		}
		if (pos < eingabe.length()) {
			result.append("<span class=\"res_f\">");
			result.append(eingabe.substring(pos).toLowerCase());
			result.append("</span>");
		}

		badchars = eingabe.length() - goodchars;
		return badchars == 0 ? 100 : (double) goodchars / (double) badchars;
	}

	class Tuple {
		int start;
		int end;

		Tuple(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}

	public static String formatWebString(String text, String prefix, boolean toXML) {
		StringBuffer ret = new StringBuffer();
		try {
			boolean listOpen = false;

			BufferedReader br = new BufferedReader(new StringReader(text));

			String line = br.readLine();
			while (line != null) {
				if (line.equals("***")) {
					listOpen = !listOpen;
					if (listOpen)
						ret.append("<" + prefix + "ul>");
					else
						ret.append("</" + prefix + "ul>");
				} else if (line.equals("+++")) {
					listOpen = !listOpen;
					if (listOpen)
						ret.append("<" + prefix + "ol>");
					else
						ret.append("</" + prefix + "ol>");
				} else {
					if (listOpen)
						ret.append("<" + prefix + "li>");
					if (toXML)
						ret.append("<![CDATA[");
					ret.append(line);
					if (toXML)
						ret.append("]]>");
					if (listOpen)
						ret.append("</" + prefix + "li>");
					else
						ret.append("<" + prefix + "br/>");
				}
				line = br.readLine();
			}

		} catch (Exception e) {
			log.error("Failed to format String: " + text);
			log.debug(e);
			return text;
		}
		return ret.toString();
	}

	public static String generateNickname(String name) {
		String ret = "<anon>";
		int pos = name.indexOf("@");
		if (pos > 0)
			name = name.substring(0, pos);
		char[] cn = name.toCharArray();
		int len = name.length();
		int len2 = name.length() / 2;

		for (; len2 < len; len2++)
			cn[len2] = '#';

		ret = new String(cn);
		return ret;
	}

	public static void main(String[] p) {
		test1(p);
		// test2(p);
	}

	public static void test2(String[] p) {
		String text = "12345@NAME@67@NAME@89@EMAIL@0";
		String erg = replaceSubstring(text, "@NAME@", "Jan");
		System.out.println(erg);

		text = "12345@NAME@67@NAME@89@EMAIL@0";
		erg = replaceSubstring(text, "@XXX@", "Jan");
		System.out.println(erg);

		text = "12345@NAME@67@NAME@89@EMAIL@0";
		erg = replaceSubstring(text, "@EMAIL@", "test@test.de");
		System.out.println(erg);

	}

	public static void test1(String[] p) {

		// String text1="aburido";
		// String text2="aburrido";

		// String text1="abuaburido";
		// String text2="aburrido";

		String eingabe = "12345";
		String loesung = "minus";

		StringUtils cp = new StringUtils();

		StringBuffer lcs = new StringBuffer();
		double q = cp.longestCommonStrings(eingabe, loesung, lcs);
		cp.out(eingabe);
		cp.out(loesung);
		cp.out(lcs.toString());
		cp.out("Q: " + q);

	}

	private void out(String t, int hitstart, int hitend) {
		// System.out.println(t);
		// System.out.println(" (" + hitstart + "," + hitend + ")");
	}

	void out(String t) {
		System.out.println(t);
	}

}
