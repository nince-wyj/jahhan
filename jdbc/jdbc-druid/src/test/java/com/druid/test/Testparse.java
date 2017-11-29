package com.druid.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Column;
import com.alibaba.druid.stat.TableStat.Name;
import com.alibaba.druid.util.JdbcConstants;

public class Testparse {
	public static void main(String[] args) {

		String sql = "create table 't_user_info' ( user_id              bigint not null comment '用户id',"
				+ "is_telephone         boolean not null comment '是否手机号账号',"
				+ "user_name            varchar(32) comment '用户名',"
				+ "user_state           tinyint not null comment '用户状态（0：未激活，1：正常，2：禁用，3：被绑定）',"
				+ "mobile_phone         char(11) comment '手机号',"
				+ "email                varchar(32) comment 'email@Email',"
				+ " gender               varchar(4) comment '性别',"
				+ "   create_time          datetime not null comment '创建时间',"
				+ "   last_modify_time     datetime not null comment '最后修改时间',"
				+ "   connect_id           bigint comment '关联id'," + "   secirty_key          char(16) comment '秘钥',"
				+ "   union_id             char(128) comment 'union_id',"
				+ "   modify_timestamp     bigint not null comment '修改时间戳'," + "   primary key (user_id));"
				+ "CREATE TABLE `t_user` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,"
				+ "  `username` varchar(45) NOT NULL COMMENT '登陆用户名',"
				+ "  `password` varchar(45) NOT NULL COMMENT '登陆密码',"
				+ "  `role_id` bigint(20) NOT NULL COMMENT '角色ID',"
				+ "  `name` varchar(45) NOT NULL COMMENT '用户名字，用来显示',"
				+ "  `department_id` bigint(20) NOT NULL COMMENT '所属部门ID',"
				+ "  `access` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否允许登陆：1为允许，0为不允许，默认允许',"
				+ "  PRIMARY KEY (`id`)," + "  UNIQUE KEY `username_UNIQUE` (`username`)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表，用于用户登陆';";
		String dbType = JdbcConstants.MYSQL;

		// 格式化输出
		String result = SQLUtils.format(sql, dbType);
		System.out.println(result); // 缺省大写格式
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);

		// 解析出的独立语句的个数
		System.out.println("size is:" + stmtList.size());
		for (int i = 0; i < stmtList.size(); i++) {

			SQLStatement stmt = stmtList.get(i);

			MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
			stmt.accept(visitor);
			Map<String, String> aliasmap = visitor.getAliasMap();
			for (Iterator iterator = aliasmap.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next().toString();
				System.out.println("[ALIAS]" + key + " - " + aliasmap.get(key));
			}
			Set<Column> groupby_col = visitor.getGroupByColumns();
			//
			for (Iterator iterator = groupby_col.iterator(); iterator.hasNext();) {
				Column column = (Column) iterator.next();
				System.out.println("[GROUP]" + column.toString());
			}
			// 获取表名称
			System.out.println("table names:");
			Map<Name, TableStat> tabmap = visitor.getTables();
			for (Iterator iterator = tabmap.keySet().iterator(); iterator.hasNext();) {
				Name name = (Name) iterator.next();
				System.out.println(name.toString() + " - " + tabmap.get(name).toString());
			}
			// System.out.println("Tables : " + visitor.getCurrentTable());
			// 获取操作方法名称,依赖于表名称
			System.out.println("Manipulation : " + visitor.getTables());
			// 获取字段名称
			System.out.println("fields : " + visitor.getColumns());
		}

	}
}
