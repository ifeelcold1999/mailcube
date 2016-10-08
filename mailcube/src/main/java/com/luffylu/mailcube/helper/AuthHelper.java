package com.luffylu.mailcube.helper;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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

	private static final String PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKpnVoAtCzvPmJX3" + "\r"
			+ "fwP7OAK0dIGFHFLv2UTlD/ZthbURdJ5EVlDUeMJJec2b7Ok/GRtMD6Sqmkw4CA5x" + "\r"
			+ "27/aY5tXlh1YT5wAds6+9mVP6ZZ6dc+L+mAcNfUrs9Pc6D/chQ3ynIBfC8lCk6eq" + "\r"
			+ "4sg8YeRr6nBcau6CovFzvu+4WcU1AgMBAAECgYAFfSSlJk5JU+uHURyPRUVNmQBz" + "\r"
			+ "t5Ts+vrVpEc6WdbHuVM3Ud4x5lYpc/JvdAlSjgzB+1Y5qKEluVxREsvQRzmUFuwt" + "\r"
			+ "swqYmTBev16B/K4miSfHuGdXbsvv7Z8AKSVMahBFVxGdkg1ByuxhagP5vH4XMSfu" + "\r"
			+ "jRbXr4PqdRYU44jwFQJBANjIOu+5GaFvj8bkwQRCXHtakaS54aWvUCmf8rUyJ2cG" + "\r"
			+ "3IVFSQuZvMDFPm0s81I+eYOF2rSW7FuLALTA+Nim0qsCQQDJOzGxMUr6IsgoD4l4" + "\r"
			+ "3r3mklf0OGOGPwH0QvVjrx2CJKWu8/CqibcchnUT0zWCeVCP+O7JQDVI+d5vIZ7g" + "\r"
			+ "7sefAkAdqS/fMv1dyEzs0snSEl5jBl5dIo3MRLN8LoVf42/eueKcXYGEE9Husk47" + "\r"
			+ "U+Yq/59SrpLhGLFYSSmckba6tgCnAkEAgHnMhibEieL/C6Svzn3XnIg+o2wFDsbX" + "\r"
			+ "ho3hgd1h559iMMsKEreOMyYdRaUJet3dc64pNlBKNOdi/mzmtRF+XQJAD9sHw08r" + "\r"
			+ "cZtgukIrgQjpKjIyK/p4Acm0mE6QoxsAkmRXttzaQGEtLP/TqGIIfT0X99YH0L8R" + "\r"
			+ "SgqT3Ig+pWkH7w==" + "\r";

	/**
	 * 私钥
	 */
	private PrivateKey privateKey;

	/**
	 * 公钥
	 */
	private PublicKey publicKey;
	
	@PostConstruct
	private void init() {
		try {
			privateKey = this.loadPrivateKey(PRIVATE_KEY);
			publicKey = this.loadPublicKey(PUBLIC_KEY);
		} catch (Exception e) {
			throw new RuntimeException("load key fail[" + e.getMessage() + "]");
		}
		
	}
	
	public AuthHelper() throws Exception {
		privateKey = this.loadPrivateKey(PRIVATE_KEY);
		publicKey = this.loadPublicKey(PUBLIC_KEY);
	}
	
	/**
	 * 签名
	 * @param plain
	 * @return
	 * @throws Exception
	 */
	public String sign(String plain) throws Exception {
		Signature signetcheck = Signature.getInstance("MD5withRSA");
		signetcheck.initSign(privateKey);
		signetcheck.update(plain.getBytes("ISO-8859-1"));
		return Hex.encodeHexString(signetcheck.sign());
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

	/**
	 * 加载私钥
	 * @param privateKeyStr
	 * @throws Exception
	 */
	private PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
		try {
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("私钥非法");
		} catch (IOException e) {
			throw new Exception("私钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}
	}
	
}
