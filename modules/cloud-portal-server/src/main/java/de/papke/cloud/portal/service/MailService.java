package de.papke.cloud.portal.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

	@Value("${MAIL_PROTOCOL}")
	private String protocol;

	@Value("${MAIL_HOST}")
	private String host;

	@Value("${MAIL_PORT}")
	private int port;

	@Value("${MAIL_SMTP_AUTH}")
	private boolean auth;

	@Value("${MAIL_SMTP_START_TLS_ENABLE}")
	private boolean starttls;
	
	@Value("${MAIL_SMTP_TIMEOUT}")
	private int timeout;

	@Value("${MAIL_FROM}")
	private String from;
	
	@Value("${MAIL_CC}")
	private String cc;

	@Value("${MAIL_USERNAME}")
	private String username;

	@Value("${MAIL_PASSWORD}")
	private String password;

	@Value("${MAIL_SEND}")
	private boolean send;

	private JavaMailSenderImpl mailSender;

	@Autowired
	private VelocityService velocityService;

	@PostConstruct
	public void init() {

		// create new java mail sender
		mailSender = new JavaMailSenderImpl();

		// set mail sender configuration
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setProtocol(protocol);
		mailSender.setUsername(username);
		mailSender.setPassword(password);
		
		// set mail encoding
		mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

		// create java mail properties
		Properties mailProperties = new Properties();
		mailProperties.put("mail.smtp.auth", auth);
		mailProperties.put("mail.smtp.starttls.enable", starttls);
		mailProperties.put("mail.smtp.timeout", timeout);    
		mailProperties.put("mail.smtp.connectiontimeout", timeout);   

		// set java mail properties
		mailSender.setJavaMailProperties(mailProperties);
	}

	@SuppressWarnings("rawtypes")
	public void send(String to, String subject, String templatePath, File attachment, Map variableMap) {

		// create attachment list
		List<File> attachmentList = new ArrayList<>();
		if (attachment != null) {
			attachmentList.add(attachment);
		}
		
		// send mail
		send(to, subject, templatePath, attachmentList, variableMap);
	}
	
	@SuppressWarnings("rawtypes")
	public void send(String to, String subject, String templatePath, List<File> attachmentList, Map variableMap) {

		// logging
		LOG.info("Sending mail from '{}' to address '{}' and cc addresses '{}' with subject '{}'", from, to, cc, subject);

		// really send mail
		if (send) {

			// create mime message preparator
			MimeMessagePreparator preparator = new MimeMessagePreparator() {

				@SuppressWarnings("unchecked")
				@Override
				public void prepare(MimeMessage mimeMessage) throws Exception {

					// get mail text by substituting variables in mail template with velocity
					String text = velocityService.evaluate(templatePath, variableMap);

					// create message helper
					MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
					messageHelper.setFrom(from);
					messageHelper.setTo(to);
					messageHelper.setCc(cc.split(","));
					messageHelper.setSubject(subject);
					messageHelper.setText(text, true);
					
					// add attachments if available
					for (File file : attachmentList) {
						messageHelper.addAttachment(file.getName(), file);
					}
				}
			};

			// send HTML mail
			mailSender.send(preparator);
		}
	}
}