/**
 * 
 */
package org.stoevesand.brain.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

class PlayThread extends Thread {
	/**
	 * 
	 */
	// private final RecApplet recApplet;
	SourceDataLine sourceDataLine;
	AudioInputStream audioInputStream;

	/**
	 * @param recApplet
	 */
	public PlayThread(SourceDataLine sourceDataLine, AudioInputStream audioInputStream) {
		this.sourceDataLine = sourceDataLine;
		this.audioInputStream = audioInputStream;
	}

	byte tempBuffer[] = new byte[10000];

	public void run() {
		try {
			int cnt;
			while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
				if (cnt > 0) {
					sourceDataLine.write(tempBuffer, 0, cnt);
				}
			}
			sourceDataLine.drain();
			sourceDataLine.close();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}// end catch
	}// end run
}