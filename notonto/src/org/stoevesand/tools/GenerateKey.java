package org.stoevesand.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.jboss.resteasy.util.Base64;

public class GenerateKey {

	private static String algorithm = "DESede";
	private static SecretKey key = null;
	private static Cipher cipher = null;
	private static KeyStore ks = null;

	private static char[] pw = "1xholterdipolter".toCharArray();

	private static String store = "zs7OzgAAAAIAAAABAAAAAwAOYXV0b2xvZ2luYWxpYXMAAAEaO5OQy6ztAAVzcgAzY29tLnN1bi5jcnlwdG8ucHJvdmlkZXIuU2VhbGVkT2JqZWN0Rm9yS2V5UHJvdGVjdG9yzVfKWecwu1MCAAB4cgAZamF2YXguY3J5cHRvLlNlYWxlZE9iamVjdD42PabDt1RwAgAEWwANZW5jb2RlZFBhcmFtc3QAAltCWwAQZW5jcnlwdGVkQ29udGVudHEAfgACTAAJcGFyYW1zQWxndAASTGphdmEvbGFuZy9TdHJpbmc7TAAHc2VhbEFsZ3EAfgADeHB1cgACW0Ks8xf4BghU4AIAAHhwAAAADzANBAjz15kX+XCqVQIBFHVxAH4ABQAAASCqJ+x9+KTooMYGW75jgvCKPd2UuPuiVJwJYpYhKw8emsNCD0vtKonVlvmmkyAjrRB/M67ChA4BWUfUJweAcXD8WHsyDof6PXSBgD73i714SZwNiAcIpB373B73yHrFHJCm7e6YkmDeT8WCvZbniT52fWp11RbkzOR6xYmtlFxhP/3m2341N0JLClMusTh5k26OqTv53z0NSWiKCss47vh/H0vjenI+hBJRQsMWLFVXCHJ9YRrNuLY2LDV5ny2/gN4mCRU5ZVBd5xrVknyUeFBR0YgNOqET10WKGAPy4uTNhha0NZVje/4QODA5hk54Etzs2D4zm+wZReOok1Nds8HounooW+yQbyWJmt2kj1HvT9Fufbckhedw2Yq6hsbJzul0ABZQQkVXaXRoTUQ1QW5kVHJpcGxlREVTdAAWUEJFV2l0aE1ENUFuZFRyaXBsZURFU0o32Bpy1/UCogL409j+UtRUNNAx";
	//private static String store = "zs7OzgAAAAIAAAABAAAAAwAOYXV0b2xvZ2luYWxpYXMAAAEaO7mhQaztAAVzcgAzY29tLnN1bi5jcnlwdG8ucHJvdmlkZXIuU2VhbGVkT2JqZWN0Rm9yS2V5UHJvdGVjdG9yzVfKWecwu1MCAAB4cgAZamF2YXguY3J5cHRvLlNlYWxlZE9iamVjdD42PabDt1RwAgAEWwANZW5jb2RlZFBhcmFtc3QAAltCWwAQZW5jcnlwdGVkQ29udGVudHEAfgACTAAJcGFyYW1zQWxndAASTGphdmEvbGFuZy9TdHJpbmc7TAAHc2VhbEFsZ3EAfgADeHB1cgACW0Ks8xf4BghU4AIAAHhwAAAADzANBAj572Jz+Qy1QgIBFHVxAH4ABQAAASDxyD2Ya+rvK5aHM7OnjFF/BHff7Q8cq9hi5i4iNZLZ5LGksRn9a2kRTmSTGcZJmkpWjuRXlaQ6K6Iz2qKQxyDddQfLu7QVFVWgsRzv9a9fBj7oP37Tk5w0Byxk6jaYB4C40RlUBjlAabGsrzmPrJrfDWUMouwMAa4JUdfmz5rG6a7f1NWEp5iaTynyDkLh3gKa7AAb4Xu/76LM2o+cVnooXD9kjpt+Kx2W1SXm5qxbgm3ylRiF73Etl1l1zWt61D+mfmB6eQs81MiMWRRyQKLjkh7AXtlzQuJt1xrvBchSTlf0DsfoyzqMsV0Fh3Z1WTQF52pi1bmm7dr6oDrXn7RObTzeNnj5gT+D/n+bdJ90p31vq2Uy6LJn+u9PbZXIh650ABZQQkVXaXRoTUQ1QW5kVHJpcGxlREVTdAAWUEJFV2l0aE1ENUFuZFRyaXBsZURFUwfZJiIcJ6HZXQLU4z2++9poSk5Y";

	public static void main(String[] args) throws Exception {
		// generateKS();
		loadKS();
		useKS();
	}

	public static void generateKS() throws Exception {

		KeyGenerator kg = KeyGenerator.getInstance(algorithm);
		kg.init(new SecureRandom());
		key = kg.generateKey();

		ks = KeyStore.getInstance("JCEKS");

		// get user password and file input stream

		InputStream is = null;
		ks.load(is, pw);

		KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(key);
		ks.setEntry("autologinAlias", skEntry, new KeyStore.PasswordProtection(pw));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ks.store(baos, pw);

		byte[] bstore = Base64.encodeBytesToBytes(baos.toByteArray());

		System.out.println("KS: " + new String(bstore));
	}

	public static void loadKS() throws Exception {
		ks = KeyStore.getInstance("JCEKS");

		byte[] bstore = Base64.decode(store.getBytes());

		ByteArrayInputStream is = new ByteArrayInputStream(bstore);
		ks.load(is, pw);

		KeyStore.SecretKeyEntry skEntry = (KeyStore.SecretKeyEntry) ks.getEntry("autologinAlias", new KeyStore.PasswordProtection(pw));
		key = skEntry.getSecretKey();
	}

	public static void useKS() throws Exception {

		String input = "jantest1234";
		System.out.println("Entered: " + input);

		String coded = encrypt(input);
		
		System.out.println("Recovered from "+coded+" : " + decrypt(coded));
	}

	private static String encrypt(String input) throws Exception {
		cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] inputBytes = input.getBytes();
		byte[] code = cipher.doFinal(inputBytes);
		
		return Base64.encodeBytes(code);
	}

	private static String decrypt(String input) throws Exception {
		byte[] encryptionBytes = Base64.decode(input.getBytes());
		cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] recoveredBytes = cipher.doFinal(encryptionBytes);
		String recovered = new String(recoveredBytes);
		return recovered;
	}
}
