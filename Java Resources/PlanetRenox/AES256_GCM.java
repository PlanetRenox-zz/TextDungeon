package PlanetRenox;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
// import java.security.Provider;
// import java.security.Security;

public class AES256_GCM { 
	
	public static synchronized byte[] encrypt(String cleartext, String clearPass) {

		byte[] shaltAndBaltAndNonceAndCipher = null;
		
		try 
		{
			final byte[] clearTextbytes = cleartext.getBytes("UTF-8");
			final SecureRandom random = SecureRandom.getInstanceStrong();
			/************************************************************************/
			final byte[] derivedShaltAndBaltAndHashByte = BCryptSHA256.initHash(clearPass);
			final SecretKeySpec key = new SecretKeySpec(derivedShaltAndBaltAndHashByte, 32+29, 32, "AES");
			/* 
			 * Other Java runtime environments may not necessarily contain these Sun providers, so applications.. 
			 * ..should not request an provider-specific implementation unless it is known that a particular provider will be available.
			 * Cipher AES_GCM = Cipher.getInstance("AES/GCM/NoPadding", "Sun, SunJSSE, SunJCE, SunRsaSign");
			 */
			final Cipher aesCipherInstance = Cipher.getInstance("AES/GCM/NoPadding");
			
			/************************************************************************/
	        byte[] nonce = new byte[12]; // 12 bytes always
	        random.nextBytes(nonce);     
	        /************************************************************************/
			aesCipherInstance.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
	        final byte[] encryptedTextBytes = aesCipherInstance.doFinal(clearTextbytes);
	        /************************************************************************/
	        
	        /* combining encryptedText and salts for transfer */
	        shaltAndBaltAndNonceAndCipher = new byte[nonce.length + encryptedTextBytes.length + 32 + 29]; 
	        System.arraycopy(derivedShaltAndBaltAndHashByte, 0, shaltAndBaltAndNonceAndCipher, 0, 32); // shalt
	        System.arraycopy(derivedShaltAndBaltAndHashByte, 32, shaltAndBaltAndNonceAndCipher, 32, 29); // balt
	        System.arraycopy(nonce, 0, shaltAndBaltAndNonceAndCipher, 32+29, 12); // nonce
	        System.arraycopy(encryptedTextBytes, 0, shaltAndBaltAndNonceAndCipher, 32+29+12, encryptedTextBytes.length); // cipher
		} 
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException |  InvalidAlgorithmParameterException e) 
		{
			e.printStackTrace();
		}
		
		return shaltAndBaltAndNonceAndCipher; 
		
	} // encrypt.
	
	public static synchronized String decrypt(byte[] shaltAndBaltAndNonceAndCipher, String clearPass)  {
		
		String decryptedText = null;
		
		try 
		{
			byte[] shaltAndBalt = new byte[32+29];
			System.arraycopy(shaltAndBaltAndNonceAndCipher, 0, shaltAndBalt, 0, 32);
			System.arraycopy(shaltAndBaltAndNonceAndCipher, 32, shaltAndBalt, 32, 29);
			final SecretKeySpec key = new SecretKeySpec(BCryptSHA256.initHashWithShalt(clearPass, shaltAndBalt), "AES");
			final Cipher aesCipherInstance = Cipher.getInstance("AES/GCM/NoPadding");
			final GCMParameterSpec params = new GCMParameterSpec(128, shaltAndBaltAndNonceAndCipher, 32+29, 12);
			aesCipherInstance.init(Cipher.DECRYPT_MODE, key, params);
			final byte[] decryptedTextByte = aesCipherInstance.doFinal(shaltAndBaltAndNonceAndCipher, 32+29+12, shaltAndBaltAndNonceAndCipher.length - (32+29+12));
			decryptedText = new String(decryptedTextByte, "UTF-8");
		} 
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) 
		{
			e.printStackTrace();
		}
		
		return decryptedText;
		
	} // decrypt.

} // AES256_GCM.

// https://soundcloud.com/kaiyko/so-why-dont-we-just-dance
