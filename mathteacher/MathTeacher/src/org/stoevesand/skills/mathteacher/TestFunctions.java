package org.stoevesand.skills.mathteacher;

import java.util.Random;

public class TestFunctions {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		generateNumbers();
		
	}

	private static void generateNumbers() {

		StringBuffer bufa=new StringBuffer();
		
		for(int a=0; a<=2; a++)
			for(int b=0; b<=2; b++) {
				bufa.append(a);
				bufa.append(b);
			}

		String f = bufa.toString();
		
		Random rnd = new Random();
		
		while (f.length()>0) {
			int pos = rnd.nextInt(f.length()/2)*2;
			String a = f.substring(pos, pos+1);
			String b = f.substring(pos+1, pos+2);
			f = f.substring(0,pos) + f.substring(pos+2);
			int ai = Integer.parseInt(a) + 1;
			int bi = Integer.parseInt(b) + 1;
			System.out.println("A: "+ ai);
			System.out.println("B: "+ bi);
			System.out.println(f);
		}
		
	}

}
