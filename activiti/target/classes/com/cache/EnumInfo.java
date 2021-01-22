package com.cache;

import org.apache.poi.ss.formula.functions.T;

public interface EnumInfo<T> {
	T getValue();

	default String getValueName() {
		return ((Enum<?>) this).name();
	}
}
