package org.stoevesand.util;

import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class CryptUtils {

	final private transient static String password = "naHkJKkjKHHkhjhkjhiIuhsdfssdfAgs";
	final private transient byte[] salt = { (byte) 0xc8, (byte) 0xb9, (byte) 0x23, (byte) 0x63, (byte) 0xc9, (byte) 0xc9, (byte) 0xc9, (byte) 0xc9 };
	final int iterations = 3;

	protected CryptUtils() {
		// java.security.Security.addProvider(new
		// com.sun.crypto.provider.SunJCE());
		// // implizit bereits erledigt!
	}

	/** instance */
	private static CryptUtils instance;

	/**
	 * Singleton Factory
	 * 
	 * @return instance
	 */
	public static CryptUtils getInstance() {
		if (instance == null) {
			instance = new CryptUtils();
			instance.init(password.toCharArray(), instance.salt, instance.iterations);
		}
		return instance;

	}

	/** Notwendige Instanczen */
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	private sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
	private sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

	/** Verwendete Zeichendecodierung */
	private String charset = "UTF16";

	/**
	 * Initialisiert den Verschlüsselungsmechanismus
	 * 
	 * @param pass
	 *            char[]
	 * @param salt
	 *            byte[]
	 * @param iterations
	 *            int
	 * @throws SecurityException
	 */
	public void init(final char[] pass, final byte[] salt, final int iterations) throws SecurityException {
		try {
			final PBEParameterSpec ps = new PBEParameterSpec(salt, 20);
			final SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			final SecretKey k = kf.generateSecret(new PBEKeySpec(pass));
			encryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, k, ps);
			decryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
			decryptCipher.init(Cipher.DECRYPT_MODE, k, ps);
		} catch (Exception e) {
			throw new SecurityException("Could not initialize CryptoLibrary: " + e.getMessage());
		}
	}

	/**
	 * Verschlüsselt eine Zeichenkette
	 * 
	 * @param str
	 *            Description of the Parameter
	 * @return String the encrypted string.
	 * @exception SecurityException
	 *                Description of the Exception
	 */
	public synchronized String encrypt(String str) throws SecurityException {
		try {
			byte[] b = str.getBytes(this.charset);
			byte[] enc = encryptCipher.doFinal(b);
			return encoder.encode(enc);
		} catch (Exception e) {
			throw new SecurityException("Could not encrypt: " + e.getMessage());
		}

	}

	/**
	 * Entschlüsselt eine Zeichenkette, welche mit der Methode encrypt
	 * verschlüsselt wurde.
	 * 
	 * @param str
	 *            Description of the Parameter
	 * @return String the encrypted string.
	 * @exception SecurityException
	 *                Description of the Exception
	 */
	public synchronized String decrypt(String str) throws SecurityException {
		try {
			byte[] dec = decoder.decodeBuffer(str);
			byte[] b = decryptCipher.doFinal(dec);
			return new String(b, this.charset);
		} catch (Exception e) {
			throw new SecurityException("Could not decrypt: " + e.getMessage());
		}
	}

	public static void main(final String[] ignored) {
		CryptUtils cu = CryptUtils.getInstance();

		final String user = "jan@stoevesand.org";
		final String pass = "jan1234";

		final String eu = cu.encrypt(user);
		final String ep = cu.encrypt(pass);
		final String e1 = cu.encrypt(user + "###" + pass);

		System.out.println("Verschlüsselt :" + eu);
		System.out.println("Verschlüsselt :" + ep);
		System.out.println("Verschlüsselt :" + e1);
		final String decrypted = cu.decrypt(e1);
		System.out.println("Entschlüsselt :" + decrypted);
	}

	private static final char[] symbols;

	static {
		StringBuilder tmp = new StringBuilder();
		for (char ch = '0'; ch <= '9'; ++ch)
			tmp.append(ch);
		for (char ch = 'a'; ch <= 'z'; ++ch)
			tmp.append(ch);
		symbols = tmp.toString().toCharArray();
	}

	public static String getRandomString(int len) {
		final Random random = new Random();
		char[] buf = new char[len];

		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}

}
