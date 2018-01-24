package net.jahhan.demo.controller;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;

import lombok.extern.slf4j.Slf4j;
//import net.jahhan.authenticationcenter.intf.UserIntf;
//import net.jahhan.authenticationcenter.vo.UserAuthenticationOVO;
import net.jahhan.cache.annotation.Cache;
import net.jahhan.common.extension.annotation.GlobalSyncTransaction;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.demo.dao.TUserDemoInfoDao;
import net.jahhan.demo.intf.DemoIntf;
import net.jahhan.demo.pojo.TUserDemoInfo;
import net.jahhan.demo.service.UserService;
import net.jahhan.demo.vo.UserNameIVO;
import net.jahhan.demo.vo.UserOVO;
import net.jahhan.demo.vo.UserRegistIVO;
import net.jahhan.demo.vo.UserRegistOVO;
import net.jahhan.exception.JahhanException;
import net.jahhan.test.intf.TestService;

@Service
@Slf4j
@Singleton
public class UserController implements DemoIntf {
	@Inject
	private UserService userService;
	@Inject
	private TUserDemoInfoDao tUserInfoDao;
	@Reference
	private TestService testService;

	@Override
	@GlobalSyncTransaction
	public UserRegistOVO register(UserRegistIVO userRegistIVO) {
		if (userRegistIVO.getGender().equals("女") && userRegistIVO.getUserName().startsWith("test")) {
			JahhanException.throwException(HttpServletResponse.SC_BAD_REQUEST, JahhanErrorCode.VALIATION_EXCEPTION,
					"性别为女时名称不能以test开头！");
		}
		return userService.register(userRegistIVO);
	}

	@Override
	public UserOVO updateUserName(UserNameIVO userNameIVO) {
		log.info("修改用户id：" + userNameIVO.getUserId());
		return userService.updateUserName(userNameIVO);
	}

	@Override
	@Cache(blockTime = 10)
	public void deleteUser(Long userId) {
		TUserDemoInfo tUserDemoInfo = tUserInfoDao.queryTUserDemoInfo(userId);
		Assert.notNull(tUserDemoInfo, "用户不存在", JahhanErrorCode.VALIATION_EXCEPTION);
		Assert.isTrue(tUserDemoInfo.getGender().equals("男"), "性别为男的不允许删除", JahhanErrorCode.VALIATION_EXCEPTION);
		userService.deleteUser(userId);
	}

	@Override
	// @NoneToken
	@Cache(blockTime = 3, isCustomCacheKey = true, argumentIndexNumbers = {
			0 }, customCacheKeyCreaterClass = CacheKeyCreater.class)
	public UserOVO getUser(Long userId) {
		return userService.getUser(userId);
	}

}
