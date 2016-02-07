package org.stoevesand.brain.audio;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class RecApplet extends JApplet implements ActionListener {

	JTextField text = new JTextField("1234");

	public void init() {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createGUI();
				}
			});
		} catch (Exception e) {
			System.err.println("createGUI didn't successfully complete");
		}
	}

	JButton captureBtn = null;
	JButton stopBtn = null;
	JButton playBtn = null;
	JButton sendBtn = null;

	private void createGUI() {

		captureBtn = new JButton("Capture");
		stopBtn = new JButton("Stop");
		playBtn = new JButton("Playback");
		sendBtn = new JButton("Send");

		captureBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		playBtn.setEnabled(true);
		sendBtn.setEnabled(false);

		captureBtn.setActionCommand("capture");
		captureBtn.addActionListener(this);
		getContentPane().add(captureBtn);

		stopBtn.setActionCommand("stop");
		stopBtn.addActionListener(this);
		getContentPane().add(stopBtn);

		playBtn.setActionCommand("play");
		playBtn.addActionListener(this);
		getContentPane().add(playBtn);

		text.setColumns(10);
		getContentPane().add(text);

		sendBtn.setActionCommand("send");
		sendBtn.addActionListener(this);
		getContentPane().add(sendBtn);

		getContentPane().setLayout(new FlowLayout());
		getContentPane().setSize(400, 50);

	}

	public void start() {
	}

	public void stop() {
	}

	public void destroy() {
		cleanUp();
	}

	private void cleanUp() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
				}
			});
		} catch (Exception e) {
			System.err.println("cleanUp didn't successfully complete");
		}

	}

	// boolean stopCapture = false;
	public ByteArrayOutputStream byteArrayOutputStream;
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;

	CaptureThread captureThread = null;

	private void captureAudio() {
		try {
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.addLineListener(new LineListener() {

				public void update(LineEvent event) {
					System.out.println("E: " + event.getType().toString());
				}

			});
			targetDataLine.open(audioFormat);
			targetDataLine.start();

			captureThread = new CaptureThread(this, targetDataLine);
			Thread ct = new Thread(captureThread);
			ct.start();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void playAudio() {
		try {
			byte audioData[] = byteArrayOutputStream.toByteArray();

			InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
			AudioFormat audioFormat = getAudioFormat();
			System.out.println("X: " + audioFormat.getFrameSize());
			audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioData.length / audioFormat.getFrameSize());

			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sourceDataLine.addLineListener(new LineListener() {
				public void update(LineEvent event) {
					System.out.println("S: " + event.getType().toString());
				}
			});
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();

			Thread playThread = new Thread(new PlayThread(sourceDataLine, audioInputStream));
			playThread.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void sendAudio() {
		try {
			byte audioData[] = byteArrayOutputStream.toByteArray();
			System.out.println("arr-length: " + byteArrayOutputStream.size());

			ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
			AudioInputStream ais = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());

			String boundary = "-----------" + System.currentTimeMillis();

			URL u = new URL("http://localhost:8090/audio/jan/jan/" + text.getText().trim());
			URLConnection c = u.openConnection();
			c.setDoOutput(true);
			c.setDoInput(true);
			c.setUseCaches(false);
			c.setRequestProperty("Connection", "Keep-Alive");
			// c.setRequestProperty( "HTTP_REFERER", codebase);
			c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			DataOutputStream dstream = new DataOutputStream(c.getOutputStream());
			// dstream.writeBytes("--" + boundary + "\r\n");

			System.out.println("data-length: " + audioData.length);
			AudioSystem.write(ais, AudioFileFormat.Type.AU, dstream);
			// ais.reset();
			// AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);

			dstream.flush();
			dstream.close();

			// ******if we do not read the server response as bellow, the files are
			// not upload to server.
			try {
				DataInputStream in = new DataInputStream(new BufferedInputStream(c.getInputStream()));
				String sIn = in.readLine();
				while (sIn != null) {
					if (sIn != null) {
						System.out.println(sIn);
					}
					sIn = in.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("file sent.");

		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}

	private AudioFormat getAudioFormat() {
		float sampleRate = 22050.0F;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		// 8,16
		int channels = 1;
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = false;
		// true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	public void actionPerformed(ActionEvent e) {
		
		System.out.println("ev: " + e.toString());
		
		if ("stop".equals(e.getActionCommand())) {
			captureBtn.setEnabled(true);
			stopBtn.setEnabled(false);
			playBtn.setEnabled(true);
			sendBtn.setEnabled(true);

			captureThread.stopCapture = true;
		} else if ("send".equals(e.getActionCommand())) {
			sendAudio();
		} else if ("capture".equals(e.getActionCommand())) {
			captureBtn.setEnabled(false);
			stopBtn.setEnabled(true);
			playBtn.setEnabled(true);
			sendBtn.setEnabled(false);

			captureAudio();

		} else if ("play".equals(e.getActionCommand())) {
			playAudio();
		}

	}

}
