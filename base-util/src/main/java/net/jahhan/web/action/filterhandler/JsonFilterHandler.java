package net.jahhan.web.action.filterhandler;

import net.jahhan.web.action.FilterHandler;

/**
 * @author nince
 */
public class JsonFilterHandler implements FilterHandler {

    @Override
    public String doFilter(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 20);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            case '\"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '/':
                sb.append("\\/");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
