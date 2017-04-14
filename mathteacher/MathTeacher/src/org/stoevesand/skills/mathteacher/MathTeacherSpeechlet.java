/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.stoevesand.skills.mathteacher;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

/**
 * This sample shows how to create a simple speechlet for handling intent
 * requests and managing session interactions.
 */
public class MathTeacherSpeechlet implements Speechlet {
	private static final Logger log = LoggerFactory.getLogger(MathTeacherSpeechlet.class);

	private static final String SLOT_ZAHL = "zahl";

	private String aufgaben;

	private int aufgabe_a;

	private int aufgabe_b;

	@Override
	public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		return getWelcomeResponse();
	}

	@Override
	public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		// Get intent from the request object.
		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;

		// Note: If the session is started with an intent, no welcome message
		// will be rendered;
		// rather, the intent specific response will be returned.
		if ("AllNumbersIntent".equals(intentName)) {
			return setAllNumbers(intent, session);
		} else if ("SingleNumberIntent".equals(intentName)) {
			return prepareSingleNumber(intent, session);
		} else if ("AnswerIntent".equals(intentName)) {
			return answer(intent, session);
		} else if ("PlusIntent".equals(intentName)) {
			return preparePlus(intent, session);
		} else if ("MinusIntent".equals(intentName)) {
			return prepareMinus(intent, session);
		} else if ("AMAZON.StopIntent".equals(intent.getName())) {
			String speech = "<speak>Bis zum nächsten Mal.</speak>";
			return getSpeechletResponse(speech, speech, false);
		} else {
			throw new SpeechletException("Invalid Intent");
		}
	}

	private SpeechletResponse prepareMinus(Intent intent, Session session) {

		generateMinus();
		getNext();

		session.setAttribute("mode", "MINUS");
		session.setAttribute("aufgaben", aufgaben);
		session.setAttribute("sa", Integer.toString(aufgabe_a));
		session.setAttribute("sb", Integer.toString(aufgabe_b));

		String speechText = String.format("<speak>Ok wir können anfangen. Wir lernen minus rechnen. %s</speak>", getAufgabeText("MINUS"));
		String repromptText = String.format("<speak>%s</speak>", getAufgabeText("MINUS"));

		boolean isAskResponse = true;
		return getSpeechletResponse(speechText, repromptText, isAskResponse);
	}

	private SpeechletResponse preparePlus(Intent intent, Session session) {

		generatePlus();
		getNext();

		session.setAttribute("mode", "PLUS");
		session.setAttribute("aufgaben", aufgaben);
		session.setAttribute("sa", Integer.toString(aufgabe_a));
		session.setAttribute("sb", Integer.toString(aufgabe_b));

		String speechText = String.format("<speak>Ok wir können anfangen. Wir lernen plus rechnen. %s</speak>", getAufgabeText("PLUS"));
		String repromptText = String.format("<speak>%s</speak>", getAufgabeText("PLUS"));

		boolean isAskResponse = true;
		return getSpeechletResponse(speechText, repromptText, isAskResponse);
	}

	private SpeechletResponse prepareSingleNumber(Intent intent, Session session) {

		int mult = 5;

		try {
			mult = Integer.parseInt(intent.getSlot(SLOT_ZAHL).getValue());
		} catch (NumberFormatException e) {
			String speechText = "<speak>Es tut mir leid, ich habe die Zahl nicht verstanden.</speak>";
			return getAskSpeechletResponse(speechText, speechText);
		}

		generateSingleMultiplikation(mult);
		getNext();

		session.setAttribute("mode", "EINMALEINS");
		session.setAttribute("aufgaben", aufgaben);
		session.setAttribute("sa", Integer.toString(aufgabe_a));
		session.setAttribute("sb", Integer.toString(aufgabe_b));

		String speechText = String.format("<speak>Ok wir können anfangen. Wir lernen das ein mal %d <break time='1s'/>. %s</speak>", mult, getAufgabeText("EINMALEINS"));
		String repromptText = String.format("<speak>%s</speak>", getAufgabeText("EINMALEINS"));

		boolean isAskResponse = true;
		return getSpeechletResponse(speechText, repromptText, isAskResponse);
	}

	private String getAufgabeText(String mode) {
		String aufgabeText = "";
		switch (mode) {
			case "EINMALEINS":
				if (aufgabe_a == 1)
					aufgabeText = String.format("Was ist ein mal %d?", aufgabe_b);
				else
					aufgabeText = String.format("Was ist %d mal %d?", aufgabe_a, aufgabe_b);
				break;
			case "MINUS":
				aufgabeText = String.format("Was ist %d minus %d?", aufgabe_a, aufgabe_b);
				break;
			case "PLUS":
				aufgabeText = String.format("Was ist %d plus %d?", aufgabe_a, aufgabe_b);
				break;
		}
		return aufgabeText;
	}

	private SpeechletResponse answer(Intent intent, Session session) {

		String mode = (String) session.getAttribute("mode");
		String sa = (String) session.getAttribute("sa");
		String sb = (String) session.getAttribute("sb");
		aufgabe_a = Integer.parseInt(sa);
		aufgabe_b = Integer.parseInt(sb);
		int erg = 0;

		switch (mode) {
			case "EINMALEINS":
				erg = aufgabe_a * aufgabe_b;
				break;
			case "MINUS":
				erg = aufgabe_a - aufgabe_b;
				break;
			case "PLUS":
				erg = aufgabe_a + aufgabe_b;
				break;
		}

		int zahl = 0;
		try {
			zahl = Integer.parseInt(intent.getSlot(SLOT_ZAHL).getValue());
		} catch (NumberFormatException e) {
			String speechText = String.format("<speak>Es tut mir leid, ich habe die Zahl nicht verstanden. %s</speak>", getAufgabeText(mode));
			return getAskSpeechletResponse(speechText, speechText);
		}

		if (erg == zahl) {
			aufgaben = (String) session.getAttribute("aufgaben");

			if (getNext()) {
				session.setAttribute("aufgaben", aufgaben);
				session.setAttribute("sa", Integer.toString(aufgabe_a));
				session.setAttribute("sb", Integer.toString(aufgabe_b));

				String speechText = String.format("<speak>Richtig. %s</speak>", getAufgabeText(mode));
				String repromptText = String.format("<speak>%s</speak>", getAufgabeText(mode));

				boolean isAskResponse = true;
				return getSpeechletResponse(speechText, repromptText, isAskResponse);
			} else {
				String speechText = String.format("<speak>Super. Du hast alle Aufgaben gelöst</speak>");
				boolean isAskResponse = true;
				return getSpeechletResponse(speechText, speechText, isAskResponse);
			}
		} else {
			String speechText = String.format("<speak>Das war leider falsch. %s</speak>", getAufgabeText(mode));
			String repromptText = String.format("%s", getAufgabeText(mode));

			boolean isAskResponse = true;
			return getSpeechletResponse(speechText, repromptText, isAskResponse);
		}

	}

	private SpeechletResponse setAllNumbers(Intent intent, Session session) {

		generateAllMultiplikation();
		getNext();

		String mode = "EINMALEINS";
		session.setAttribute("mode", mode);
		session.setAttribute("aufgaben", aufgaben);
		session.setAttribute("sa", Integer.toString(aufgabe_a));
		session.setAttribute("sb", Integer.toString(aufgabe_b));

		String speechText = String.format("<speak>Ok wir können anfangen. Wir lernen das ganze ein mal eins. %s</speak>", getAufgabeText(mode));
		String repromptText = String.format("<speak>%s</speak>", getAufgabeText(mode));

		boolean isAskResponse = true;
		return getSpeechletResponse(speechText, repromptText, isAskResponse);
	}

	private void generateAllMultiplikation() {

		StringBuffer bufa = new StringBuffer();

		for (int a = 1; a < 11; a++)
			for (int b = 1; b < 11; b++) {
				append(bufa, a);
				append(bufa, b);
			}

		aufgaben = bufa.toString();

	}

	private void generateSingleMultiplikation(int b) {

		StringBuffer bufa = new StringBuffer();

		for (int a = 1; a < 11; a++) {
			append(bufa, a);
			append(bufa, b);
		}

		aufgaben = bufa.toString();

	}

	private void generatePlus() {

		StringBuffer bufa = new StringBuffer();

		for (int a = 0; a < 21; a++)
			for (int b = 0; b < 21 - a; b++) {
				append(bufa, a);
				append(bufa, b);
			}

		aufgaben = bufa.toString();

	}

	private void generateMinus() {

		StringBuffer bufa = new StringBuffer();

		for (int a = 0; a < 21; a++)
			for (int b = 0; b <= a; b++) {
				append(bufa, a);
				append(bufa, b);
			}

		aufgaben = bufa.toString();

	}

	void append(StringBuffer buf, int n) {
		if (n < 10)
			buf.append(0);
		buf.append(n);
	}

	private boolean getNext() {

		Random rnd = new Random();

		if (aufgaben.length() > 0) {
			int pos = rnd.nextInt(aufgaben.length() / 4) * 4;
			String a = aufgaben.substring(pos, pos + 2);
			String b = aufgaben.substring(pos + 2, pos + 4);
			aufgaben = aufgaben.substring(0, pos) + aufgaben.substring(pos + 4);
			aufgabe_a = Integer.parseInt(a);
			aufgabe_b = Integer.parseInt(b);
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		// any cleanup logic goes here
	}

	/**
	 * Creates and returns a {@code SpeechletResponse} with a welcome message.
	 *
	 * @return SpeechletResponse spoken and visual welcome message
	 */
	private SpeechletResponse getWelcomeResponse() {
		// Create the welcome message.
		String speechText = "<speak>Willkommen beim Mathe Trainer. Was möchtest Du lernen?</speak>";
		String repromptText = "<speak>Möchtest Du das ein mal eins lernen oder plus oder minus</speak>";

		return getSpeechletResponse(speechText, repromptText, true);
	}

	/**
	 * Returns a Speechlet response for a speech and reprompt text.
	 */
	private SpeechletResponse getSpeechletResponse(String speechText, String repromptText, boolean isAskResponse) {

		if (!speechText.startsWith("<speach>")) {
			System.out.println("Speech: " + speechText);
			System.out.println("Reprompt: " + repromptText);
		}
		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle("Session");
		card.setContent(speechText);

		// Create the plain text output.
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		speech.setSsml(speechText);

		if (isAskResponse) {
			// Create reprompt
			SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
			repromptSpeech.setSsml(repromptText);
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(repromptSpeech);

			return SpeechletResponse.newAskResponse(speech, reprompt, card);

		} else {
			return SpeechletResponse.newTellResponse(speech, card);
		}
	}

	private SpeechletResponse getAskSpeechletResponse(String speechText, String repromptText) {

		if (!speechText.startsWith("<speach>")) {
			System.out.println("Speech: " + speechText);
			System.out.println("Reprompt: " + repromptText);
		}
		
		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle("Session");
		card.setContent(speechText);

		// Create the plain text output.
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		speech.setSsml(speechText);

		// Create reprompt
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml(repromptText);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}
}
