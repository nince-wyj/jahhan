package net.jahhan.dblogistics.repository.parameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * ParameterNameDiscoverer implementation that tries several ParameterNameDiscoverers
 * in succession. Those added first in the {@code addDiscoverer} method have
 * highest priority. If one returns {@code null}, the next will be tried.
 *
 * <p>The default behavior is always to return {@code null}
 * if no discoverer matches.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class PrioritizedParameterNameDiscoverer implements ParameterNameDiscoverer {
	
	private final List<ParameterNameDiscoverer> parameterNameDiscoverers =
			new LinkedList<ParameterNameDiscoverer>();


	/**
	 * Add a further ParameterNameDiscoverer to the list of discoverers
	 * that this PrioritizedParameterNameDiscoverer checks.
	 */
	public void addDiscoverer(ParameterNameDiscoverer pnd) {
		this.parameterNameDiscoverers.add(pnd);
	}


	@Override
	public String[] getParameterNames(Method method) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(method);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public String[] getParameterNames(Constructor<?> ctor) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(ctor);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
