package net.jahhan.init.initer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.Script;
import net.jahhan.cache.ScriptCache;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1000)
public class RedisCacheIniter implements BootstrapInit {

	@Override
	public void execute() {
		// 启动初始化
		RedisFactory.init();
		
		Properties is = PropertiesUtil.getProperties("lua");
		Map<String, Script> scriptMap = new HashMap<>();
		Set<Object> keySet = is.keySet();
		for (Object key : keySet) {
			String property = is.getProperty(key.toString());
			Script script = new Script();
			script.setLuaScript(property);
			script.setName(key.toString());
			scriptMap.put(key.toString(), script);
		}
		ScriptCache.setScripts(scriptMap);
	}
}
