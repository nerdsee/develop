package org.stoevesand.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Answer;
import org.stoevesand.brain.model.Item;
import org.stoevesand.brain.model.Lesson;

public class DictMake {

	private static Logger log = Logger.getLogger(DictMake.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DictMake dc = new DictMake();

		String filename = args[0] + "/resources/dicts/capax/phil-2.txt";

		System.out.println("File: " + filename);

		//dc.makeDictRegex(filename);
		dc.makeDict(filename);

	}

	public static void invertLesson(Lesson lesson) {
		OutputStreamWriter out;

		String filename = "c:/brain/lessons/";
		filename = filename + lesson.getTitle() + "_INV.xml";

		HashMap<String, Item> hm = new HashMap<String, Item>();

		try {
			out = new OutputStreamWriter(new FileOutputStream(filename + ".dict.xml"), "utf-8");

			System.out.println("Lesson: " + lesson.getDescription());

			writeHeader(out, lesson.getTitle() + "_INV", lesson.getDescription() + " (inv)", lesson.getKeyboardLayout());
			outprintln(out, "<items>");

			for (Item orgitem : lesson.getItems()) {
				StringBuffer newtext = new StringBuffer();
				if (orgitem.getAnswersSize() > 1)
					newtext.append("+++\n");
				for (Answer a : orgitem.getAnswers()) {
					newtext.append(a.getText()).append("\n");
				}
				if (orgitem.getAnswersSize() > 1)
					newtext.append("+++\n");
				String newcomment = "";
				Item newitem = new Item(newtext.toString(), newcomment, orgitem.getExtId(), orgitem.getChapter());

				Vector<Answer> answers = new Vector<Answer>();
				answers.add(new Answer(orgitem.getText(), true, 1));
				answers.add(new Answer(orgitem.getComment(), true, 2));

				newitem.appendAnswers(answers);
				writeItem(newitem, out);

			}

			outprintln(out, "</items>");
			writeFooter(out);
			out.close();
			System.out.println("done. (invert)");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Format: <title> <description> <keyboardlayout> <frage>$$<comment>$$<chapter>$$<extid>::<answer>#<answer>#...
	 * 
	 * @param filename
	 */
//* Format: 
//	<title> 
//	<description> 
//	<keyboardlayout> 
//	<frage>$$<comment>$$<chapter>$$<extid>::<answer>#<answer>#...

	private void makeDict(String filename) {

		OutputStreamWriter out;

		HashMap<String, Item> hm = new HashMap<String, Item>();

		try {
			out = new OutputStreamWriter(new FileOutputStream(filename + ".dict.xml"), "utf-8");

			String fullleft = "";
			String left = "";
			String fullright = "";
			String comment = "";
			String line = "";

			InputStreamReader isr = new InputStreamReader(new FileInputStream(filename), "utf-8");
			BufferedReader br = new BufferedReader(isr);

			String title = br.readLine();
			String desc = br.readLine();
			String kl = br.readLine();
			writeHeader(out, title, desc, kl);

			line = br.readLine();
			outprintln(out, "<items>");
			while (line != null) {
				if (line.trim().length() > 0) {

					String[] split = line.split("::");
					// StringTokenizer st = new StringTokenizer(line, "::");
					// System.out.println(line);
					fullleft = split[0];
					fullright = split[1];

					StringTokenizer stl = new StringTokenizer(fullleft, "$$");
					left = stl.nextToken().trim();
					if (stl.hasMoreTokens())
						comment = stl.nextToken().trim();
					else
						comment = "";

					int chapter = 0;
					if (stl.hasMoreTokens()) {
						String es = stl.nextToken().trim();
						chapter = Integer.parseInt(es);
					} else
						chapter = 1;

					int extId = 0;
					if (stl.hasMoreTokens()) {
						String es = stl.nextToken().trim();
						extId = Integer.parseInt(es);
					} else
						extId = 0;

					Item item = hm.get(left.trim());
					if (item == null) {
						item = new Item(left.trim(), comment.trim(), extId, chapter);
						hm.put(left.trim(), item);
					} else {
						System.out.println("da: " + item.getExtId());
						System.out.println("ne: " + extId);
					}

					Vector<Answer> answers = new Vector<Answer>();

					StringTokenizer at = new StringTokenizer(fullright, "#;");
					while (at.hasMoreTokens()) {
						String ats = at.nextToken().trim();
						if (ats.length() > 0)
							answers.add(new Answer(ats, true, 0));
					}
					item.appendAnswers(answers);

				}
				line = br.readLine();
			}

			for (Item item : hm.values()) {
				writeItem(item, out);
			}

			outprintln(out, "</items>");
			writeFooter(out);
			out.close();

			System.out.println("done.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void makeDictRegex(String filename) {

		OutputStreamWriter out;

		HashMap<String, Item> hm = new HashMap<String, Item>();

		try {
			out = new OutputStreamWriter(new FileOutputStream(filename + ".dict.xml"), "utf-8");

			String line = "";

			InputStreamReader isr = new InputStreamReader(new FileInputStream(filename), "utf-8");
			BufferedReader br = new BufferedReader(isr);

			String title = br.readLine();
			String desc = br.readLine();
			String kl = br.readLine();
			writeHeader(out, title, desc, kl);

			String regex = "\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\"";
			// "1","2","好","好","hǎo","good; well; fine; O.K.","A"
			// chapter,id,trad,simp,pinyin,translation,type
			Pattern pattern = Pattern.compile(regex);

			int count = 0;

			line = br.readLine();
			outprintln(out, "<items>");
			while (line != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					count++;
					String s_chapter = matcher.group(1);
					int chapter = toInt(s_chapter);
					String s_id = matcher.group(2);
					int id = toInt(s_id);
					String s_trad = matcher.group(3);
					String s_simp = matcher.group(4);
					String s_pinyin = matcher.group(5);
					String s_trans = matcher.group(6);
					String s_type = matcher.group(7);

					long extId = chapter * 1000 + id;

					Item item = hm.get(s_simp);
					if (item == null) {
						item = new Item(s_simp, s_pinyin.trim(), extId, chapter);
						hm.put(s_simp, item);
					} else {
						System.out.println("da: " + item.getExtId());
						// System.out.println("ne: " + extId);
					}

					Vector<Answer> answers = new Vector<Answer>();
					
					StringTokenizer at = new StringTokenizer(s_trans, ";");
					while (at.hasMoreTokens()) {
						String ats = at.nextToken().trim();
						if (ats.length() > 0)
							answers.add(new Answer(ats, true, 0));
					}
					item.appendAnswers(answers);

				} else {
					System.out.println(line);
				}
				line = br.readLine();
			}

			for (Item item : hm.values()) {
				writeItem(item, out);
			}

			outprintln(out, "</items>");
			writeFooter(out);
			out.close();

			System.out.println("done. (" + count + ")");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int toInt(String text) {
		int ret = 0;
		try {
			ret = Integer.parseInt(text);
		} catch (Exception e) {
		}
		return ret;
	}

	private long toLong(String text) {
		long ret = 0;
		try {
			ret = Long.parseLong(text);
		} catch (Exception e) {
		}
		return ret;
	}

	private static void outprintln(OutputStreamWriter out, String string) {
		try {
			out.write(string);
			out.write("\n");
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	private void makeDictTest(String filename) {

		OutputStreamWriter out;

		try {
			// out = new PrintWriter(new BufferedWriter(new FileWriter(filename +
			// ".dict.xml")));
			// out = new FileWriter(filename + ".dict.xml");
			out = new OutputStreamWriter(new FileOutputStream(filename + ".dict.xml"), "utf-8");

			System.out.println("ENC: " + out.getEncoding());

			String line = "";
			InputStreamReader isr = new InputStreamReader(new FileInputStream(filename), "utf-8");
			System.out.println("FNC: " + isr.getEncoding());
			BufferedReader br = new BufferedReader(isr);
			line = br.readLine();
			while (line != null) {
				out.write(line);
				out.write("\n");
				line = br.readLine();
			}

			out.close();

			System.out.println("done.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void writeItem(Item item, OutputStreamWriter out) {
		outprintln(out, item.toXML());
	}

	private static void writeHeader(OutputStreamWriter out, String title, String desc, String kl) {
		outprintln(out, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		outprintln(out, "		<brain>");
		outprintln(out, "			<lessons>");
		outprintln(out, "			<lesson>");
		outprintln(out, "			<title><![CDATA[" + title + "]]></title>");
		outprintln(out, "			<description><![CDATA[" + desc + "]]></description>");
		outprintln(out, "			<keyboardLayout>" + kl + "</keyboardLayout>");
		outprintln(out, "			<type>1</type>");
	}

	private static void writeFooter(OutputStreamWriter out) {
		outprintln(out, "			</lesson>");
		outprintln(out, "			</lessons>");
		outprintln(out, "		</brain>");
	}

}
