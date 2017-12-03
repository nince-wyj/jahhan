package net.jahhan.controller.authenticationcenter.intf;

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
import net.jahhan.common.extension.constant.ContentType;
import net.jahhan.controller.authenticationcenter.vo.ServiceAuthenticationOVO;
import net.jahhan.controller.authenticationcenter.vo.ServiceAuthorizeMessageOVO;
import net.jahhan.controller.authenticationcenter.vo.ServiceLoginIVO;
import net.jahhan.controller.authenticationcenter.vo.ServiceLoginOVO;
import net.jahhan.controller.authenticationcenter.vo.ServiceRegisteIVO;
import net.jahhan.controller.authenticationcenter.vo.ServiceRegisteOVO;
import net.jahhan.controller.authenticationcenter.vo.TokenIVO;
import net.jahhan.service.service.bean.TokenOVO;

@Path("service")
@Consumes({ ContentType.APPLICATION_JSON_UTF_8 })
@Produces({ ContentType.APPLICATION_JSON_UTF_8 })
@Api("服务接口")
public interface ServiceIntf {

	@POST
	@ApiOperation(value = "服务注册")
	public ServiceRegisteOVO register(@NotNull(message = "服务信息不能为空") ServiceRegisteIVO serviceRegisteIVO);

	@POST
	@Path("login")
	@ApiOperation(value = "服务登陆")
	public ServiceLoginOVO login(@NotNull(message = "服务信息不能为空") ServiceLoginIVO serviceLoginIVO);

	@POST
	@Path("refresh_token")
	@ApiOperation(value = "刷新token")
	public TokenOVO refreshToken(@NotNull(message = "token信息不能为空") TokenIVO tokenIVO);

	@GET
	@Path("authentication")
	@ApiOperation(value = "鉴权")
	public ServiceAuthenticationOVO authentication(
			@QueryParam(value = "token") @ApiParam(value = "token") @NotNull(message = "token不能为空") String token);

	@GET
	@Path("redirect")
	@ApiOperation(value = "重定向跳转")
	public void redirect(
			@QueryParam(value = "redirect_uri") @NotNull(message = "redirect_uri不能为空") @ApiParam(value = "redirect_uri") String redirectUri);

	@GET
	@Path("authorize/message")
	@ApiOperation(value = "授权服务器信息")
	public ServiceAuthorizeMessageOVO authorizeMessage(
			@QueryParam(value = "service_code") @ApiParam(value = "service_code") @NotNull(message = "service_code不能为空") String serviceCode,
			@QueryParam(value = "client_code") @ApiParam(value = "client_code") @NotNull(message = "client_code不能为空") String clientCode);
}
