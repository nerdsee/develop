package org.stoevesand.brain.auth;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.jboss.logging.Logger;
import org.jboss.resteasy.util.Base64;

public class Security {

	private static Logger log = Logger.getLogger(Security.class);

	private static String algorithm = "DESede";
	private SecretKey key = null;
	private Cipher cipher = null;
	private KeyStore ks = null;

	private static char[] pw = "1xholterdipolter".toCharArray();

	private static String store = "zs7OzgAAAAIAAAABAAAAAwAOYXV0b2xvZ2luYWxpYXMAAAEaO5OQy6ztAAVzcgAzY29tLnN1bi5jcnlwdG8ucHJvdmlkZXIuU2VhbGVkT2JqZWN0Rm9yS2V5UHJvdGVjdG9yzVfKWecwu1MCAAB4cgAZamF2YXguY3J5cHRvLlNlYWxlZE9iamVjdD42PabDt1RwAgAEWwANZW5jb2RlZFBhcmFtc3QAAltCWwAQZW5jcnlwdGVkQ29udGVudHEAfgACTAAJcGFyYW1zQWxndAASTGphdmEvbGFuZy9TdHJpbmc7TAAHc2VhbEFsZ3EAfgADeHB1cgACW0Ks8xf4BghU4AIAAHhwAAAADzANBAjz15kX+XCqVQIBFHVxAH4ABQAAASCqJ+x9+KTooMYGW75jgvCKPd2UuPuiVJwJYpYhKw8emsNCD0vtKonVlvmmkyAjrRB/M67ChA4BWUfUJweAcXD8WHsyDof6PXSBgD73i714SZwNiAcIpB373B73yHrFHJCm7e6YkmDeT8WCvZbniT52fWp11RbkzOR6xYmtlFxhP/3m2341N0JLClMusTh5k26OqTv53z0NSWiKCss47vh/H0vjenI+hBJRQsMWLFVXCHJ9YRrNuLY2LDV5ny2/gN4mCRU5ZVBd5xrVknyUeFBR0YgNOqET10WKGAPy4uTNhha0NZVje/4QODA5hk54Etzs2D4zm+wZReOok1Nds8HounooW+yQbyWJmt2kj1HvT9Fufbckhedw2Yq6hsbJzul0ABZQQkVXaXRoTUQ1QW5kVHJpcGxlREVTdAAWUEJFV2l0aE1ENUFuZFRyaXBsZURFU0o32Bpy1/UCogL409j+UtRUNNAx";

	public Security() {
		loadKS();
	}

	private void loadKS() {
		try {
			ks = KeyStore.getInstance("JCEKS");

			byte[] bstore = Base64.decode(store.getBytes());

			ByteArrayInputStream is = new ByteArrayInputStream(bstore);
			ks.load(is, pw);

			KeyStore.SecretKeyEntry skEntry = (KeyStore.SecretKeyEntry) ks.getEntry("autologinAlias", new KeyStore.PasswordProtection(pw));
			key = skEntry.getSecretKey();

			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public String encrypt(String input) throws Exception {
		byte[] inputBytes = input.getBytes();
		byte[] code = cipher.doFinal(inputBytes);

		return new String(Base64.encodeBytes(code));
	}

	public String decrypt(String input) throws Exception {
		byte[] encryptionBytes = Base64.decode(input.getBytes());

		byte[] recoveredBytes = cipher.doFinal(encryptionBytes);
		String recovered = new String(recoveredBytes);
		return recovered;
	}
}
