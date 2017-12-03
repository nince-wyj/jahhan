package net.jahhan.demo.intf;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jahhan.common.extension.constant.ContentType;
import net.jahhan.demo.vo.UserNameIVO;
import net.jahhan.demo.vo.UserOVO;
import net.jahhan.demo.vo.UserRegistIVO;
import net.jahhan.demo.vo.UserRegistOVO;
import net.jahhan.service.service.bean.User;

@Path("user")
@Consumes({ ContentType.APPLICATION_JSON_UTF_8 })
@Produces({ ContentType.APPLICATION_JSON_UTF_8 })
@Api("用户接口")
public interface DemoIntf {

	@POST
	@Path("register")
	@ApiOperation(value = "用户注册")
	public UserRegistOVO register(@NotNull(message = "注册信息不能为空") UserRegistIVO userRegistIVO);
	
	@PUT
	@Path("updateUserName")
	@ApiOperation(value = "用户更名")
	public UserOVO updateUserName(@NotNull(message = "用户信息不能为空") UserNameIVO userNameIVO,User user);

	@DELETE
	@ApiOperation(value = "删除")
	public void deleteUser(
			@QueryParam(value = "user_id") @NotNull(message = "user_id不能为空") @ApiParam(value = "user_id") Long userId);

	@GET
	@ApiOperation(value = "查询")
	public UserOVO getUser(
			@QueryParam(value = "user_id") @NotNull(message = "user_id不能为空") @ApiParam(value = "user_id") Long userId);
}
