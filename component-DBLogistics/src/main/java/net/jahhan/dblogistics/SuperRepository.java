package net.jahhan.dblogistics;

import net.jahhan.dblogistics.domain.Page;
import net.jahhan.dblogistics.domain.Pageable;
import net.jahhan.dblogistics.domain.Sort;
import net.jahhan.dblogistics.entity.SuperEntity;

public interface SuperRepository<T extends SuperEntity> {

	public T save(T entity);

	public Iterable<T> save(Iterable<T> entities);

	public void delete(Long ooid);

	public void delete(T entity);

	public void delete(Iterable<T> entities);

	public void deleteAll();

	public void update(T entity);

	public void updateNeo(T entity);

	public void updateDoc(Long ooid, T entity);

	public void resetDoc(Long ooid, T entity);

	public T findOne(Long ooid);

	public T findOne(Long ooid, int depth);

	public Iterable<T> findByOoids(Iterable<Long> ooids);

	public Iterable<T> findByOoids(Iterable<Long> ooids, int depth);

	public Iterable<T> findByOoids(Iterable<Long> ooids, Sort sort);

	public Iterable<T> findByOoids(Iterable<Long> ooids, Sort sort, int depth);

	public Page<T> findAll(Pageable pageable);

	public Page<T> findAll(Pageable pageable, int depth);

	public boolean exists(Long ooid);

	public long count();

}
