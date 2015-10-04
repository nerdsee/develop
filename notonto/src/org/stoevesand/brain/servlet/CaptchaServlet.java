package org.stoevesand.brain.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

public class CaptchaServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(CaptchaServlet.class);

	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("req: " + request.getPathInfo());
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		  String imageFormat = "jpg";
		  response.setContentType("image/" + imageFormat);

		 try {
		   // you can pass in fontSize, width, height via the request
		   
		   Color backgroundColor = new Color(252, 158, 0);
		   Color borderColor = Color.black;
		   Color textColor = Color.white;
		   Color circleColor = new Color(251, 95, 0);
		   Font textFont = new Font("Arial", Font.PLAIN, paramInt(request, "fontSize", 24));
		   int charsToPrint = 5;
		   int width = paramInt(request, "width", 150);
		   int height = paramInt(request, "height", 60);
		   int circlesToDraw = 6;
		   float horizMargin = 20.0f;
		   float imageQuality = 0.95f; // max is 1.0 (this is for jpeg)
		   double rotationRange = 0.7; // this is radians
		   BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		   Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

		   g.setColor(backgroundColor);
		   g.fillRect(0, 0, width, height);

		   // lets make some noisey circles
		   g.setColor(circleColor);
		   for ( int i = 0; i < circlesToDraw; i++ ) {
		     int circleRadius = (int) (Math.random() * height / 2.0);
		     int circleX = (int) (Math.random() * width - circleRadius);
		     int circleY = (int) (Math.random() * height - circleRadius);
		     g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		   }

		   g.setColor(textColor);
		   g.setFont(textFont);

		   FontMetrics fontMetrics = g.getFontMetrics();
		   int maxAdvance = fontMetrics.getMaxAdvance();
		   int fontHeight = fontMetrics.getHeight();

		   // i removed 1 and l and i because there are confusing to users...
		   // Z, z, and N also get confusing when rotated
		   // 0, O, and o are also confusing...
		   // lowercase G looks a lot like a 9 so i killed it
		   // this should ideally be done for every language...
		   // i like controlling the characters though because it helps prevent confusion
		   String elegibleChars = "ABCDEFGHJKLMPQRSTUVWXYabcdefhjkmnpqrstuvwxy23456789";
		   char[] chars = elegibleChars.toCharArray();

		   float spaceForLetters = -horizMargin * 2 + width;
		   float spacePerChar = spaceForLetters / (charsToPrint - 1.0f);

		   AffineTransform transform = g.getTransform();

		   StringBuffer finalString = new StringBuffer();

		   for ( int i = 0; i < charsToPrint; i++ ) {
		     double randomValue = Math.random();
		     int randomIndex = (int) Math.round(randomValue * (chars.length - 1));
		     char characterToShow = chars[randomIndex];
		     finalString.append(characterToShow);

		     // this is a separate canvas used for the character so that
		     // we can rotate it independently
		     int charImageWidth = maxAdvance * 2;
		     int charImageHeight = fontHeight * 2;
		     int charWidth = fontMetrics.charWidth(characterToShow);
		     int charDim = Math.max(maxAdvance, fontHeight);
		     int halfCharDim = (int) (charDim / 2);

		     BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
		     Graphics2D charGraphics = charImage.createGraphics();
		     charGraphics.translate(halfCharDim, halfCharDim);
		     double angle = (Math.random() - 0.5) * rotationRange;
		     charGraphics.transform(AffineTransform.getRotateInstance(angle));
		     charGraphics.translate(-halfCharDim,-halfCharDim);
		     charGraphics.setColor(textColor);
		     charGraphics.setFont(textFont);

		     int charX = (int) (0.5 * charDim - 0.5 * charWidth);
		     charGraphics.drawString("" + characterToShow, charX, 
		                            (int) ((charDim - fontMetrics.getAscent()) 
		                                   / 2 + fontMetrics.getAscent()));

		     float x = horizMargin + spacePerChar * (i) - charDim / 2.0f;
		     int y = (int) ((height - charDim) / 2);
//		log.debug("x=" + x + " height=" + height + " charDim=" + charDim + " y=" + y + " advance=" + maxAdvance + " fontHeight=" + fontHeight + " ascent=" + fontMetrics.getAscent());
		     g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);

		     charGraphics.dispose();
		   }

		   log.debug("Captcha: " + finalString.toString());
		   
		   // let's do the border
		   g.setColor(borderColor);
		   g.drawRect(0, 0, width - 1, height - 1);

		   //Write the image as a jpg
		   Iterator iter = ImageIO.getImageWritersByFormatName(imageFormat);
		   if( iter.hasNext() ) {
		     ImageWriter writer = (ImageWriter)iter.next();
		     ImageWriteParam iwp = writer.getDefaultWriteParam();
		     if ( imageFormat.equalsIgnoreCase("jpg") || imageFormat.equalsIgnoreCase("jpeg") ) {
		       iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		       iwp.setCompressionQuality(imageQuality);
		     }
		     writer.setOutput(ImageIO.createImageOutputStream(response.getOutputStream()));
		     IIOImage imageIO = new IIOImage(bufferedImage, null, null);
		     writer.write(null, imageIO, iwp);
		   } else {
		     throw new RuntimeException("no encoder found for jsp");
		   }

		   // let's stick the final string in the session
		   request.getSession().removeAttribute("captcha");
		   request.getSession().setAttribute("captcha", finalString.toString());

		   g.dispose();
		 } catch (IOException ioe) {
		   throw new RuntimeException("Unable to build image" , ioe);
		 }

	}

  public static String paramString(HttpServletRequest request, String paramName, String defaultString) {
    return request.getParameter(paramName) != null ? request.getParameter(paramName) : defaultString;
  }

  public static int paramInt(HttpServletRequest request, String paramName, int defaultInt) {
    return request.getParameter(paramName) != null ? Integer.parseInt(request.getParameter(paramName)) : defaultInt;
  }

}
