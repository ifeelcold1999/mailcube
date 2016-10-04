package com.luffylu.mailcube.web.controller;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.luffylu.mailcube.service.SimpleMailSender;

@Controller
@RequestMapping("/api/cube")
public class CubeController {
	
	private static Logger logger = LoggerFactory.getLogger(CubeController.class);
	
	@RequestMapping("/test")
	@ResponseBody
	public JSONObject atest(){
		JSONObject result = new JSONObject();
		
		result.put("msg", "hi");
		
		return result;
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
