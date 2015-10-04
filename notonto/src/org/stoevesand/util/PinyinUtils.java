package org.stoevesand.util;

import org.jboss.logging.Logger;


public class PinyinUtils {

	private static Logger log = Logger.getLogger(PinyinUtils.class);

	public static void main(String[] args) {
		String text = "hao3";
		
		System.out.println("OUT: " + addtones(text));
	}
	
	public static String addtones(String textin) {

		// Array of vowels used in the conversion
		char[] vowels = new char[] { 'a', 'e', 'i', 'o', 'u', 'v', 'ü' };
		String umlatu = "ü";

		// Array of vowels with tones
		String[] tones = new String[] { "ā", "ē", "ī", "ō", "ū", "ǖ", "á", "é", "í", "ó", "ú", "ǘ", "ǎ", "ě", "ǐ", "ǒ", "ǔ", "ǚ", "à", "è", "ì", "ò", "ù", "ǜ" };

		// textin = document.tonetool.inputtext.value;
		textin = textin.toLowerCase();

		// alert(textin);

		String currentword = "";
		String currentchar = "";
		int i = 0;
		int numletters = textin.length();
		String textout = ""; // final output
		String tempword = "";
		int usevowel = 1; // which vowel will have the tone over it
		int foundvowel = 0;

		for (i = 0; i <= numletters; i++) {
			currentchar = new Character(textin.charAt(i)).toString();
			int currentnumvalue = currentchar.charAt(0) - 1;

			// numbers 1-5 are tone marks, build the word until we hit one
			if (!(currentchar.matches("[1-5]"))) {
				if (currentchar.matches("[aeiouvü]"))
					foundvowel++;
				// if the last character was a vowel and this isn't...
				if (((foundvowel != 0)) && (currentchar.matches("[^aeiouvüngr]")) || (currentchar.equals(""))) {
					textout = textout + currentword;
					currentword = currentchar;
				} else {
					currentword = currentword + currentchar;
				}
			} else {
				tempword = ""; // the word being built in this loop
				foundvowel = 0; // number of vowels found in the word
				usevowel = 1; // which vowel (1st or 2nd) will get the tone mark

				int wordlen = currentword.length();

				if (!(currentword.matches("[a-zü]"))) {
					textout = textout + currentword + currentchar;
					currentword = "";
				}

				if (currentword.matches("i[aeou]"))
					usevowel = 2;
				if (currentword.matches("u[aeio]"))
					usevowel = 2;
				if (currentword.matches("[vü]e"))
					usevowel = 2;

				// We'll check either the first or the first two vowels, depending on
				// which should have the tone
				for (int j = 0; (j <= wordlen) && (foundvowel < usevowel); j++) {
					// Check to see if the character is a vowel
					for (int vowelnum = 0; vowelnum < 7; vowelnum++) {
						if (currentword.charAt(j) == vowels[vowelnum]) {
							// It's a vowel - convert to corresponding numbered tone character
							// from tones array
							// If tone is 5th (Neutral tone) - Leave it as the normal vowel
							if (currentnumvalue <= 3) {
								if (vowelnum == 6)
									currentchar = tones[5 + (currentnumvalue * 6)];
								else
									currentchar = tones[vowelnum + (currentnumvalue * 6)];
							} else {
								if (vowelnum == 5)
									currentchar = umlatu; // neutral tone umlat
								else
									currentchar = ""+vowels[vowelnum]; // all other neutral tones
							}

							foundvowel++; // Increment the counter for vowels found in the
														// word

							if (foundvowel >= usevowel) {
								// rebuild word with the tone if this vowel should have the tone
								tempword = "";
								for (int k = 0; k <= wordlen; k++) {
									if (k == j) {
										tempword = tempword + currentchar;
									} else { // just copy from the input, but turn all remaining
														// v's
										// into umlated u's
										if (currentword.charAt(k) == vowels[5])
											tempword = tempword + umlatu;
										else
											tempword = tempword + currentword.charAt(k);
									}
								}
								currentword = "";
							}
						}
					}
					textout = textout + tempword;
				}
			}
		}
		// alert(textout);
		// document.tonetool.inputtext.value = textout;
		// document.tonetool.outputtext.value = textout;
		return textout;
	}
}
