package org.stoevesand.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;

public class DictCheck {

	private static Logger log = Logger.getLogger(DictCheck.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DictCheck dc = new DictCheck();
		
		String filename=args[0]+"/resources/dict-1.1.4/dicts/es-de/alle-es-de.txt";
		
		log.debug("File: " + filename);
		
		File file = new File(filename);
		
		dc.lookForDoubles(file);

	}

	private void lookForDoubles(File file) {

		String last="";
		String curr="";
		String line="";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			
			while (line!=null) {
				StringTokenizer st=new StringTokenizer(line, "::");
				
				curr = st.nextToken();
				if (curr.equals(last)) {
					log.debug(line);
				}
				last=curr;
				line = br.readLine();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
