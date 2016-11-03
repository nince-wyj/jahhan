package net.jahhan.dblogistics.domain;


/**
 * A page is a sublist of a list of objects. It allows gain information about the position of it in the containing
 * entire list.
 * 
 * @param <T>
 * @author Oliver Gierke
 */
public interface Page<T> extends Slice<T> {

	/**
	 * Returns the number of total pages.
	 * 
	 * @return the number of toral pages
	 */
	int getTotalPages();

	/**
	 * Returns the total amount of elements.
	 * 
	 * @return the total amount of elements
	 */
	long getTotalElements();

	/**
	 * Returns a new {@link Page} with the content of the current one mapped by the given {@link Converter}.
	 * 
	 * @param converter must not be {@literal null}.
	 * @return a new {@link Page} with the content of the current one mapped by the given {@link Converter}.
	 * @since 1.10
	 */
	<S> Page<S> map(Converter<? super T, ? extends S> converter);
}
