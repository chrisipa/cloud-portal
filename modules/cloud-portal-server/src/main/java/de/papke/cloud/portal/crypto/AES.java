package de.papke.cloud.portal.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AES {
	
	private static final Logger LOG = LoggerFactory.getLogger(AES.class);
	private static final String ECB_PKCS5_PADDING = "AES/ECB/PKCS5Padding";
	private static final String ECB_PKCS5PADDING = "AES/ECB/PKCS5PADDING";
	 
    private AES() {}
 
    public static SecretKeySpec getSecretKey(String myKey) {
    	
    	SecretKeySpec secretKey = null;
    	
        try {
        	byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
        	LOG.error(e.getMessage(), e);
        }
        
        return secretKey;
    }
 
    public static String encrypt(String strToEncrypt, String secret) {
        
    	try {
            SecretKeySpec secretKey = getSecretKey(secret);
            Cipher cipher = Cipher.getInstance(ECB_PKCS5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e) {
        	LOG.error(e.getMessage(), e);
        }
        return null;
    }
 
    public static String decrypt(String strToDecrypt, String secret) {
        
    	try {
    		SecretKeySpec secretKey = getSecretKey(secret);
            Cipher cipher = Cipher.getInstance(ECB_PKCS5PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e) {
        	LOG.error(e.getMessage(), e);
        }
        
        return null;
    }
}
