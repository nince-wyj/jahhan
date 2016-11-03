package net.jahhan.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.SysConfiguration;

@Named
public class HtmlPageLoader {
	private static Pattern pattern = Pattern.compile("#\\w+#");
	private static Logger logger = LoggerFactory.getLogger(HtmlPageLoader.class);
	private Map<String, List<PageFragment>> pageCache = new HashMap<>();

	public String load(String filePath, Map params) {
		return parse(loadPageContent(filePath), params, false);
	}

	private String loadPageContent(String filePath) {
		return FileUtil.readFileToString(filePath, "UTF-8");
	}

	public void clear() {
		pageCache.clear();
	}

	private String parse(String srcHtml, Map<String, Object> params, boolean nullToEmpty) {
		StringBuilder sb = new StringBuilder();

		List<PageFragment> frags = pageCache.get(srcHtml);

		if (SysConfiguration.getIsDebug() || frags == null) {
			logger.info("重新加载网页");
			frags = new ArrayList<>();
			String group = null;

			int startPos = 0;
			Matcher matcher = pattern.matcher(srcHtml);
			while (matcher.find()) {
				group = matcher.group();
				frags.add(new StaticPageFragment(srcHtml.substring(startPos, matcher.start())));
				frags.add(new DynamicPageFragment(group.substring(1, group.length() - 1).trim(), nullToEmpty));
				startPos = matcher.end();
			}
			frags.add(new StaticPageFragment(srcHtml.substring(startPos)));
			pageCache.put(srcHtml, frags);
		} else {
			logger.info("从缓存中加载网页");
		}

		for (PageFragment frag : frags) {
			sb.append(frag.getContext(params));
		}

		return sb.toString();
	}

}

interface PageFragment {
	String getContext(Map<String, Object> params);
}

class StaticPageFragment implements PageFragment {
	private String context;

	public StaticPageFragment(String context) {
		this.context = context;
	}

	@Override
	public String getContext(Map<String, Object> params) {
		return context;
	}
}

class DynamicPageFragment implements PageFragment {
	private String key;
	private boolean nullToEmpty;

	public DynamicPageFragment(String key, boolean nullToEmpty) {
		this.key = key;
		this.nullToEmpty = nullToEmpty;
	}

	@Override
	public String getContext(Map<String, Object> params) {
		String value = String.valueOf(params.get(key));
		return (value == null && nullToEmpty) ? "" : value;
	}
}
