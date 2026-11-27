package com.example.ny.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Email đã được xác minh trên Brevo
    private final String FROM_EMAIL = "daod1068@gmail.com";

    // Phương thức mới để gửi email có file đính kèm
    public void sendEmailWithAttachment(String to, String subject, String body, MultipartFile attachment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // SỬA LỖI Ở ĐÂY: Phải dùng email đã xác minh
            helper.setFrom(FROM_EMAIL);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            if (attachment != null && !attachment.isEmpty()) {
                helper.addAttachment(
                        attachment.getOriginalFilename(),
                        new ByteArrayResource(attachment.getBytes())
                );
            }

            mailSender.send(message);
            System.out.println("Mail sent successfully!");

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // SỬA LỖI Ở ĐÂY: Thêm dòng setFrom
            message.setFrom(FROM_EMAIL);

            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("Simple Mail sent successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendNewLetterNotification(String title, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL); // Bắt buộc phải có để Brevo không chặn gửi
            message.setTo("tduongxinhgai3@gmail.com");
            message.setSubject("💌 Bạn lớn vừa gửi cho cậu một bức thư mới nè!");
            message.setText(
                    "Chào Thùy Dương (bạn nhỏ) 💕,\n\n" +
                            "Bạn lớn vừa viết một bức thư mới trên web dành riêng cho cậu:\n" +
                            "📌 Tiêu đề: " + title + "\n\n" +
                            "Vào web đọc trọn vẹn bức thư và nghe nhạc nhé!\n" +
                            "Link web: https://initial-barbee-duc18102007-hippeach-f2b51271.koyeb.app/"
            );
            mailSender.send(message);
            System.out.println("Đã gửi email thông báo thư mới cho Thùy Dương!");
        } catch (Exception e) {
            System.err.println("Lỗi gửi email thông báo cho Thùy Dương: " + e.getMessage());
        }
    }
}