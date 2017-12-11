package net.jahhan.service.authenticationcenter;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;

import com.frameworkx.annotation.Reference;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.cache.util.SerializerUtil;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.context.BaseContext;
import net.jahhan.context.BaseVariable;
import net.jahhan.controller.authenticationcenter.intf.ServiceIntf;
import net.jahhan.controller.authenticationcenter.intf.UserIntf;
import net.jahhan.controller.authenticationcenter.vo.ServiceAuthenticationOVO;
import net.jahhan.controller.authenticationcenter.vo.TokenIVO;
import net.jahhan.controller.authenticationcenter.vo.UserAuthenticationOVO;
import net.jahhan.exception.JahhanException;
import net.jahhan.service.context.AuthenticationVariable;
import net.jahhan.service.service.bean.Service;
import net.jahhan.service.service.bean.TokenOVO;
import net.jahhan.service.service.bean.User;
import net.jahhan.service.service.constant.UserTokenType;
import net.jahhan.spi.TokenCache;
import net.jahhan.spi.common.ICrypto;

@Named
@Singleton
public class Authentication {
	@Reference
	private ServiceIntf serviceInft;
	@Reference
	private UserIntf userInft;
	@Inject
	private TokenCache cache;
	private ICrypto crypto = ExtensionExtendUtil.getExtension(ICrypto.class, "aes");

	private static String SERVICE = BaseConfiguration.SERVICE;

