package com.btr.platform.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ConfigService configService;

    public EmailService(ConfigService configService) {
        this.configService = configService;
    }

    public boolean send(String to, String subject, String html) {
        Map<String, String> mail = configService.getRawGroup("mail");
        if (!Boolean.parseBoolean(mail.getOrDefault("enabled", "false"))) {
            return false;
        }
        String host = mail.getOrDefault("host", "");
        if (host.isBlank()) {
            return false;
        }
        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host);
            sender.setPort(Integer.parseInt(mail.getOrDefault("port", "465")));
            sender.setUsername(mail.getOrDefault("username", ""));
            sender.setPassword(mail.getOrDefault("password", ""));
            sender.setDefaultEncoding(StandardCharsets.UTF_8.name());

            Properties props = sender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", mail.getOrDefault("ssl", "true"));
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            String username = mail.getOrDefault("username", "");
            String from = mail.getOrDefault("from", "BTR 赛事平台");
            if (!username.isBlank()) {
                helper.setFrom(username, from);
            } else {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(message);
            return true;
        } catch (Exception ex) {
            log.warn("Send mail failed: {}", ex.getMessage());
            return false;
        }
    }

    public boolean sendTest(String to) {
        return send(to, "BTR 邮件配置测试",
                "<p>这是一封测试邮件。</p><p>如果你收到它，说明 BTR 的 SMTP 配置已生效。</p>");
    }
}
