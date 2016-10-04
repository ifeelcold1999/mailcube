package com.luffylu.mailcube.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class SimpleMailSender {

	/** 发送邮件的props文件 */
	private final transient Properties props = System.getProperties();
	
	/** 邮件服务器登陆验证器 */
	private transient MailAuthenticator authenticator;
	
	/** 邮箱session */
	private transient Session session;
	
	/**
	 * 实例化邮件发送器
	 * @param smtpHostName
	 * @param username
	 * @param password
	 */
	public SimpleMailSender(final String smtpHostName, final String username, final String password){
		this.init(username, password, smtpHostName);
	}
	
	/**
	 * 实例化邮件发送器
	 * @param username
	 * @param password
	 */
	public SimpleMailSender(final String username, final String password){
		// 通过邮箱地址解析出smtp服务器，对大多数邮箱都适用
		final String smtpHostName = "smtp." + username.split("@")[1];
		this.init(username, password, smtpHostName);
	}
	
	/**
	 * 发送纯文本邮件
	 * @param recipient
	 * @param subject
	 * @param content
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void send(String recipient, String subject, Object content) throws AddressException, MessagingException {
		// 创建mime类型邮件
		final MimeMessage message = new MimeMessage(session);
		// set from
		message.setFrom(new InternetAddress(authenticator.getUsername()));
		// set recipient
		message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
		// set subject
		message.setSubject(subject);
		// set content
		message.setContent(content.toString(), "text/html;charset=utf-8");
		
		// send
		Transport.send(message);
	}
	
	/**
	 * 带附件的邮件
	 * @param recipient
	 * @param subject
	 * @param content
	 * @param attachList
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public void sendWithAttach(String recipient, String subject, Object content, List<File> attachList) throws AddressException, MessagingException, UnsupportedEncodingException {
		// init mime
		final MimeMessage message = new MimeMessage(session);
		// set from
		message.setFrom(new InternetAddress(authenticator.getUsername()));
		// set recipient
		message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
		// set subject
		message.setSubject(subject);
		
		// set content
		Multipart multipart = new MimeMultipart();
		
		// content
		BodyPart contentPart = new MimeBodyPart();
		contentPart.setContent(content.toString(), "text/html;charset=utf-8");
		multipart.addBodyPart(contentPart);
		
		// attach
		for(File attach : attachList){
			BodyPart attachBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(attach);
			attachBodyPart.setDataHandler(new DataHandler(source));
			
			attachBodyPart.setFileName(MimeUtility.encodeWord(attach.getName()));
			multipart.addBodyPart(attachBodyPart);
		}
		// set multipart
		message.setContent(multipart);
		// save
		message.saveChanges();
		
		// send
		Transport.send(message);
	}
	
	/**
	 * 初始化session和auth
	 * @param username
	 * @param password
	 * @param smtpHostName
	 */
	private void init(String username, String password,String smtpHostName){
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", smtpHostName);
		
		authenticator = new MailAuthenticator(username, password);
		
		session = Session.getInstance(props, authenticator);
	}
}
