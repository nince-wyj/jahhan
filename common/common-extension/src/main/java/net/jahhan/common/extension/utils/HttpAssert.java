package net.jahhan.common.extension.utils;

import net.jahhan.common.extension.exception.HttpException;

public abstract class HttpAssert extends Assert{
	public static void isTrue(boolean expression, String message, int httpStatus, String code, Throwable exception) {
		if (!expression) {
			HttpException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isTrue(boolean expression, String message, int httpStatus, String code) {
		if (!expression) {
			HttpException.throwException(httpStatus, code, message);
		}
	}

	public static void isTrue(boolean expression, String code) {
		isTrue(expression, "[Assertion failed] - this expression must be true", code);
	}

	public static void isFalse(boolean expression, String message, int httpStatus, String code, Throwable exception) {
		if (expression) {
			HttpException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isFalse(boolean expression, String message, int httpStatus, String code) {
		if (expression) {
			HttpException.throwException(httpStatus, code, message);
		}
	}

	public static void isFalse(boolean expression, String code) {
		isFalse(expression, "[Assertion failed] - this expression must be true", code);
	}

	public static void isNull(Object object, String message, int httpStatus, String code, Throwable exception) {
		if (object != null) {
			HttpException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isNull(Object object, String message, int httpStatus, String code) {
		if (object != null) {
			HttpException.throwException(httpStatus, code, message);
		}
	}

	public static void isNull(Object object, String code) {
		isNull(object, "[Assertion failed] - the object argument must be null", code);
	}

	public static void notNull(Object object, String message, int httpStatus, String code, Throwable exception) {
		if (object == null) {
			HttpException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void notNull(Object object, String message, int httpStatus, String code) {
		if (object == null) {
			HttpException.throwException(httpStatus, code, message);
		}
	}

	public static void notNull(Object object, String code) {
		notNull(object, "[Assertion failed] - the object argument must be null", code);
	}

	public static void notNullString(String string, String message, int httpStatus, String code, Throwable exception) {
		if (string == null || string.equals("")) {
			HttpException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void notNullString(String string, String message, int httpStatus, String code) {
		if (string == null || string.equals("")) {
			HttpException.throwException(httpStatus, code, message);
		}
	}

	public static void notNullString(String object, String code) {
		notNullString(object, "[Assertion failed] - this string is required; it must not be null", code);
	}

	public static void notEmpty(Object[] array, String message, int httpStatus, String code, Throwable exception) {
		if (array == null || array.length == 0) {
			HttpException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void notEmpty(Object[] array, String message, int httpStatus, String code) {
		if (array == null || array.length == 0) {
			HttpException.throwException(httpStatus, code, message);
		}
	}

	public static void notEmpty(Object[] array, String code) {
		notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element", code);
	}

	public static void noNullElements(Object[] array, String message, int httpStatus, String code, Throwable exception) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					HttpException.throwException(httpStatus, code, message, exception);
				}
			}
		}
	}

	public static void noNullElements(Object[] array, String message, int httpStatus, String code) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					HttpException.throwException(httpStatus, code, message);
				}
			}
		}
	}

	public static void noNullElements(Object[] array, String code) {
		noNullElements(array, "[Assertion failed] - this array must not contain any null elements", code);
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, String message, int httpStatus, String code,
			Throwable exception) {
		notNull(superType, "Type to check against must not be null", code);
		if (subType == null || !superType.isAssignableFrom(subType)) {
			HttpException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, String message, int httpStatus, String code) {
		notNull(superType, "Type to check against must not be null", code);
		if (subType == null || !superType.isAssignableFrom(subType)) {
			HttpException.throwException(httpStatus, code, message);
		}
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, String code) {
		isAssignable(superType, subType, "", code);
	}
}
