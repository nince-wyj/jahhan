package net.jahhan.authenticationcenter.intf;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jahhan.authenticationcenter.vo.ExchangeTokenIVO;
import net.jahhan.authenticationcenter.vo.ThirdTokenOVO;
import net.jahhan.authenticationcenter.vo.TokenIVO;
import net.jahhan.authenticationcenter.vo.UserAuthenticationOVO;
import net.jahhan.authenticationcenter.vo.UserLoginIVO;
import net.jahhan.authenticationcenter.vo.UserLoginOVO;
import net.jahhan.authenticationcenter.vo.UserRegistIVO;
import net.jahhan.authenticationcenter.vo.UserRegistOVO;
import net.jahhan.authenticationcenter.vo.UserTelLoginIVO;
import net.jahhan.common.extension.constant.ContentType;
import net.jahhan.service.service.bean.TokenOVO;

@Path("user")
@Consumes({ ContentType.APPLICATION_JSON_UTF_8 })
@Produces({ ContentType.APPLICATION_JSON_UTF_8 })
@Api("用户接口")
public interface UserIntf {

	@POST
	@Path("register")
	@ApiOperation(value = "用户注册")
	public UserRegistOVO register(@NotNull(message = "注册信息不能为空") UserRegistIVO userRegistIVO);

	@POST
	@Path("login")
	@ApiOperation(value = "用户登陆")
	public UserLoginOVO login(@NotNull(message = "登陆信息不能为空") UserLoginIVO userLogin);

	@GET
	@Path("tel/validation_code")
	@ApiOperation(value = "发送短信验证码")
	public void validationCode(
			@QueryParam(value = "tel") @NotNull(message = "tel不能为空") @ApiParam(value = "电话号码") String tel);

	@POST
	@Path("tel/login")
	@ApiOperation(value = "用户短信登陆")
	public UserLoginOVO telLogin(@NotNull(message = "登陆信息不能为空") UserTelLoginIVO userLogin);

	@GET
	@Path("open_id/login")
	@ApiOperation(value = "用户openid登陆")
	public UserLoginOVO openIdLogin(
			@QueryParam(value = "open_id") @NotNull(message = "open_id不能为空") @ApiParam(value = "open_id") String openId);

	@POST
	@Path("refresh_token")
	@ApiOperation(value = "刷新token")
	public TokenOVO refreshToken(@NotNull(message = "token信息不能为空") TokenIVO tokenIVO);

	@GET
	@Path("authentication")
	@ApiOperation(value = "鉴权")
	public UserAuthenticationOVO authentication(
			@QueryParam(value = "token") @NotNull(message = "token不能为空") @ApiParam(value = "token") String token,
			@QueryParam(value = "token_type") @NotNull(message = "token_type不能为空") @ApiParam(value = "token类型") String tokenType,
			@QueryParam(value = "nonce") @ApiParam(value = "随机码") String nonce,
			@QueryParam(value = "new_nonce") @ApiParam(value = "新随机码") String newNonce,
			@QueryParam(value = "sign") @ApiParam(value = "请求签名串") String sign);

	@GET
	@ApiOperation(value = "查询")
	public UserAuthenticationOVO getUser(
			@QueryParam(value = "user_id") @NotNull(message = "user_id不能为空") @ApiParam(value = "user_id") Long userId);

	@GET
	@Path("redirect/message")
	@ApiOperation(value = "用户重定向信息")
	public void redirectMessage(
			@QueryParam(value = "token") @NotNull(message = "token不能为空") @ApiParam(value = "token") String token);

	@POST
	@Path("token")
	@ApiOperation(value = "交换token")
	public ThirdTokenOVO exchangeToken(ExchangeTokenIVO exchangeTokenIVO);
}
