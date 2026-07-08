package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/**
 * Service for sending emails asynchronously.
 */
@Service
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";
    private static final String ORDER = "order";
    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;
    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;

    public MailService(
        JHipsterProperties jHipsterProperties,
        JavaMailSender javaMailSender,
        MessageSource messageSource,
        SpringTemplateEngine templateEngine
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        sendEmailSync(to, subject, content, isMultipart, isHtml);
    }

    private void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        LOG.debug(
            "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart,
            isHtml,
            to,
            subject,
            content
        );

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            LOG.debug("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            LOG.warn("Email could not be sent to user '{}'", to, e);
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            LOG.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey() != null ? user.getLangKey() : "en");
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());

        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);

        sendEmailSync(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendBookingNotificationEmail(
        String to,
        String customerName,
        String invoiceCode,
        String totalAmount,
        String paymentMethod,
        String movieTitle,
        String roomName,
        String showtimeText,
        List<String> ticketLines
    ) {
        if (to == null || to.isBlank()) {
            LOG.debug("Email doesn't exist for booking notification '{}'", invoiceCode);
            return;
        }

        LOG.info("Preparing booking notification email for invoice '{}' to '{}'", invoiceCode, to);
        Locale locale = Locale.forLanguageTag("vi");
        Context context = new Context(locale);
        context.setVariable("customerName", customerName);
        context.setVariable("invoiceCode", invoiceCode);
        context.setVariable("totalAmount", totalAmount);
        context.setVariable("paymentMethod", paymentMethod);
        context.setVariable("movieTitle", movieTitle);
        context.setVariable("roomName", roomName);
        context.setVariable("showtimeText", showtimeText);
        context.setVariable("ticketLines", ticketLines);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());

        String content = templateEngine.process("mail/bookingNotificationEmail", context);
        String subject = "Thong bao dat ve thanh cong - CinemaTick " + invoiceCode;
        sendEmailSync(to, subject, content, false, true);
        LOG.info("Booking notification email queued/sent for invoice '{}' to '{}'", invoiceCode, to);
    }

    @Async
    public void sendActivationEmail(User user) {
        LOG.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        LOG.debug("Sending creation/welcome email to '{}'", user.getEmail());
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return;
        }
        try {
            sendEmailFromTemplate(user, "mail/creationEmail", "email.creation.title");
        } catch (Exception e) {
            LOG.error("Failed to send creation email to '{}'", user.getEmail(), e);
        }
    }

    @Async
    public void sendPasswordResetMail(User user) {
        LOG.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title");
    }
}
