package net.jahhan.common.extension.utils;

import net.jahhan.common.extension.exception.JahhanException;

public abstract class Assert {
	public static void isTrue(boolean expression, String message, int httpStatus, int code, Throwable exception) {
		if (!expression) {
			JahhanException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isTrue(boolean expression, String message, int code, Throwable exception) {
		if (!expression) {
			JahhanException.throwException(code, message, exception);
		}
	}

	public static void isTrue(boolean expression, String message, int httpStatus, int code) {
		if (!expression) {
			JahhanException.throwException(httpStatus, code, message);
		}
	}

	public static void isTrue(boolean expression, String message, int code) {
		if (!expression) {
			JahhanException.throwException(code, message);
		}
	}

	public static void isTrue(boolean expression, int code) {
		isTrue(expression, "[Assertion failed] - this expression must be true", code);
	}

	public static void isFalse(boolean expression, String message, int httpStatus, int code, Throwable exception) {
		if (expression) {
			JahhanException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isFalse(boolean expression, String message, int code, Throwable exception) {
		if (expression) {
			JahhanException.throwException(code, message, exception);
		}
	}

	public static void isFalse(boolean expression, String message, int httpStatus, int code) {
		if (expression) {
			JahhanException.throwException(httpStatus, code, message);
		}
	}

	public static void isFalse(boolean expression, String message, int code) {
		if (expression) {
			JahhanException.throwException(code, message);
		}
	}

	public static void isFalse(boolean expression, int code) {
		isFalse(expression, "[Assertion failed] - this expression must be true", code);
	}

	public static void isNull(Object object, String message, int httpStatus, int code, Throwable exception) {
		if (object != null) {
			JahhanException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isNull(Object object, String message, int code, Throwable exception) {
		if (object != null) {
			JahhanException.throwException(code, message, exception);
		}
	}

	public static void isNull(Object object, String message, int httpStatus, int code) {
		if (object != null) {
			JahhanException.throwException(httpStatus, code, message);
		}
	}

	public static void isNull(Object object, String message, int code) {
		if (object != null) {
			JahhanException.throwException(code, message);
		}
	}

	public static void isNull(Object object, int code) {
		isNull(object, "[Assertion failed] - the object argument must be null", code);
	}

	public static void notNull(Object object, String message, int httpStatus, int code, Throwable exception) {
		if (object == null) {
			JahhanException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void notNull(Object object, String message, int code, Throwable exception) {
		if (object == null) {
			JahhanException.throwException(code, message, exception);
		}
	}

	public static void notNull(Object object, String message, int httpStatus, int code) {
		if (object == null) {
			JahhanException.throwException(httpStatus, code, message);
		}
	}

	public static void notNull(Object object, String message, int code) {
		if (object == null) {
			JahhanException.throwException(code, message);
		}
	}

	public static void notNull(Object object, int code) {
		notNull(object, "[Assertion failed] - the object argument must be null", code);
	}

	public static void notNullString(String string, String message, int httpStatus, int code, Throwable exception) {
		if (string == null || string.equals("")) {
			JahhanException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void notNullString(String string, String message, int code, Throwable exception) {
		if (string == null || string.equals("")) {
			JahhanException.throwException(code, message, exception);
		}
	}

	public static void notNullString(String string, String message, int httpStatus, int code) {
		if (string == null || string.equals("")) {
			JahhanException.throwException(httpStatus, code, message);
		}
	}

	public static void notNullString(String string, String message, int code) {
		if (string == null || string.equals("")) {
			JahhanException.throwException(code, message);
		}
	}

	public static void notNullString(String object, int code) {
		notNullString(object, "[Assertion failed] - this string is required; it must not be null", code);
	}

	public static void notEmpty(Object[] array, String message, int httpStatus, int code, Throwable exception) {
		if (array == null || array.length == 0) {
			JahhanException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void notEmpty(Object[] array, String message, int code, Throwable exception) {
		if (array == null || array.length == 0) {
			JahhanException.throwException(code, message, exception);
		}
	}

	public static void notEmpty(Object[] array, String message, int httpStatus, int code) {
		if (array == null || array.length == 0) {
			JahhanException.throwException(httpStatus, code, message);
		}
	}

	public static void notEmpty(Object[] array, String message, int code) {
		if (array == null || array.length == 0) {
			JahhanException.throwException(code, message);
		}
	}

	public static void notEmpty(Object[] array, int code) {
		notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element", code);
	}

	public static void noNullElements(Object[] array, String message, int httpStatus, int code, Throwable exception) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					JahhanException.throwException(httpStatus, code, message, exception);
				}
			}
		}
	}

	public static void noNullElements(Object[] array, String message, int code, Throwable exception) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					JahhanException.throwException(code, message, exception);
				}
			}
		}
	}

	public static void noNullElements(Object[] array, String message, int httpStatus, int code) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					JahhanException.throwException(httpStatus, code, message);
				}
			}
		}
	}

	public static void noNullElements(Object[] array, String message, int code) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					JahhanException.throwException(code, message);
				}
			}
		}
	}

	public static void noNullElements(Object[] array, int code) {
		noNullElements(array, "[Assertion failed] - this array must not contain any null elements", code);
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, String message, int httpStatus, int code,
			Throwable exception) {
		notNull(superType, "Type to check against must not be null", code);
		if (subType == null || !superType.isAssignableFrom(subType)) {
			JahhanException.throwException(httpStatus, code, message, exception);
		}
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, String message, int code,
			Throwable exception) {
		notNull(superType, "Type to check against must not be null", code);
		if (subType == null || !superType.isAssignableFrom(subType)) {
			JahhanException.throwException(code, message, exception);
		}
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, String message, int httpStatus, int code) {
		notNull(superType, "Type to check against must not be null", code);
		if (subType == null || !superType.isAssignableFrom(subType)) {
			JahhanException.throwException(httpStatus, code, message);
		}
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, String message, int code) {
		notNull(superType, "Type to check against must not be null", code);
		if (subType == null || !superType.isAssignableFrom(subType)) {
			JahhanException.throwException(code, message + subType + " is not assignable to " + superType);
		}
	}

	public static void isAssignable(Class<?> superType, Class<?> subType, int code) {
		isAssignable(superType, subType, "", code);
	}
}
