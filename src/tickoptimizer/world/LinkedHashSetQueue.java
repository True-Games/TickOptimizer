package tickoptimizer.world;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import tickoptimizer.world.collectionsclone.LinkedHashMap;

public class LinkedHashSetQueue<K> extends LinkedHashMap<K, Boolean> implements Queue<K> {

	@Override
	public boolean contains(Object o) {
		return containsKey(o);
	}

	@Override
	public Iterator<K> iterator() {
		return keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		return keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return keySet().toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object element : c) {
			if (!containsKey(element)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		for (K element : c) {
			put(element, Boolean.TRUE);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object element : c) {
			remove(element);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (c instanceof HashSet) {
			c = new HashSet<>(c);
		}
		Iterator<K> thisIterator = iterator();
		while (thisIterator.hasNext()) {
			K next = thisIterator.next();
			if (!c.contains(next)) {
				thisIterator.remove();
			}
		}
		return true;
	}

	@Override
	public boolean offer(K e) {
		return add(e);
	}

	@Override
	public boolean add(K e) {
		put(e, Boolean.TRUE);
		return true;
	}

	@Override
	public K remove() {
		return getElement(true, true);
	}

	@Override
	public K poll() {
		return getElement(true, false);
	}

	@Override
	public K element() {
		return getElement(false, true);
	}

	@Override
	public K peek() {
		return getElement(false, false);
	}

	private K getElement(boolean remove, boolean throwIfNone) {
		Iterator<K> iterator = iterator();
		if (!iterator.hasNext()) {
			if (throwIfNone) {
				throw new NoSuchElementException();
			} else {
				return null;
			}
		}
		K element = iterator.next();
		if (remove) {
			iterator.remove();
		}
		return element;
	}

}
