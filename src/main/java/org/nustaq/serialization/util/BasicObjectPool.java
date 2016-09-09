package org.nustaq.serialization.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Basic unbounded object pool implementation.
 * @author carlos
 *
 * @param <T>
 */
public class BasicObjectPool<T> implements ObjectPool<T>
{
	protected final ObjectPoolFactory<T> factory;
	protected final Deque<T> poolObject;
	
	public BasicObjectPool(ObjectPoolFactory<T> factory, int initialCapacity)
	{
		if (factory == null) throw new NullPointerException();
		if (initialCapacity <= 0) initialCapacity = 10;
		
		this.factory = factory;
		this.poolObject = new ArrayDeque<>(initialCapacity);
		
		for (int i = 0; i < initialCapacity; i++)
		{
			poolObject.offer(factory.create());
		}
	}
	
	@Override
	public T borrow()
	{
		final T borrowedObject = poolObject.poll();
		return borrowedObject != null ? borrowedObject : factory.create();
	}

	@Override
	public void release(T borrowedObject)
	{
		poolObject.offer(borrowedObject);
	}
}