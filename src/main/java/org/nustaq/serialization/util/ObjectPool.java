package org.nustaq.serialization.util;

/**
 * A very simple object pool interface
 * @author carlos
 *
 * @param <T>
 */
public interface ObjectPool<T>
{
	T borrow();
	void release(T object);
	
	interface ObjectPoolFactory<T>
	{
		T create();
	}
}