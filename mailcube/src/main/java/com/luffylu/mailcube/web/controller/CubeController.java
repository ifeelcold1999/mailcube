package com.luffylu.mailcube.web.controller;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luffylu.mailcube.helper.AuthHelper;
import com.luffylu.mailcube.service.SimpleMailSender;

@Controller
@RequestMapping("/api/cube")
public class CubeController {
	
	private static Logger logger = LoggerFactory.getLogger(CubeController.class);
	
	@Resource
	private AuthHelper authHelper;
	
	private static final String CER_DIR = "D:\\keys\\mailcube.cer";
	
	private String authMail = "";
	private long deadline = 0L;
	private String sign = "";
	
	boolean authenticated = false;
	
	@PostConstruct
	private void init() {
		try{
			List<String> cer = Files.readAllLines(Paths.get(CER_DIR));
			String hex = cer.get(0);
			sign = cer.get(1);
			if(authHelper.verifySign(hex, sign)){
				authenticated = true;
			}else{
				return;
			}
			String plain = new String(Hex.decodeHex(hex.toCharArray()));
			String[] plainArray = StringUtils.split(plain, "\\|");
			authMail = plainArray[0];
			deadline = Long.parseLong(plainArray[1]);
		}catch(Exception e){
			logger.error("auth fail", e);
		}
		
	}
	
	/**
	 * start mail sending task
	 * @return
	 */
	@RequestMapping(value = "/mail-task", method = RequestMethod.POST)
	@ResponseBody
	public void startMailTask(@RequestParam String email,
			@RequestParam String password,
			@RequestParam String subject,
			@RequestParam String receivers,
			@RequestParam(name = "mail_body") String mailBody,
			@RequestParam(name = "attach_dir") String attachDir,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		PrintWriter writer = response.getWriter();
		long current = System.currentTimeMillis();
		if(!authenticated || !StringUtils.equals(authMail, email) || current > deadline){
			writer.write("unauthenticated or authentication expired\n");
			writer.write("please contact ifeelcold1999@163.com for authentication\n");
			writer.flush();
			writer.close();
		}else{
			SimpleMailSender sender = new SimpleMailSender(email, password);
			
			String[] receiverArray = StringUtils.split(receivers, "\n");
			
			List<File> attachList = readAttach(attachDir);
			
			for(String to : receiverArray){
				String[] toArray = StringUtils.split(to, "\\|");
				try {
					sender.sendWithAttach(toArray[0], subject, toArray[1] + "\n" + mailBody, attachList);
					writer.write(toArray[0] + " done\n");
					Thread.sleep(1000);
				} catch (Exception e) {
					logger.error("sending [" + toArray[0] + "] fail", e);
				}
			}
			
			writer.write("all task finished\n");
			writer.flush();
			writer.close();
		}
	}
	
	/**
	 * 读取指定路径下所有文件
	 * @param attachDir
	 * @return
	 */
	private static List<File> readAttach(String attachDir){
		File file = new File(attachDir);
		File[] tempList = file.listFiles();
		int count = tempList.length;
		if(count == 0){
			return null;
		}
		return Arrays.asList(tempList);
	}
}
