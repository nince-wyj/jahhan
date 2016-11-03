package net.jahhan.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicStringParser {
	private final static Pattern pattern = Pattern.compile("#\\w+#");

	public static MessageParser buildParser(String patternStr, boolean nullToEmpty) {
		List<PageFragment> frags = new ArrayList<>();
		frags = new ArrayList<>();
		String group = null;

		int startPos = 0;
		Matcher matcher = pattern.matcher(patternStr);
		while (matcher.find()) {
			group = matcher.group();
			frags.add(new StaticPageFragment(patternStr.substring(startPos, matcher.start())));
			frags.add(new DynamicPageFragment(group.substring(1, group.length() - 1).trim(), false));
			startPos = matcher.end();
		}
		frags.add(new StaticPageFragment(patternStr.substring(startPos)));
		return new MessageParser(frags);
	}

	public static String parse(String patternStr, Map<String, Object> params, boolean nullToEmpty) {
		MessageParser parser = buildParser(patternStr, nullToEmpty);
		return parser.parser(params);
	}

	public static String parse(String patternStr, Map<String, Object> params) {
		return parse(patternStr, params, false);
	}
}
