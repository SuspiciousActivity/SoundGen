package me.SoundCreator.util;

import java.util.function.Predicate;

public class FromToPredicate<T> implements Predicate<T> {

	private int cnt;
	private final int from;
	private final int to;

	public FromToPredicate(int from, int to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean test(T obj) {
		boolean check = cnt <= from || cnt >= to;
		cnt++;
		return check;
	}

}
