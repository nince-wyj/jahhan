package net.jahhan.lmq;

import com.alibaba.fastjson.JSONObject;
import net.jahhan.lmq.common.define.MqToken;
import net.jahhan.lmq.common.define.MqTopic;
import net.jahhan.lmq.common.define.MqTopicDefine;
import net.jahhan.lmq.common.define.QoS;
import net.jahhan.lmq.common.util.Tools;

import java.io.IOException;
import java.security.*;
import java.util.*;

/**
 * Created by linwb on 2017/12/16 0016.
 */
public class AliyunMQTokenUtil {
    private AliyunMQTokenUtil() {
    }

    public static String applyToken(MqTopic... topicArr) throws InvalidKeyException, NoSuchAlgorithmException,
			UnrecoverableKeyException, KeyManagementException, KeyStoreException, IOException {
		List<String> paramList = new ArrayList<String>();
		for (MqTopic topic : topicArr) {
			paramList.add(topic.getTopicName());
		}
		Collections.sort(paramList);

		StringBuilder sb=new StringBuilder();
		for (String name : paramList) {
			sb.append(name).append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}

		String apiUrl = "https://mqauth.aliyuncs.com/token/apply";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("resources",sb.toString());
		paramMap.put("actions", MqTopicDefine.parentTopic.getTokenPermission().toString());
		paramMap.put("serviceName", "mq");
		paramMap.put("expireTime", String.valueOf(System.currentTimeMillis() + 20*60*60*1000));
		String signature = Tools.doHttpSignature(paramMap, MqTopicDefine.secretKey);
		paramMap.put("proxyType", MqToken.MQTT.toString());
		paramMap.put("accessKey", MqTopicDefine.accessKey);
		paramMap.put("signature", signature);
		JSONObject object = Tools.httpsPost(apiUrl, paramMap);
//		Charset charset = Charset.forName("UTF-8");
//		System.out.println("token:"+new String(Base64.decodeBase64(object.getString("tokenData")),charset));
		System.out.println(object);

		return null;
	}

	/** 吊销token */
	public static void revokeToken(String token) throws InvalidKeyException, NoSuchAlgorithmException, IOException,
			UnrecoverableKeyException, KeyStoreException, KeyManagementException {
		String apiUrl = "https://mqauth.aliyuncs.com/token/revoke";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("token", token);
		String signature = Tools.doHttpSignature(paramMap, MqTopicDefine.secretKey);
		paramMap.put("signature", signature);
		paramMap.put("accessKey", MqTopicDefine.accessKey);
		JSONObject object = Tools.httpsPost(apiUrl, paramMap);

		System.out.println(object);
	}

	/** 查詢token */
	public static String queryToken(String token) throws InvalidKeyException, NoSuchAlgorithmException, IOException,
			UnrecoverableKeyException, KeyStoreException, KeyManagementException {
		String apiUrl = "https://mqauth.aliyuncs.com/token/query";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("token", token);
		String signature = Tools.doHttpSignature(paramMap, MqTopicDefine.secretKey);
		paramMap.put("signature", signature);
		paramMap.put("accessKey", MqTopicDefine.accessKey);
		JSONObject object = Tools.httpsPost(apiUrl, paramMap);
		System.out.println(object);
		return null;
	}

	public static void main(String[] args) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, InvalidKeyException, IOException {
		AliyunMQTokenUtil.applyToken(MqTopicDefine.parentTopic,new MqTopic(MqTopicDefine.parentTopic.getTopicName()+"/1111111111", QoS.QoS1),new MqTopic(MqTopicDefine.parentTopic.getTopicName()+"/0591", QoS.QoS1));
	}
}
