package net.jahhan.dblogistics.domain;


/**
 * A converter converts a source object of type S to a target of type T.
 * Implementations of this interface are thread-safe and can be shared.
 *
 * <p>Implementations may additionally implement {@link ConditionalConverter}.
 *
 * @author Keith Donald
 * @since 3.0
 * @param <S> The source type
 * @param <T> The target type
 */
public interface Converter<S, T> {

	/**
	 * Convert the source of type S to target type T.
	 * @param source the source object to convert, which must be an instance of S (never {@code null})
	 * @return the converted object, which must be an instance of T (potentially {@code null})
	 * @throws IllegalArgumentException if the source could not be converted to the desired target type
	 */
	T convert(S source);

}

