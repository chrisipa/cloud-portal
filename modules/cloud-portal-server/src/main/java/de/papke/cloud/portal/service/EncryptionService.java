package de.papke.cloud.portal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.crypto.AES;

@Service
public class EncryptionService {
	
	@Value("${encryptor.secret}")
	private String secret;
	
	public String encrypt(String text) {
		return AES.encrypt(text, secret);
	}
	
	public String decrypt(String text) {
		return AES.decrypt(text, secret);
	}
}
