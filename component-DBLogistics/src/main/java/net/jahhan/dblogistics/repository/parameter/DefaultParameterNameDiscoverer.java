package net.jahhan.dblogistics.repository.parameter;

public class DefaultParameterNameDiscoverer extends
		PrioritizedParameterNameDiscoverer {

	public DefaultParameterNameDiscoverer() {
		addDiscoverer(new StandardReflectionParameterNameDiscoverer());
	}
}
