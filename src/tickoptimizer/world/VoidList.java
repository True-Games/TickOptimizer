package tickoptimizer.world;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class VoidList<T> implements List<T> {

	private final ListIterator<T> emptyIter = new ListIterator<T>() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public boolean hasPrevious() {
			return false;
		}

		@Override
		public T next() {
			throw new NoSuchElementException();
		}

		@Override
		public T previous() {
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new IllegalStateException();
		}

		@Override
		public int nextIndex() {
			return 0;
		}

		@Override
		public int previousIndex() {
			return 1;
		}

		@Override
		public void set(T e) {
			throw new IllegalStateException();
		}

		@Override
		public void add(T e) {
		}
	};

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return emptyIter;
	}

	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@Override
	public <R> R[] toArray(R[] a) {
		if (a.length > 0) {
			a[0] = null;
		}
		return a;
	}

	@Override
	public boolean add(T e) {
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return true;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
	}

	@Override
	public T get(int index) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public T set(int index, T element) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public void add(int index, T element) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public T remove(int index) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public int indexOf(Object o) {
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return emptyIter;
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new IndexOutOfBoundsException();
	}

}
