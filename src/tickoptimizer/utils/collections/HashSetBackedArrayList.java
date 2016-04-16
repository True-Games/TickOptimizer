package tickoptimizer.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.google.common.collect.HashMultiset;

public class HashSetBackedArrayList<E> implements List<E> {

	protected final ArrayList<E> arraylist = new ArrayList<E>();
	protected final HashMultiset<E> hashset = HashMultiset.create();

	@Override
	public int size() {
		return arraylist.size();
	}

	@Override
	public boolean isEmpty() {
		return arraylist.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return hashset.contains(o);
	}

	@Override
	public Object[] toArray() {
		return arraylist.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return arraylist.toArray(a);
	}

	@Override
	public boolean add(E e) {
		arraylist.add(e);
		hashset.add(e);
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return hashset.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		arraylist.addAll(c);
		hashset.addAll(c);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = arraylist.removeAll(c);
		if (result) {
			hashset.removeAll(c);
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = arraylist.retainAll(c);
		if (result) {
			hashset.retainAll(c);
		}
		return result;
	}

	@Override
	public void clear() {
		hashset.clear();
		arraylist.clear();
	}

	@Override
	public boolean remove(Object o) {
		arraylist.remove(o);
		hashset.remove(o);
		return true;
	}

	@Override
	public E get(int index) {
		return arraylist.get(index);
	}

	@Override
	public E remove(int index) {
		E e = arraylist.remove(index);
		hashset.remove(e);
		return e;
	}

	@Override
	public int indexOf(Object o) {
		return arraylist.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return arraylist.lastIndexOf(o);
	}

	@Override
	public E set(int index, E element) {
		E result = arraylist.set(index, element);
		hashset.add(element);
		return result;
	}

	@Override
	public void add(int index, E element) {
		arraylist.add(index, element);
		hashset.add(element);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result = arraylist.addAll(index, c);
		if (result) {
			hashset.addAll(c);
		}
		return result;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListIteratorImpl<E>(this, index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	protected static class ListIteratorImpl<E> implements ListIterator<E> {

		protected final HashSetBackedArrayList<E> list;
		protected int nextIndex;
		protected int lastRet;

		public ListIteratorImpl(HashSetBackedArrayList<E> list, int nextIndex) {
			this.list = list;
			this.nextIndex = nextIndex;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < list.size();
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex != 0;
		}

		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public int previousIndex() {
			return nextIndex - 1;
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return list.get(lastRet = nextIndex++);
		}

		@Override
		public E previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			return list.get(lastRet = --nextIndex);
		}

		@Override
		public void remove() {
			if (lastRet < 0) {
				throw new IllegalStateException();
			}
			list.remove(nextIndex = lastRet);
			lastRet = -1;
		}

		@Override
		public void set(E e) {
			if (lastRet < 0) {
				throw new IllegalStateException();
			}
			list.set(lastRet, e);
		}

		@Override
		public void add(E e) {
			list.set(nextIndex++, e);
			lastRet = -1;
		}

	}

}
