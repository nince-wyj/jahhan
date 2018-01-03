package net.jahhan.lmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.jahhan.lmq.common.define.MqTokenType;
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
@Slf4j
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
		paramMap.put("proxyType", MqTokenType.MQTT.toString());
		paramMap.put("accessKey", MqTopicDefine.accessKey);
		paramMap.put("signature", signature);
		JSONObject result = Tools.httpsPost(apiUrl, paramMap);
		log.debug("topics:{}\napplyToken result:{}",topicArr,result);
		String tokenData=null;
		if(200==result.getInteger("code")){
			tokenData= result.getString("tokenData");
		}else{
			Map<String,Object> exMap=new HashMap<>();
			exMap.put("topics",topicArr);
			exMap.put("result",result);
			throw new RuntimeException(JSON.toJSONString(exMap));
		}


		return tokenData;
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
		JSONObject result = Tools.httpsPost(apiUrl, paramMap);
        log.debug("revokeToken:{} result:{}",token,result);
		if(200!=result.getInteger("code")){
			Map<String,Object> exMap=new HashMap<>();
			exMap.put("revokeToken:",token);
			exMap.put("result",result);
			throw new RuntimeException(JSON.toJSONString(exMap));
		}
	}

	/** 查詢tokens是否存在有效，true有效，false无效或不存在 */
	public static boolean queryToken(String token) throws InvalidKeyException, NoSuchAlgorithmException, IOException,
			UnrecoverableKeyException, KeyStoreException, KeyManagementException {
		boolean valid=false;
		String apiUrl = "https://mqauth.aliyuncs.com/token/query";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("token", token);
		String signature = Tools.doHttpSignature(paramMap, MqTopicDefine.secretKey);
		paramMap.put("signature", signature);
		paramMap.put("accessKey", MqTopicDefine.accessKey);
		JSONObject result = Tools.httpsPost(apiUrl, paramMap);
		log.debug("queryToken:{}\nresult:{}",token,result);

		String tokenData=null;
		if(200!=result.getInteger("code")){
			valid=false;
			Map<String,Object> exMap=new HashMap<>();
			exMap.put("topics",token);
			exMap.put("result",result);
			throw new RuntimeException(JSON.toJSONString(exMap));
		}

		return valid;
	}

	public static void main(String[] args) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, InvalidKeyException, IOException {
	String token=	AliyunMQTokenUtil.applyToken(MqTopicDefine.parentTopic,new MqTopic(MqTopicDefine.parentTopic.getTopicName()+"/3333333333", QoS.QoS1));
//		AliyunMQTokenUtil.revokeToken("LzMT+XLFl5uQ6bDU0o4vUiD1tyZUuRMsRtiCs0jC/89YfYNDKkKbsD8SHE+oBvCx+rR6voF7GWM55H57d740BtZQqbGMBT1BOKHdGDbP3MVCvKP80Xo0WTyueZHxmSH3RFhfnieKruPKbKeh+/kYqXEJ2FHc7ldOM536K4f+b6h0zQbn/x8h7uBOALgUzpoVsGBb81BRxGSfBfMN2oDZc/Wl/2390vCnZpZTFyqqueze3810hZnBRhm/nWfdML8gvJ4KOmSv4DAr9AveBwlpG2JVRASkYonJ+Ct6Bp11Tmg=");
//		AliyunMQTokenUtil.queryToken("LzMT+XLFl5uQ6bDU0o4vUiD1tyZUuRMsRtiCs0jC/89YfYNDKkKbsD8SHE+oBvCx+rR6voF7GWM55H57d740BleFl1Mz7xXC4tSxMok17I1CvKP80Xo0WTyueZHxmSH3RFhfnieKruPKbKeh+/kYqXEJ2FHc7ldOM536K4f+b6h0zQbn/x8h7uBOALgUzpoVsGBb81BRxGSfBfMN2oDZc/Wl/2390vCnZpZTFyqqueze3810hZnBRjicT9hkYUXxvRM5TsSLVpAnvTFkK6hsfc/IDDpDA7k8+Ct6Bp11Tmgcccccc=");
	}
}
