// PlanetRenox.com | github.com/PlanetRenox   
// Contact: planetrenox@pm.me 

package PlanetRenox;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import PlanetRenox.OfficialBCrypt;

public class BCryptSHA256 {
	
	// SHA-256(BCrypt(SHA-256)))
	public static synchronized byte[] initHash (String clearPass) {
		
		byte[] derivedShaltAndBaltAndHashByte = new byte[32+29+32];
		
		try 
		{
			byte[] derivedHashByte = new byte[32];
			final byte[] clearPassByte = clearPass.getBytes("UTF-8");
			
			/**********************************SALT*************************************/
			final SecureRandom random = SecureRandom.getInstanceStrong();
			byte[] shalt = new byte[32];
			random.nextBytes(shalt);
			
			/*********************************SHA-256***********************************/
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(clearPassByte);
			md.update(shalt);
			derivedHashByte = md.digest();
			
			/*******************************BCrypt/SHA-256******************************/
			String balt = OfficialBCrypt.gensalt(13);
			byte[] baltByte = balt.getBytes("UTF-8");
			derivedHashByte = md.digest(OfficialBCrypt.hashpw(String.format("%064x", new BigInteger(1, derivedHashByte)), balt).getBytes("UTF-8"));
			System.arraycopy(shalt, 0, derivedShaltAndBaltAndHashByte, 0, 32);
	        System.arraycopy(baltByte, 0, derivedShaltAndBaltAndHashByte, 32, 29);
	        System.arraycopy(derivedHashByte, 0, derivedShaltAndBaltAndHashByte, 32+29, 32);
		} 
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		return derivedShaltAndBaltAndHashByte;
		
	} // initHash.
	
	public static synchronized byte[] initHashWithShalt (String clearPass, byte[] shaltAndBalt) {
		
		byte[] derivedHashByte = new byte[32];
		
		try 
		{
			//////////////////
			byte[] shalt = new byte[32];
			byte[] balt = new byte[29];
			System.arraycopy(shaltAndBalt, 0, shalt, 0, 32);
	        System.arraycopy(shaltAndBalt, 32, balt, 0, 29);
			String baltStr = new String(balt, "UTF-8");
			final byte[] clearPassByte = clearPass.getBytes("UTF-8");
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(clearPassByte);
			md.update(shalt);
			derivedHashByte = md.digest();
			
			/*******************************BCrypt/SHA-256******************************/
			derivedHashByte = md.digest(OfficialBCrypt.hashpw(String.format("%064x", new BigInteger(1, derivedHashByte)), baltStr).getBytes("UTF-8"));
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			e.printStackTrace();	
		}
		
		return derivedHashByte;
	}
	
	public static synchronized Boolean match (String clearPass, String derivedHashStr, byte[] shalt) {
		
		byte[] derivedHashByte = new byte[32];
		
		try 
		{
			final byte[] clearPassByte = clearPass.getBytes("UTF-8");
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(clearPassByte);
			md.update(shalt);
			derivedHashByte = md.digest();
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			e.printStackTrace();	
		}
		
		return OfficialBCrypt.checkpw(String.format("%064x", new BigInteger(1, derivedHashByte)), derivedHashStr);
	} // match.

} // BcryptSha512.
