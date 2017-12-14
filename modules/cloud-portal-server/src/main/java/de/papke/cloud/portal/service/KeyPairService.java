package de.papke.cloud.portal.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

import de.papke.cloud.portal.constants.Constants;

@Service
public class KeyPairService {
	
	private static final Logger LOG = LoggerFactory.getLogger(KeyPairService.class); 

	private static final JSch JSCH = new JSch();
	private static final String FILE_PREFIX = "id_rsa";
	private static final String FILE_SUFFIX = ".pub";
	private static final String COMMENT = "keygen@cloud-portal";
	
	public List<File> createKeyPair() {

		List<File> keyFileList = new ArrayList<>(); 
		
		try{
			File privateKeyFile = File.createTempFile(FILE_PREFIX, Constants.CHAR_EMPTY);
			File publicKeyFile = new File(privateKeyFile.getAbsolutePath() + FILE_SUFFIX);
			
			KeyPair keyPair = KeyPair.genKeyPair(JSCH, KeyPair.RSA);
			keyPair.writePrivateKey(privateKeyFile.getAbsolutePath());
			keyPair.writePublicKey(publicKeyFile.getAbsolutePath(), COMMENT);
			keyPair.dispose();
			
			keyFileList.add(privateKeyFile);
			keyFileList.add(publicKeyFile);
		}
		catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		
		return keyFileList;
	}
}
