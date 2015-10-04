
function csc(evt, parent, code) {
		return false;
}

function parse(parent) {
		text = parent.value;
		outline = $("[id$='pinyin']");
		outline.val(addtones(text));
		//outline.replaceChild(document.createTextNode(addtones(text)), outline.lastChild);
		return true;
}
// * Javascript Pinyin Tone Tool
// * Mark Wilbur
// * Copyright (c) 2005-2006
// * If you want to copy this and put it on your page fine, but give credit and link back to
// * http://toshuo.com

// To use this on your page, you need a form called "tonetool" with a textarea box called "inputtext"
// The submit button should call the function addtones()

// The output is SHOULD BE correct. If you find any errors, email me at doubtingtoshuo@gmail.com
// - The first vowel in the syllable is the only one to become accented
// - For the u: (umlat) character, this converter follows the convention of using the letter v.

// Array of vowels used in the conversion
var vowels = new Array ("a","e","i","o","u","v","ü");
var umlatu = "ü";

// Array of vowels with tones
var tones = new Array ("ā","ē","ī","ō","ū","ǖ","á","é","í","ó","ú","ǘ","ǎ", "ě", "ǐ", "ǒ", "ǔ", "ǚ","à","è","ì","ò","ù","ǜ");

function addtones (textin) {

//textin = document.tonetool.inputtext.value;
textin=textin.toLowerCase();

//alert(textin);

currentword = "";
currentchar = "";
i = 0;
numletters = textin.length;
textout = ""; // final output
tempword = "";
usevowel = 1; // which vowel will have the tone over it
foundvowel = 0;

for (i=0; i<=numletters; i++) {
	currentchar = textin.charAt (i);
	currentnumvalue = currentchar - 1;

	// numbers 1-5 are tone marks, build the word until we hit one
	if ( !(currentchar.match(/[1-5]/)) ) {
		if ( currentchar.match(/[aeiouvü]/)) foundvowel++;
		// if the last character was a vowel and this isn't...
		if ( ((foundvowel != 0))  && (currentchar.match(/[^aeiouvüngr]/))  || (currentchar == "")) {
			textout = textout + currentword;
			currentword = currentchar;
		} else {
			currentword = currentword + currentchar;
		}
	} else {
		tempword=""; // the word being built in this loop
		foundvowel = 0; // number of vowels found in the word
		usevowel = 1; // which vowel (1st or 2nd) will get the tone mark

		wordlen = currentword.length;

		if ( !(currentword.match(/[a-zü]/)) ) {
			textout = textout + currentword + currentchar; 
			currentword = "";
		}

		if ( currentword.match(/i[aeou]/) ) usevowel = 2;
		if ( currentword.match(/u[aeio]/) ) usevowel = 2;
		if ( currentword.match(/[vü]e/) ) usevowel = 2;

		// We'll check either the first or the first two vowels, depending on which should have the tone
		for (j=0; (j<=wordlen) && (foundvowel<usevowel); j++) {
		// Check to see if the character is a vowel
		for (vowelnum=0; vowelnum<7; vowelnum++) {
			if (currentword.charAt (j) == vowels [ vowelnum ]) {
				// It's a vowel - convert to corresponding numbered tone character from tones array
				// If tone is 5th (Neutral tone) - Leave it as the normal vowel
				if (currentnumvalue<=3) {
					if (vowelnum == 6) currentchar = tones [5 + (currentnumvalue *6)]; // Handle the damned ü for Europeans who can input it directly
					else currentchar = tones [ vowelnum + (currentnumvalue * 6)];
				} else {
					if (vowelnum == 5) currentchar = umlatu; //neutral tone umlat
					else currentchar = vowels [ vowelnum ]; //all other neutral tones
				}
		
				foundvowel++; // Increment the counter for vowels found in the word
		
				if (foundvowel>=usevowel) {
				// rebuild word with the tone if this vowel should have the tone
					tempword="";
					for (k=0; k<=wordlen; k++) {
						if (k == j) {
							tempword = tempword + currentchar;
						} else { //just copy from the input, but turn all remaining v's into umlated u's
							if (currentword.charAt(k) == vowels[5]) tempword = tempword + umlatu;
							else tempword = tempword + currentword.charAt(k);
						}
					}
					currentword="";
				}
			}
		}
		textout = textout + tempword;
		}
	}
}
//alert(textout);
//document.tonetool.inputtext.value = textout;
//document.tonetool.outputtext.value = textout;
return textout;
}

