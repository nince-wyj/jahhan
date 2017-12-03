package net.jahhan.demo.pojo;

import java.sql.Timestamp;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.jdbc.SuperPojo;
import net.jahhan.jdbc.validategroup.Create;
import net.jahhan.jdbc.validategroup.Modify;
import net.jahhan.jdbc.validategroup.Reset;

/**
 * t_user_demo_info:用户信息表
 * 自动生成,开发人员请勿修改.
 * @author code-generate-service
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "用户信息表")
public class TUserDemoInfo extends SuperPojo<TUserDemoInfo> {

	private static final long serialVersionUID = 10000000L;
		
	@NotNull(groups = { Create.class, Modify.class, Reset.class }, message = "userId不能为空")
	@ApiModelProperty(value = "用户id")
	private Long userId;

	@NotNull(groups = { Create.class, Reset.class }, message = "userName不能为空")
	@ApiModelProperty(value = "用户名")
	@Length(min = 0, max = 32, groups = { Create.class, Modify.class, Reset.class }, message = "userName字符串长度需小于32")
	private String userName;

	@NotNull(groups = { Create.class, Reset.class }, message = "email不能为空")
	@ApiModelProperty(value = "email")
	@Email(message = "email格式不正确")
	@Length(min = 0, max = 32, groups = { Create.class, Modify.class, Reset.class }, message = "email字符串长度需小于32")
	private String email;

	@NotNull(groups = { Create.class, Reset.class }, message = "gender不能为空")
	@ApiModelProperty(value = "性别")
	@Pattern(regexp = "男|女", message = "输入值必须为男或女")
	@Length(min = 0, max = 4, groups = { Create.class, Modify.class, Reset.class }, message = "gender字符串长度需小于4")
	private String gender;

	@NotNull(groups = { Create.class, Reset.class }, message = "createTime不能为空")
	@ApiModelProperty(value = "创建时间")
	private Timestamp createTime;

	@NotNull(groups = { Create.class, Reset.class }, message = "lastModifyTime不能为空")
	@ApiModelProperty(value = "最后修改时间")
	private Timestamp lastModifyTime;

	@NotNull(groups = { Create.class, Reset.class }, message = "modifyTimestamp不能为空")
	@ApiModelProperty(value = "修改时间戳")
	private Long modifyTimestamp;

}