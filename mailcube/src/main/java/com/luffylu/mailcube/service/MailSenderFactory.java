package com.luffylu.mailcube.service;

public class MailSenderFactory {

	private static SimpleMailSender mailSender = null;
	
	public static SimpleMailSender getSender(String email, String password){
		if(mailSender == null){
			mailSender = new SimpleMailSender(email, password);
		}
		return mailSender;
	}
}
