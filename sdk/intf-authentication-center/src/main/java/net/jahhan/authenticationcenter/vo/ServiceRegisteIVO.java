package net.jahhan.authenticationcenter.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务注册")
public class ServiceRegisteIVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@NotNull(message = "service不能为空")
	@ApiModelProperty(value = "服务名称")
	@Length(min = 0, max = 32, message = "service字符串长度需小于32")
	private String service;

	@NotNull(message = "serviceCode不能为空")
	@ApiModelProperty(value = "服务代码")
	@Length(min = 0, max = 32, message = "serviceCode字符串长度需小于32")
	private String serviceCode;

	@Pattern(regexp = "inner|third", message = "serviceType错误")
	@ApiModelProperty(value = "服务类型(inner：内部服务，third：第三方服务)默认值：inner")
	private String serviceType = "inner";

	@ApiModelProperty(value = "token过期时间(秒)")
	private Integer tokenExipres;
}