package com.annex.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;

import java.util.Properties;

@Configuration
public class EmailConfig {
    @Bean
    public TemplateEngine templateEngine(){
        return new TemplateEngine();
    }

    @Bean
    public JavaMailSenderImpl mailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost("localhost");
        mailSender.setPort(1025);
        mailSender.setUsername("annex.server11@gmail.com");
        mailSender.setPassword("hmfhmwlsenvlxrxw");

        Properties props = mailSender.getJavaMailProperties();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
