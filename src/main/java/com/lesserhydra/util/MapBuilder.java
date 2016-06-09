package com.lesserhydra.util;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class MapBuilder<K, V> {
	
	private final Map<K, V> map;
	private boolean done = false;
	
	
	public static <K, V> MapBuilder<K, V> init(Supplier<Map<K, V>> mapSupplier) {
		return new MapBuilder<>(mapSupplier.get());
	}
	
	public MapBuilder<K, V> put(K key, V value) {
		if (done) throw new IllegalStateException("Builder has already been used.");
		map.put(key, value);
		return this;
	}
	
	public Map<K, V> build() {
		if (done) throw new IllegalStateException("Builder has already been used.");
		done = true;
		return map;
	}
	
	public Map<K, V> buildImmutable() {
		if (done) throw new IllegalStateException("Builder has already been used.");
		done = true;
		return Collections.unmodifiableMap(map);
	}
	
	private MapBuilder(Map<K, V> map) {
		this.map = map;
	}
}
