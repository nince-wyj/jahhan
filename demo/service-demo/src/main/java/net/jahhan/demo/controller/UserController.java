package net.jahhan.demo.controller;

import com.frameworkx.annotation.Controller;
import com.frameworkx.annotation.Reference;
import lombok.extern.slf4j.Slf4j;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

//import net.jahhan.authenticationcenter.intf.UserIntf;
//import net.jahhan.authenticationcenter.vo.UserAuthenticationOVO;

@Controller
@Slf4j
@Singleton
public class UserController implements DemoIntf {
    @Inject
    private UserService userService;
    @Inject
    private TUserDemoInfoDao tUserInfoDao;
    @Reference
    private TestService testService;
//	@Reference
//	private UserIntf userIntf;

    @Override
    @GlobalSyncTransaction
    public UserRegistOVO register(UserRegistIVO userRegistIVO) {
        if (userRegistIVO.getGender().equals("女") && userRegistIVO.getUserName().startsWith("test")) {
            JahhanException.throwException(HttpServletResponse.SC_BAD_REQUEST, JahhanErrorCode.VALIATION_EXCEPTION,
                    "性别为女时名称不能以test开头！");
        }
        // testService.excuteOptimization();
//		net.jahhan.authenticationcenter.vo.UserRegistIVO r = new net.jahhan.authenticationcenter.vo.UserRegistIVO();
//		r.setEmail(System.currentTimeMillis() + "aaa@bbb.com");
//		r.setPassword("123456");
//		net.jahhan.authenticationcenter.vo.UserRegistOVO register = userIntf.register(r);
//		UserAuthenticationOVO user = userIntf.getUser(register.getUserId());
        return userService.register(userRegistIVO);
    }

    @Override
    public UserOVO updateUserName(UserNameIVO userNameIVO) {
        // log.info("登陆用户id：" + user.getUserId());
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
    @Cache(blockTime = 3, isCustomCacheKey = true, argumentIndexNumbers = {0}, indexArgumentField = {""}, customCacheKeyCreaterClass = UserNameIVO.class)
    public UserOVO getUser(Long userId) {
        return userService.getUser(userId);
    }

    @Override
    @Cache(blockTime = 10, isCustomCacheKey = true, argumentIndexNumbers = {0}, indexArgumentField = {"userId"})
    public UserOVO getUser(UserNameIVO userNameIVO) {
        return userService.getUser(userNameIVO.getUserId());
    }
}
