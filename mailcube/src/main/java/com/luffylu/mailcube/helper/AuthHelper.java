package com.luffylu.mailcube.helper;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import sun.misc.BASE64Decoder;

@Component
public class AuthHelper {
	private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqZ1aALQs7z5iV938D+zgCtHSB" + "\r"
			+ "hRxS79lE5Q/2bYW1EXSeRFZQ1HjCSXnNm+zpPxkbTA+kqppMOAgOcdu/2mObV5Yd" + "\r"
			+ "WE+cAHbOvvZlT+mWenXPi/pgHDX1K7PT3Og/3IUN8pyAXwvJQpOnquLIPGHka+pw" + "\r"
			+ "XGrugqLxc77vuFnFNQIDAQAB" + "\r";

	/**
	 * 公钥
	 */
	private PublicKey publicKey;
	
	@PostConstruct
	private void init() {
		try {
			publicKey = this.loadPublicKey(PUBLIC_KEY);
		} catch (Exception e) {
			throw new RuntimeException("load key fail[" + e.getMessage() + "]");
		}
		
	}

	/**
	 * 校验签名
	 * @param plain
	 * @param sign
	 * @return
	 * @throws Exception
	 */
	public boolean verifySign(String plain, String sign) throws Exception {
		if (plain == null || sign == null || publicKey == null) {
			return false;
		}
		Signature signetcheck = Signature.getInstance("MD5withRSA");
		signetcheck.initVerify(publicKey);
		signetcheck.update(plain.getBytes("ISO-8859-1"));
		return signetcheck.verify(Hex.decodeHex(sign.toCharArray()));
	}

	/**
	 * 加载公钥
	 * 
	 * @param publicKeyStr
	 *            公钥数据字符串
	 * @throws Exception
	 *             加载公钥时产生的异常
	 */
	private PublicKey loadPublicKey(String publicKeyStr) throws Exception {
		try {
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			return keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法");
		} catch (IOException e) {
			throw new Exception("公钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("公钥数据为空");
		}
	}
	
}
