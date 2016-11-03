package net.jahhan.constant.enumeration;

/**
 * @author nince
 */
public enum CryptEnum {
	AES, LOGIN {
		@Override
		public String toString() {
			return "1";
		}

		@SuppressWarnings("unused")
		public String desc() {
			return "AES加密";
		}
	},
	MD5, SIGN {
		@Override
		public String toString() {
			return "2";
		}

		@SuppressWarnings("unused")
		public String desc() {
			return "签名验证";
		}
	},
	PLAIN {
		@Override
		public String toString() {
			return "0";
		}

		@SuppressWarnings("unused")
		public String desc() {
			return "明文";
		}
	}
}
