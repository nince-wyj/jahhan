package net.jahhan.demo.service;

import java.sql.Timestamp;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Service;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.common.extension.utils.BeanTools;
import net.jahhan.demo.dao.TUserDemoInfoDao;
import net.jahhan.demo.pojo.TUserDemoInfo;
import net.jahhan.demo.vo.UserNameIVO;
import net.jahhan.demo.vo.UserOVO;
import net.jahhan.demo.vo.UserRegistIVO;
import net.jahhan.demo.vo.UserRegistOVO;

@Service
@Singleton
public class UserService {
	@Inject
	private TUserDemoInfoDao tUserInfoDao;

	public UserRegistOVO register(UserRegistIVO userRegistIVO) {
		TUserDemoInfo tUserDemoInfo = new TUserDemoInfo();
		BeanTools.copyBean(tUserDemoInfo, userRegistIVO);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		tUserDemoInfo.setCreateTime(now);
		tUserDemoInfo.setLastModifyTime(now);
		tUserInfoDao.addTUserDemoInfo(tUserDemoInfo);
		UserRegistOVO userRegistOVO = new UserRegistOVO();
		BeanTools.copyBean(userRegistOVO, tUserDemoInfo);
		return userRegistOVO;
	}

	public UserOVO updateUserName(UserNameIVO userNameIVO) {
		TUserDemoInfo tUserDemoInfo = new TUserDemoInfo();
		tUserDemoInfo.setUserName(userNameIVO.getUserName());
		tUserInfoDao.updatePartTUserDemoInfo(userNameIVO.getUserId(), tUserDemoInfo);
		UserOVO userOVO = new UserOVO();
		BeanTools.copyBean(userOVO, tUserDemoInfo);
		return userOVO;
	}

	public void deleteUser(Long userId) {
		tUserInfoDao.delTUserDemoInfo(userId);
	}

	public UserOVO getUser(Long userId) {
		TUserDemoInfo tUserDemoInfo = tUserInfoDao.queryTUserDemoInfo(userId);
		Assert.notNull(tUserDemoInfo, "用户不存在", JahhanErrorCode.BIZ_EXCEPTION);
		UserOVO userOVO = new UserOVO();
		BeanTools.copyBean(userOVO, tUserDemoInfo);
		return userOVO;
	}

}
