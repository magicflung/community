package com.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author flunggg
 * @date 2020/7/21 9:16
 * @Email: chaste86@163.com
 */
// 加上@Component，表示通用的bean
@Component
public class MailClient {

    private final static Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender javaMailSender;

    // 通过@Value获取配置文件中参数的值
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发丝邮件
     * @param to
     * @param subject
     * @param content
     */
    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            // MimeMessage是空的，还需要添加内容，借助MimeMessageHelper
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            // 设置
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            // 默认为普通文件，后面加个true为html
            mimeMessageHelper.setText(content, true);
            // 发送
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.getMessage());
        }
    }
}