	public boolean decryptToken(Map<String, String> tokenMap) {
		boolean needDecrypt = true;
		AuthenticationVariable authenticationVariable = AuthenticationVariable.getAuthenticationVariable();
		if (tokenMap.containsKey("DEBUG") && BaseConfiguration.IS_DEBUG) {
			String idInfo = tokenMap.get("DEBUG");
			for (String info : idInfo.split(",")) {
				String[] split = info.split("=");
				if (split[0].equals("user_id")) {
					User user = new User();
					Long userId = Long.valueOf(split[1]);
					UserAuthenticationOVO userAuthenticationOVO = userInft.getUser(userId);
					user.setUserId(userId);
					user.setUserAuthorizationType(UserTokenType.DEBUG);
					if (null != userAuthenticationOVO) {
						user.setAuthList(userAuthenticationOVO.getAuthList());
					}
					setUser(user);
					continue;
				}
				if (split[0].equals("service_code")) {
					String serviceCode = split[1];
					Service service = new Service();
					service.setServiceCode(serviceCode);
					authenticationVariable.setService(service);
					continue;
				}
			}
			return false;
		}
		if (tokenMap.containsKey("NONETOKEN") || tokenMap.isEmpty()) {
			authenticationVariable.setNoneToken(true);
			return false;
		}
		if (tokenMap.containsKey("BASIC_TOKEN")) {
			String token = tokenMap.get("BASIC_TOKEN");
			byte[] serviceBytes = cache.getBinary(("BASIC_TOKEN_" + SERVICE + "_" + token).getBytes());
			Service service = null;
			if (null != serviceBytes) {
				service = SerializerUtil.deserialize(serviceBytes, Service.class);
				needDecrypt = false;
			} else {
				ServiceAuthenticationOVO authentication = serviceInft.authentication(token);
				Assert.notNull(authentication, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				if (authentication.getTokenType().equals("BASIC_TOKEN")) {
					service = new Service();
					service.setServiceCode(authentication.getServiceCode());
					service.setInnerService(true);
					serviceBytes = SerializerUtil.serializeFrom(service);
					cache.setByte(("BASIC_TOKEN_" + SERVICE + "_" + token).getBytes(), serviceBytes);
					needDecrypt = false;
				}
			}
			authenticationVariable.setService(service);
		} else if (tokenMap.containsKey("ACCESS_TOKEN")) {
			String ciphertext = tokenMap.get("ACCESS_TOKEN");
			String requestId = BaseVariable.getBaseVariable().getRequestId();
			String thirdKey = BaseContext.CTX.getThirdPubKey();

			String key = thirdKey.substring(0, 8) + requestId.substring(0, 8);
			String decrypt = crypto.decrypt(ciphertext, key);
			String[] split = decrypt.split("_");
			String tokenAndNonce = split[0];
			String token = tokenAndNonce.substring(0, tokenAndNonce.length() - 4);
			String nonce = tokenAndNonce.substring(tokenAndNonce.length() - 4);
			byte[] serviceBytes = cache.getBinary(("ACCESS_TOKEN_" + SERVICE + "_" + token).getBytes());
			Service service = null;
			if (null != serviceBytes) {
				service = SerializerUtil.deserialize(serviceBytes, Service.class);
			} else {
				ServiceAuthenticationOVO authentication = serviceInft.authentication(token);
				Assert.notNull(authentication, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				if (authentication.getTokenType().equals("ACCESS_TOKEN")) {
					service = new Service();
					service.setServiceCode(authentication.getServiceCode());
					service.setSecrityKey(authentication.getSecrityKey());
					service.setInnerService(false);
					cache.setEx(("ACCESS_TOKEN_" + SERVICE + "_" + token).getBytes(),
							authentication.getExpireIn().intValue(), serviceBytes);
				}
			}
			authenticationVariable.setService(service);
			String requestMode = split[1];
			if (requestMode.equals("l")) {
				authenticationVariable
						.setKey(thirdKey.substring(thirdKey.length() - 8) + requestId.substring(0, 4) + nonce);
				authenticationVariable.setCommonRequest(false);
			} else if (requestMode.equals("c")) {
				authenticationVariable.setKey(service.getSecrityKey().substring(0, 4) + token.substring(0, 4)
						+ requestId.substring(0, 4) + nonce);
			}
			authenticationVariable.setCheckMode(true);
		}
		if (tokenMap.containsKey(UserTokenType.BEARER_TOKEN.getValue())) {
			String ciphertext = tokenMap.get(UserTokenType.BEARER_TOKEN.getValue());
			String requestId = BaseVariable.getBaseVariable().getRequestId();
			String appKey = BaseContext.CTX.getAppPubKey();
			String key = appKey.substring(0, 8) + requestId.substring(0, 8);
			String decrypt = crypto.decrypt(ciphertext, key);
			String[] split = decrypt.split("_");
			String tokenAndNonce = split[0];
			String token = tokenAndNonce.substring(0, tokenAndNonce.length() - 4);
			String nonce = tokenAndNonce.substring(tokenAndNonce.length() - 4);
			Assert.isTrue(nonce.length() == 4, "TOKEN错误！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
			byte[] userBytes = cache
					.getBinary((UserTokenType.BEARER_TOKEN.getValue() + "_" + SERVICE + "_" + token).getBytes());
			User user = null;
			if (null != userBytes) {
				user = SerializerUtil.deserialize(userBytes, User.class);
				Long ttl = cache.ttl(UserTokenType.BEARER_TOKEN.getValue() + "_" + SERVICE + "_" + token);
				if (ttl < 3600) {
					user.setNeedRefreshToken(true);
				}
			} else {
				UserAuthenticationOVO authentication = userInft.authentication(token,
						UserTokenType.BEARER_TOKEN.getValue(), null, null, null);
				Assert.notNull(authentication, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				if (null == authentication.getUserId()) {
					String requestMode = split[1];
					if (requestMode.equals("l")) {
						authenticationVariable
								.setKey(appKey.substring(appKey.length() - 8) + requestId.substring(0, 4) + nonce);
						authenticationVariable.setFirstSingleToken(true);
						return true;
					} else {
						JahhanException.throwException(HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY,
								"错误的token信息！");
					}
				} else {
					user = new User();
					user.setUserAuthorizationType(UserTokenType.BEARER_TOKEN);
					user.setUserId(authentication.getUserId());
					user.setAuthList(authentication.getAuthList());
					user.setSecrityKey(authentication.getSecrityKey());
					userBytes = SerializerUtil.serializeFrom(user);
					cache.setEx((UserTokenType.BEARER_TOKEN.getValue() + "_" + SERVICE + "_" + token).getBytes(),
							authentication.getExpireIn().intValue(), userBytes);
					if (authentication.getExpireIn() < 3600) {
						user.setNeedRefreshToken(true);
					}
				}
			}
			if (tokenMap.containsKey("REFRESH_TOKEN")) {
				String refreshToken = tokenMap.get("REFRESH_TOKEN");
				TokenIVO tokenIVO = new TokenIVO();
				tokenIVO.setRefreshToken(refreshToken);
				tokenIVO.setToken(token);
				TokenOVO tokenOVO = userInft.refreshToken(tokenIVO);
				Assert.notNull(tokenOVO, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				user.setTokenOVO(tokenOVO);
				cache.setEx(
						(UserTokenType.BEARER_TOKEN.getValue() + "_" + SERVICE + "_" + tokenOVO.getToken()).getBytes(),
						tokenOVO.getExpireIn().intValue(), userBytes);
			}
			setUser(user);
			String requestMode = split[1];
			if (requestMode.equals("l")) {
				authenticationVariable
						.setKey(appKey.substring(appKey.length() - 8) + requestId.substring(0, 4) + nonce);
				authenticationVariable.setCommonRequest(false);
			} else if (requestMode.equals("c")) {
				authenticationVariable.setKey(user.getSecrityKey().substring(0, 4) + token.substring(0, 4)
						+ requestId.substring(0, 4) + nonce);
			}
			authenticationVariable.setCheckMode(true);
		} else if (tokenMap.containsKey(UserTokenType.SINGLE_TOKEN.getValue())) {
			String ciphertext = tokenMap.get(UserTokenType.SINGLE_TOKEN.getValue());
			ciphertext = ciphertext.replace(" ", "+");
			if (!ciphertext.equals(BaseContext.CTX.getFirstSingleToken())) {
				String browserKey = BaseContext.CTX.getBrowserSecrityKey();
				String decrypt = crypto.decrypt(ciphertext, browserKey);
				String token = decrypt.substring(0, decrypt.length() - 4);
				String nonce = decrypt.substring(decrypt.length() - 4);
				String newNonce = RandomStringUtils.randomAlphanumeric(4);
				UserAuthenticationOVO authentication = userInft.authentication(token,
						UserTokenType.SINGLE_TOKEN.getValue(), nonce, newNonce, authenticationVariable.getSign());
				Assert.notNull(authentication, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				if (null == authentication.getUserId()) {
					authenticationVariable.setFirstSingleToken(true);
					authenticationVariable.setKey(BaseContext.CTX.getBrowserPubKey());
					return true;
				}
				User user = new User();
				if (null != authentication.getNonce()) {
					newNonce = authentication.getNonce();
				}
				user.setNewToken(crypto.encrypt(token + newNonce, browserKey));
				user.setUserId(authentication.getUserId());
				user.setAuthList(authentication.getAuthList());
				user.setSecrityKey(authentication.getSecrityKey());
				user.setUserAuthorizationType(UserTokenType.SINGLE_TOKEN);
				setUser(user);
			}
			authenticationVariable.setKey(BaseContext.CTX.getBrowserPubKey());
		} else if (tokenMap.containsKey(UserTokenType.OPEN_TOKEN.getValue())) {
			String ciphertext = tokenMap.get(UserTokenType.OPEN_TOKEN.getValue());
			String innerSecrityKey = BaseContext.CTX.getInnerSecrityKey();
			String decrypt = crypto.decrypt(ciphertext, innerSecrityKey);
			String token = decrypt.substring(0, decrypt.length() - 4);
			byte[] userBytes = cache
					.getBinary((UserTokenType.OPEN_TOKEN.getValue() + "_" + SERVICE + "_" + token).getBytes());
			User user = null;
			if (null != userBytes) {
				user = SerializerUtil.deserialize(userBytes, User.class);
				Long ttl = cache.ttl(UserTokenType.BEARER_TOKEN.getValue() + "_" + SERVICE + "_" + token);
				if (ttl < 3600) {
					user.setNeedRefreshToken(true);
				}
			} else {
				UserAuthenticationOVO authentication = userInft.authentication(token,
						UserTokenType.OPEN_TOKEN.getValue(), null, null, null);
				Assert.notNull(authentication, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				user = new User();
				user.setUserAuthorizationType(UserTokenType.OPEN_TOKEN);
				user.setUserId(authentication.getUserId());
				user.setAuthList(authentication.getAuthList());
				user.setSecrityKey(authentication.getSecrityKey());
				cache.setEx((UserTokenType.OPEN_TOKEN.getValue() + "_" + SERVICE + "_" + token).getBytes(),
						authentication.getExpireIn().intValue(), userBytes);
				if (authentication.getExpireIn() < 3600) {
					user.setNeedRefreshToken(true);
				}
			}
			if (tokenMap.containsKey("REFRESH_TOKEN")) {
				String refreshToken = tokenMap.get("REFRESH_TOKEN");
				TokenIVO tokenIVO = new TokenIVO();
				tokenIVO.setRefreshToken(refreshToken);
				tokenIVO.setToken(token);
				TokenOVO tokenOVO = userInft.refreshToken(tokenIVO);
				Assert.notNull(tokenOVO, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				user.setTokenOVO(tokenOVO);
				cache.setEx(
						(UserTokenType.BEARER_TOKEN.getValue() + "_" + SERVICE + "_" + tokenOVO.getToken()).getBytes(),
						tokenOVO.getExpireIn().intValue(), userBytes);
			}
			setUser(user);
		}
		return needDecrypt;
	}

	private void setUser(User user) {
		AuthenticationVariable authenticationVariable = AuthenticationVariable.getAuthenticationVariable();
		authenticationVariable.setUser(user);
	}
}
