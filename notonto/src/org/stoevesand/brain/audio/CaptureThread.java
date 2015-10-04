/**
 * 
 */
package org.stoevesand.brain.audio;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.TargetDataLine;

class CaptureThread extends Thread {
	/**
	 * 
	 */
	private final RecApplet recApplet;
	public boolean stopCapture = false;
	//ByteArrayOutputStream byteArrayOutputStream = null;
	TargetDataLine targetDataLine = null;;

	CaptureThread(RecApplet recApplet, TargetDataLine targetDataLine) {
		this.recApplet = recApplet;
		this.targetDataLine = targetDataLine;
	}

	byte tempBuffer[] = new byte[1024];

	public void run() {
		int total = 0;
		recApplet.byteArrayOutputStream = new ByteArrayOutputStream();
		stopCapture = false;
		try {
			long maxtime=1000*10;// 10 seconds
			long limit = System.currentTimeMillis()+maxtime;
			while ( (!stopCapture) && (System.currentTimeMillis()<limit)) {

				int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
				if (cnt > 0) {

					total += cnt;

					recApplet.byteArrayOutputStream.write(tempBuffer, 0, cnt);
				}
			}
			recApplet.byteArrayOutputStream.close();
			System.out.println("total: " + total);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}
}