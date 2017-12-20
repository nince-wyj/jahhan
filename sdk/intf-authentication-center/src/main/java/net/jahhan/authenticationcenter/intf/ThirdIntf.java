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
import net.jahhan.authenticationcenter.vo.ThirdAuthenticationOVO;
import net.jahhan.authenticationcenter.vo.TokenIVO;
import net.jahhan.common.extension.constant.ContentType;
import net.jahhan.service.service.bean.TokenOVO;

@Path("third")
@Consumes({ ContentType.APPLICATION_JSON_UTF_8 })
@Produces({ ContentType.APPLICATION_JSON_UTF_8 })
@Api("第三方服务接口")
public interface ThirdIntf {
	@GET
	@Path("authorize")
	@ApiOperation(value = "授权跳转")
	public void authorize(
			@QueryParam(value = "client_id") @NotNull(message = "client_id不能为空") @ApiParam(value = "第三方id") String clientId,
			@QueryParam(value = "service_id") @NotNull(message = "service_id不能为空") @ApiParam(value = "服务id") String serviceId,
			@QueryParam(value = "redirect_uri") @NotNull(message = "redirect_uri不能为空") @ApiParam(value = "跳转url") String redirectUri,
			@QueryParam(value = "state") @ApiParam(value = "state") String state);

	@GET
	@Path("authentication")
	@ApiOperation(value = "鉴权")
	public ThirdAuthenticationOVO authentication(
			@QueryParam(value = "token") @NotNull(message = "token不能为空") @ApiParam(value = "token") String token);

	@POST
	@Path("refresh_token")
	@ApiOperation(value = "刷新token")
	public TokenOVO refreshToken(@NotNull(message = "token信息不能为空") TokenIVO tokenIVO);

}
