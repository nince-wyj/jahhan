package net.jahhan.utils;

/**
 * 64进制和10进制的转换类
 * 
 * @author nince
 * 
 */
public class RadixTransform {
	final static char[] digits = { '+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
			'v', 'w', 'x', 'y', 'z' };

	/**
	 * 把long类型转换成64进制
	 * 
	 * @param number
	 * @return
	 */
	public static String transformLongTo64(long number) {
		int charPos = 11;
		char[] buf = new char[charPos];
		do {
			buf[--charPos] = digits[(int) (number & 077)];
			number >>>= 6;
		} while (number != 0);
		return new String(buf);
	}

	/**
	 * 把64进制的字符串转换成long类型
	 * 
	 * @param decompStr
	 * @return
	 */
	public static long transform64ToLong(String decompStr) {
		long result = 0;
		for (int i = decompStr.length() - 1; i >= 0; i--) {
			result += getCharIndexNum(decompStr.charAt(i)) << 6 * (decompStr.length() - 1 - i);
		}
		return result;
	}

	/**
	 * 
	 * @param ch
	 * @return
	 */
	private static long getCharIndexNum(char ch) {
		int num = ((int) ch);
		if (num >= 48 && num <= 57) {
			return num - 46;
		} else if (num >= 97 && num <= 122) {
			return num - 59;
		} else if (num >= 65 && num <= 90) {
			return num - 53;
		} else if (num == 43) {
			return 0;
		} else if (num == 45) {
			return 1;
		}
		return 0;
	}

}