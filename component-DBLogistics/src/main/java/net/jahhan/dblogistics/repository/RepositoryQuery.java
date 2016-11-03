package net.jahhan.dblogistics.repository;

/**
 * Interface for a query abstraction.
 * 
 */
public interface RepositoryQuery {

	/**
	 * Executes the {@link RepositoryQuery} with the given parameters.
	 * 
	 * @param store
	 * @param parameters
	 * @return
	 */
	public Object execute(Object[] parameters);
}