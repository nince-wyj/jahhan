package net.jahhan.dblogistics.domain;

import java.util.List;


/**
 * A slice of data that indicates whether there's a next or previous slice available. Allows to obtain a
 * {@link Pageable} to request a previous or next {@link Slice}.
 * 
 * @author Oliver Gierke
 * @since 1.8
 */
public interface Slice<T> extends Iterable<T> {

	/**
	 * Returns the number of the current {@link Slice}. Is always non-negative.
	 * 
	 * @return the number of the current {@link Slice}.
	 */
	int getNumber();

	/**
	 * Returns the size of the {@link Slice}.
	 * 
	 * @return the size of the {@link Slice}.
	 */
	int getSize();

	/**
	 * Returns the number of elements currently on this {@link Slice}.
	 * 
	 * @return the number of elements currently on this {@link Slice}.
	 */
	int getNumberOfElements();

	/**
	 * Returns the page content as {@link List}.
	 * 
	 * @return
	 */
	List<T> getContent();

	/**
	 * Returns whether the {@link Slice} has content at all.
	 * 
	 * @return
	 */
	boolean hasContent();

	/**
	 * Returns the sorting parameters for the {@link Slice}.
	 * 
	 * @return
	 */
	Sort getSort();

	/**
	 * Returns whether the current {@link Slice} is the first one.
	 * 
	 * @return
	 */
	boolean isFirst();

	/**
	 * Returns whether the current {@link Slice} is the last one.
	 * 
	 * @return
	 */
	boolean isLast();

	/**
	 * Returns if there is a next {@link Slice}.
	 * 
	 * @return if there is a next {@link Slice}.
	 */
	boolean hasNext();

	/**
	 * Returns if there is a previous {@link Slice}.
	 * 
	 * @return if there is a previous {@link Slice}.
	 */
	boolean hasPrevious();

	/**
	 * Returns the {@link Pageable} to request the next {@link Slice}. Can be {@literal null} in case the current
	 * {@link Slice} is already the last one. Clients should check {@link #hasNext()} before calling this method to make
	 * sure they receive a non-{@literal null} value.
	 * 
	 * @return
	 */
	Pageable nextPageable();

	/**
	 * Returns the {@link Pageable} to request the previous {@link Slice}. Can be {@literal null} in case the current
	 * {@link Slice} is already the first one. Clients should check {@link #hasPrevious()} before calling this method make
	 * sure receive a non-{@literal null} value.
	 * 
	 * @return
	 */
	Pageable previousPageable();

	/**
	 * Returns a new {@link Slice} with the content of the current one mapped by the given {@link Converter}.
	 * 
	 * @param converter must not be {@literal null}.
	 * @return a new {@link Slice} with the content of the current one mapped by the given {@link Converter}.
	 * @since 1.10
	 */
	<S> Slice<S> map(Converter<? super T, ? extends S> converter);
}
