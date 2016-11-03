package net.jahhan.dblogistics.entity;

import java.io.Serializable;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Transient;

import net.jahhan.dblogistics.annotation.DocTransient;

public abstract class SuperEntity implements Serializable {
	@Transient
	@DocTransient
	private static final long serialVersionUID = -8768274118529848755L;
	@GraphId
	private Long graphId;
	private Long ooid;

	public Long getGraphId() {
		return graphId;
	}

	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	public Long getOoid() {
		return ooid;
	}

	public void setOoid(Long ooid) {
		this.ooid = ooid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || ooid == null || getClass() != o.getClass())
			return false;

		SuperEntity entity = (SuperEntity) o;

		if (!ooid.equals(entity.ooid))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (ooid == null) ? -1 : ooid.hashCode();
	}
}
