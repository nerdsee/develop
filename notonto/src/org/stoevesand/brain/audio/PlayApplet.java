package org.stoevesand.brain.audio;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class PlayApplet extends JApplet implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3461380456386262154L;

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

	JButton playBtn = null;

	private void createGUI() {

		getContentPane().setLayout(new GridBagLayout());

		ImageIcon playIcon = createImageIcon("images/play.jpg");
		playBtn = new JButton(playIcon);
		playBtn.setBackground(Color.white);
		playBtn.setMargin(new Insets(0, 0, 0, 0));
		playBtn.setFocusPainted(false);
		playBtn.setContentAreaFilled(false); 		
		playBtn.setEnabled(true);

		playBtn.setActionCommand("play");
		playBtn.addActionListener(this);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill=GridBagConstraints.CENTER;
		getContentPane().add(playBtn,c);

		getContentPane().setSize(100, 50);

	}

  /** Returns an ImageIcon, or null if the path was invalid. */
  protected static ImageIcon createImageIcon(String path) {
      java.net.URL imgURL = PlayApplet.class.getResource(path);
      if (imgURL != null) {
          return new ImageIcon(imgURL);
      } else {
          System.err.println("Couldn't find file: " + path);
          return null;
      }
  }

	public void start() {
		//play();
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

	public void play() {
		try {

			String itemID=getParameter("itemID");
			
			URL u = new URL("http://localhost:8090/audio/jan/jan/" + itemID);
			audioInputStream = AudioSystem.getAudioInputStream(u);
			AudioFormat audioFormat = audioInputStream.getFormat();

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

	public void actionPerformed(ActionEvent e) {

		if ("play".equals(e.getActionCommand())) {
			play();
		}

	}

}
