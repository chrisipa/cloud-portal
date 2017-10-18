package de.papke.cloud.portal.mail;

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

import de.papke.cloud.portal.velocity.VelocityService;

/**
 * Service class for sending emails.
 *
 * @author Christoph Papke (info@papke.it)
 */
@Service
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    @Value("${mail.protocol}")
    private String protocol;

    @Value("${mail.host}")
    private String host;

    @Value("${mail.port}")
    private int port;

    @Value("${mail.smtp.auth}")
    private boolean auth;

    @Value("${mail.smtp.starttls.enable}")
    private boolean starttls;

    @Value("${mail.from}")
    private String from;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.template.path}")
    private String templatePath;

    @Value("${mail.subject}")
    private String subject;

    @Value("${mail.send}")
    private boolean send;

    private JavaMailSenderImpl mailSender;

    @Autowired
    private VelocityService velocityService;

    /**
     * Method for initializing the mail service.
     */
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

        // create java mail properties
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", auth);
        mailProperties.put("mail.smtp.starttls.enable", starttls);

        // set java mail properties
        mailSender.setJavaMailProperties(mailProperties);
    }

    /**
     * Method for sending an HTML mail.
     *
     * @param to - mail recipient
     * @param variableMap - variable map for substitution
     */
    @SuppressWarnings("rawtypes")
	public void send(String to, Map variableMap) {

        // logging
        LOG.info("Sending mail from '{}' to address '{}' with subject '{}'", from, to, subject);

        // create mime message preparator
        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            @SuppressWarnings("unchecked")
			@Override
            public void prepare(MimeMessage mimeMessage) throws Exception {

                // get mail text by substituting variables in mail template with velocity
                String text = velocityService.evaluate(templatePath, variableMap);

                // create message helper
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                messageHelper.setFrom(from);
                messageHelper.setTo(to);
                messageHelper.setSubject(subject);
                messageHelper.setText(text, true);
            }
        };

        // send HTML mail
        if (send) {
            mailSender.send(preparator);
        }
    }
}