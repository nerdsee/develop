package org.stoevesand.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Vector;

public class HanDeDictMake {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HanDeDictMake inst = new HanDeDictMake();
		inst.run();
	}

	void run() {
		// TODO Auto-generated method stub

		HashMap<Integer, Entry> entries = new HashMap<Integer, Entry>();

		String sdir = "C:/level_b/temp/txt";
		File dir = new File(sdir);
		int count=0;

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:/level_b/temp/hsk_level_b.txt"), "UTF-8"));
			// BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
			// FileOutputStream(sdir + "/hsk_level_a.txt"), "UTF-8"));
			
			String[] files = dir.list();
			for (String filename : files) {
				//System.out.println("read filename " + filename);
				File file=new File(dir, filename);
				if (file.isFile()) {
					count++;
					System.out.println("read file: " + file.getName());
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
					String line = in.readLine();
					if (line==null)
						System.out.println("empty file: " + file.getName());
					while (line != null) {
						String fn = file.getName();
						out.write("\""+fn.substring(0,fn.length()-4)+"\",");
						out.write(line+"\n");
						line = in.readLine();
					}
					in.close();
				}
			}
			out.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("P: " + count);
	}

	void run1() {
		// TODO Auto-generated method stub

		HashMap<Integer, Entry> entries = new HashMap<Integer, Entry>();

		String sdir = "C:/level_b/temp/txt";
		File dir = new File(sdir);

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sdir + "/hsk_level_b.txt"), "UTF-8"));
			// BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
			// FileOutputStream(sdir + "/hsk_level_a.txt"), "UTF-8"));

			File f = new File(sdir + "/fullfile.txt");

			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

			// String test = "A**B**C";
			// StringTokenizer st = new StringTokenizer(test, "*");
			// while (st.hasMoreElements()) {
			// System.out.println("T: " + st.nextToken());
			// }

			// System.out.println(f.getName() + "-" + id);
			String line = in.readLine();
			while (line != null) {

				int pos = line.indexOf(",");
				String seid = line.substring(0, pos);
				int extid = Integer.parseInt(seid);
				Entry entry = entries.get(extid);

				line = line.substring(pos + 2);

				String[] sub = line.split("\",\"");
				System.out.println(sub[0]);
				System.out.println(sub[1]);
				System.out.println(sub[2]);
				System.out.println(sub[3]);

				if (entry == null) {
					entry = new Entry();
					entry.extId = extid;
					entries.put(extid, entry);
				}

				entry.sshort = sub[0];
				entry.slong = sub[1];
				entry.pinyin = sub[2];
				String a = sub[3];
				entry.answers.add(a);

				line = in.readLine();
			}
			in.close();

			for (Entry entry : entries.values()) {
				System.out.println("ID: " + entry.extId + " - " + entry.answers.size());
				out.write(entry.sshort + "$$");
				out.write(entry.pinyin + "$$");
				out.write(entry.extId + "::");
				for (String a : entry.answers) {
					out.write(a + "##");
				}
				out.write("\n");
			}

			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	class Entry {
		public String slong = "";
		public String sshort = "";
		public String pinyin = "";
		public int extId = 0;
		public Vector<String> answers = new Vector<String>();

		public Entry() {

		}
	}

}
