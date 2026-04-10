package com.knockknock.backend.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private static final String FROM_EMAIL = "knock.knock.management@gmail.com";

	private final JavaMailSender mailSender;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * Account-related welcome email for new users.
	 */
	@Async
	public void sendWelcomeEmail(String toEmail, String fullName, String role, String condoCode) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(FROM_EMAIL);
		message.setTo(toEmail);
		message.setSubject("Welcome to Knock Knock!");

		StringBuilder body = new StringBuilder();
		body.append("Hi ").append(fullName).append(",\n\n")
			.append("Welcome to Knock Knock!\n")
			.append("Your account has been created with the role: ").append(role).append(".\n");

		if (condoCode != null && !condoCode.isBlank()) {
			body.append("\nYour condominium code is: ").append(condoCode).append("\n");
		}

		body.append("\nYou can now log in and start managing your visits.\n\n")
			.append("Best regards,\n")
			.append("Knock Knock Team");

		message.setText(body.toString());

		mailSender.send(message);
	}

	/**
	 * Visit-created email (no QR yet) sent when a visit is scheduled.
	 */
	@Async
	public void sendVisitCreatedEmail(String toEmail, String visitorName, String refNumber, String visitDate, String condoName) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(FROM_EMAIL);
		message.setTo(toEmail);
		message.setSubject("Your visit has been scheduled");

		String safeName = (visitorName != null && !visitorName.isBlank()) ? visitorName : "Guest";
		String safeCondo = (condoName != null && !condoName.isBlank()) ? condoName : "your condominium";
		String safeDate = (visitDate != null && !visitDate.isBlank()) ? visitDate : "your scheduled date";

		StringBuilder body = new StringBuilder();
		body.append("Hi ").append(safeName).append(",\n\n")
			.append("Your visit has been successfully scheduled.\n\n")
			.append("Reference Number: ").append(refNumber != null ? refNumber : "N/A").append("\n")
			.append("Condominium: ").append(safeCondo).append("\n")
			.append("Date of Visit: ").append(safeDate).append("\n\n")
			.append("You can generate your QR code anytime from the Knock Knock application. ")
			.append("Please keep your reference number handy when you arrive at the gate.\n\n")
			.append("Best regards,\n")
			.append("Knock Knock Team");

		message.setText(body.toString());
		mailSender.send(message);
	}

	@Async
    public void sendVisitConfirmationEmail(String toEmail, String visitorName, String refNumber, String qrUrl) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // Use true for multipart, which is required for inline images
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject("Your visit QR code is ready: " + refNumber);

            byte[] qrBytes = null;
            if (qrUrl != null && !qrUrl.isBlank()) {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<byte[]> response = restTemplate.getForEntity(qrUrl, byte[].class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        qrBytes = response.getBody();
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching QR: " + e.getMessage());
                }
            }

            // Build the HTML
            String qrContent = (qrBytes != null) 
                ? "<p>Please present the QR code attached when you arrive.</p><img src='cid:qrImage' style='width:200px;'/>"
                : "<p>Please keep your reference number handy at the gate.</p>";

            String html = String.format(
                "<html><body><h2>Visit QR Code</h2><p>Hi %s,</p>%s<p><strong>Ref:</strong> %s</p></body></html>",
                visitorName, qrContent, refNumber
            );

            // 1. SET TEXT FIRST
            helper.setText(html, true);

            // 2. ADD INLINE SECOND (Only if bytes exist)
            if (qrBytes != null) {
                // Adding a filename "qr.png" helps some email clients render properly
                helper.addInline("qrImage", new ByteArrayResource(qrBytes), "image/png");
            }

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Mail failure: " + e.getMessage());
        }
    }
}
