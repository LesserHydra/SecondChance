package com.lesserhydra.testing;


public class Capsule<T> {
	
	private T item;
	
	public Object set(T newItem) {
		item = newItem;
		return null;
	}
	
	public T get() {
		return item;
	}
}
