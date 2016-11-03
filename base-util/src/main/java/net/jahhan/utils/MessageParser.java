package net.jahhan.utils;

import java.util.List;
import java.util.Map;

public class MessageParser {
	private StringBuilder sb = new StringBuilder();
	private List<PageFragment> frags;

	public MessageParser(List<PageFragment> frags) {
		this.frags = frags;
	}

	public String parser(Map<String, Object> params) {
		for (PageFragment frag : frags) {
			sb.append(frag.getContext(params));
		}

		return sb.toString();
	}
}