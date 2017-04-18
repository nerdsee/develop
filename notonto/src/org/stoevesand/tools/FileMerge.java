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

public class FileMerge {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String sdir = "C:/level_b/temp/txt";
		File dir = new File(sdir);

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sdir + "/fullfile.txt"), "UTF-8"));

			if (dir.isDirectory()) {

				File[] files = dir.listFiles();

				for (File f : files) {
					if ((f.isFile()) && (!f.getName().equals("fullfile.txt"))) {
						BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
						String id = f.getName();
						id = id.substring(0, id.length() - 4);
						System.out.println(f.getName() + "-" + id);
						String line = in.readLine();
						while (line != null) {
							out.write(id+",");
							out.write(line);
							out.write("\n");
							line = in.readLine();
						}
						in.close();
					}
				}

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

}
