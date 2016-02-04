package tickoptimizer.world.collectionsclone;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Copied from java.util.LinkedHashMap
 */
public class LinkedHashMap<K, V> extends HashMap<K, V> {

	static class Entry<K, V> extends HashMap.Node<K, V> {
		Entry<K, V> before, after;

		Entry(int hash, K key, V value, Node<K, V> next) {
			super(hash, key, value, next);
		}
	}

	/**
	 * The head (eldest) of the doubly linked list.
	 */
	transient LinkedHashMap.Entry<K, V> head;

	/**
	 * The tail (youngest) of the doubly linked list.
	 */
	transient LinkedHashMap.Entry<K, V> tail;

	/**
	 * The iteration ordering method for this linked hash map: <tt>true</tt> for
	 * access-order, <tt>false</tt> for insertion-order.
	 *
	 * @serial
	 */
	final boolean accessOrder;

	// internal utilities

	// link at the end of list
	private void linkNodeLast(LinkedHashMap.Entry<K, V> p) {
		LinkedHashMap.Entry<K, V> last = tail;
		tail = p;
		if (last == null)
			head = p;
		else {
			p.before = last;
			last.after = p;
		}
	}

	// apply src's links to dst
	private void transferLinks(LinkedHashMap.Entry<K, V> src, LinkedHashMap.Entry<K, V> dst) {
		LinkedHashMap.Entry<K, V> b = dst.before = src.before;
		LinkedHashMap.Entry<K, V> a = dst.after = src.after;
		if (b == null)
			head = dst;
		else
			b.after = dst;
		if (a == null)
			tail = dst;
		else
			a.before = dst;
	}

	// overrides of HashMap hook methods

	void reinitialize() {
		super.reinitialize();
		head = tail = null;
	}

	Node<K, V> newNode(int hash, K key, V value, Node<K, V> e) {
		LinkedHashMap.Entry<K, V> p = new LinkedHashMap.Entry<K, V>(hash, key, value, e);
		linkNodeLast(p);
		return p;
	}

	Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
		LinkedHashMap.Entry<K, V> q = (LinkedHashMap.Entry<K, V>) p;
		LinkedHashMap.Entry<K, V> t = new LinkedHashMap.Entry<K, V>(q.hash, q.key, q.value, next);
		transferLinks(q, t);
		return t;
	}

	TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
		TreeNode<K, V> p = new TreeNode<K, V>(hash, key, value, next);
		linkNodeLast(p);
		return p;
	}

	TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
		LinkedHashMap.Entry<K, V> q = (LinkedHashMap.Entry<K, V>) p;
		TreeNode<K, V> t = new TreeNode<K, V>(q.hash, q.key, q.value, next);
		transferLinks(q, t);
		return t;
	}

	void afterNodeRemoval(Node<K, V> e) { // unlink
		LinkedHashMap.Entry<K, V> p = (LinkedHashMap.Entry<K, V>) e, b = p.before, a = p.after;
		p.before = p.after = null;
		if (b == null)
			head = a;
		else
			b.after = a;
		if (a == null)
			tail = b;
		else
			a.before = b;
	}

	void afterNodeInsertion(boolean evict) { // possibly remove eldest
		LinkedHashMap.Entry<K, V> first;
		if (evict && (first = head) != null && removeEldestEntry(first)) {
			K key = first.key;
			removeNode(hash(key), key, null, false, true);
		}
	}

	void afterNodeAccess(Node<K, V> e) { // move node to last
		LinkedHashMap.Entry<K, V> last;
		if (accessOrder && (last = tail) != e) {
			LinkedHashMap.Entry<K, V> p = (LinkedHashMap.Entry<K, V>) e, b = p.before, a = p.after;
			p.after = null;
			if (b == null)
				head = a;
			else
				b.after = a;
			if (a != null)
				a.before = b;
			else
				last = b;
			if (last == null)
				head = p;
			else {
				p.before = last;
				last.after = p;
			}
			tail = p;
			++modCount;
		}
	}

	void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
		for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
			s.writeObject(e.key);
			s.writeObject(e.value);
		}
	}

	/**
	 * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
	 * with the specified initial capacity and load factor.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @param loadFactor
	 *            the load factor
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative or the load factor is
	 *             nonpositive
	 */
	public LinkedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		accessOrder = false;
	}

	/**
	 * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
	 * with the specified initial capacity and a default load factor (0.75).
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative
	 */
	public LinkedHashMap(int initialCapacity) {
		super(initialCapacity);
		accessOrder = false;
	}

	/**
	 * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
	 * with the default initial capacity (16) and load factor (0.75).
	 */
	public LinkedHashMap() {
		super();
		accessOrder = false;
	}

	/**
	 * Constructs an empty <tt>LinkedHashMap</tt> instance with the specified
	 * initial capacity, load factor and ordering mode.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @param loadFactor
	 *            the load factor
	 * @param accessOrder
	 *            the ordering mode - <tt>true</tt> for access-order,
	 *            <tt>false</tt> for insertion-order
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative or the load factor is
	 *             nonpositive
	 */
	public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor);
		this.accessOrder = accessOrder;
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the specified
	 * value.
	 *
	 * @param value
	 *            value whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map maps one or more keys to the specified
	 *         value
	 */
	public boolean containsValue(Object value) {
		for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
			V v = e.value;
			if (v == value || (value != null && value.equals(v)))
				return true;
		}
		return false;
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null}
	 * if this map contains no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key {@code k} to a
	 * value {@code v} such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise it returns
	 * {@code null}. (There can be at most one such mapping.)
	 *
	 * <p>
	 * A return value of {@code null} does not <i>necessarily</i> indicate that
	 * the map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to {@code null}. The {@link #containsKey
	 * containsKey} operation may be used to distinguish these two cases.
	 */
	public V get(Object key) {
		Node<K, V> e;
		if ((e = getNode(hash(key), key)) == null)
			return null;
		if (accessOrder)
			afterNodeAccess(e);
		return e.value;
	}

	/**
	 * {@inheritDoc}
	 */
	public V getOrDefault(Object key, V defaultValue) {
		Node<K, V> e;
		if ((e = getNode(hash(key), key)) == null)
			return defaultValue;
		if (accessOrder)
			afterNodeAccess(e);
		return e.value;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		super.clear();
		head = tail = null;
	}

	/**
	 * Returns <tt>true</tt> if this map should remove its eldest entry. This
	 * method is invoked by <tt>put</tt> and <tt>putAll</tt> after inserting a
	 * new entry into the map. It provides the implementor with the opportunity
	 * to remove the eldest entry each time a new one is added. This is useful
	 * if the map represents a cache: it allows the map to reduce memory
	 * consumption by deleting stale entries.
	 *
	 * <p>
	 * Sample use: this override will allow the map to grow up to 100 entries
	 * and then delete the eldest entry each time a new entry is added,
	 * maintaining a steady state of 100 entries.
	 * 
	 * <pre>
	 * private static final int MAX_ENTRIES = 100;
	 *
	 * protected boolean removeEldestEntry(Map.Entry eldest) {
	 * 	return size() &gt; MAX_ENTRIES;
	 * }
	 * </pre>
	 *
	 * <p>
	 * This method typically does not modify the map in any way, instead
	 * allowing the map to modify itself as directed by its return value. It
	 * <i>is</i> permitted for this method to modify the map directly, but if it
	 * does so, it <i>must</i> return <tt>false</tt> (indicating that the map
	 * should not attempt any further modification). The effects of returning
	 * <tt>true</tt> after modifying the map from within this method are
	 * unspecified.
	 *
	 * <p>
	 * This implementation merely returns <tt>false</tt> (so that this map acts
	 * like a normal map - the eldest element is never removed).
	 *
	 * @param eldest
	 *            The least recently inserted entry in the map, or if this is an
	 *            access-ordered map, the least recently accessed entry. This is
	 *            the entry that will be removed it this method returns
	 *            <tt>true</tt>. If the map was empty prior to the <tt>put</tt>
	 *            or <tt>putAll</tt> invocation resulting in this invocation,
	 *            this will be the entry that was just inserted; in other words,
	 *            if the map contains a single entry, the eldest entry is also
	 *            the newest.
	 * @return <tt>true</tt> if the eldest entry should be removed from the map;
	 *         <tt>false</tt> if it should be retained.
	 */
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return false;
	}

	/**
	 * Returns a {@link Set} view of the keys contained in this map. The set is
	 * backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa. If the map is modified while an iteration over the set is in
	 * progress (except through the iterator's own <tt>remove</tt> operation),
	 * the results of the iteration are undefined. The set supports element
	 * removal, which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support
	 * the <tt>add</tt> or <tt>addAll</tt> operations. Its {@link Spliterator}
	 * typically provides faster sequential performance but much poorer parallel
	 * performance than that of {@code HashMap}.
	 *
	 * @return a set view of the keys contained in this map
	 */
	public Set<K> keySet() {
		Set<K> ks;
		return (ks = keySet) == null ? (keySet = new LinkedKeySet()) : ks;
	}

	final class LinkedKeySet extends AbstractSet<K> {
		public final int size() {
			return size;
		}

		public final void clear() {
			LinkedHashMap.this.clear();
		}

		public final Iterator<K> iterator() {
			return new LinkedKeyIterator();
		}

		public final boolean contains(Object o) {
			return containsKey(o);
		}

		public final boolean remove(Object key) {
			return removeNode(hash(key), key, null, false, true) != null;
		}

		public final Spliterator<K> spliterator() {
			return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.DISTINCT);
		}

		public final void forEach(Consumer<? super K> action) {
			if (action == null)
				throw new NullPointerException();
			int mc = modCount;
			for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
				action.accept(e.key);
			if (modCount != mc)
				throw new ConcurrentModificationException();
		}
	}

	/**
	 * Returns a {@link Collection} view of the values contained in this map.
	 * The collection is backed by the map, so changes to the map are reflected
	 * in the collection, and vice-versa. If the map is modified while an
	 * iteration over the collection is in progress (except through the
	 * iterator's own <tt>remove</tt> operation), the results of the iteration
	 * are undefined. The collection supports element removal, which removes the
	 * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
	 * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
	 * <tt>addAll</tt> operations. Its {@link Spliterator} typically provides
	 * faster sequential performance but much poorer parallel performance than
	 * that of {@code HashMap}.
	 *
	 * @return a view of the values contained in this map
	 */
	public Collection<V> values() {
		Collection<V> vs;
		return (vs = values) == null ? (values = new LinkedValues()) : vs;
	}

	final class LinkedValues extends AbstractCollection<V> {
		public final int size() {
			return size;
		}

		public final void clear() {
			LinkedHashMap.this.clear();
		}

		public final Iterator<V> iterator() {
			return new LinkedValueIterator();
		}

		public final boolean contains(Object o) {
			return containsValue(o);
		}

		public final Spliterator<V> spliterator() {
			return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED);
		}

		public final void forEach(Consumer<? super V> action) {
			if (action == null)
				throw new NullPointerException();
			int mc = modCount;
			for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
				action.accept(e.value);
			if (modCount != mc)
				throw new ConcurrentModificationException();
		}
	}

	/**
	 * Returns a {@link Set} view of the mappings contained in this map. The set
	 * is backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa. If the map is modified while an iteration over the set is in
	 * progress (except through the iterator's own <tt>remove</tt> operation, or
	 * through the <tt>setValue</tt> operation on a map entry returned by the
	 * iterator) the results of the iteration are undefined. The set supports
	 * element removal, which removes the corresponding mapping from the map,
	 * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>
	 * , <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support
	 * the <tt>add</tt> or <tt>addAll</tt> operations. Its {@link Spliterator}
	 * typically provides faster sequential performance but much poorer parallel
	 * performance than that of {@code HashMap}.
	 *
	 * @return a set view of the mappings contained in this map
	 */
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> es;
		return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
	}

	final class LinkedEntrySet extends AbstractSet<Map.Entry<K, V>> {
		public final int size() {
			return size;
		}

		public final void clear() {
			LinkedHashMap.this.clear();
		}

		public final Iterator<Map.Entry<K, V>> iterator() {
			return new LinkedEntryIterator();
		}

		public final boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			Object key = e.getKey();
			Node<K, V> candidate = getNode(hash(key), key);
			return candidate != null && candidate.equals(e);
		}

		public final boolean remove(Object o) {
			if (o instanceof Map.Entry) {
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				Object key = e.getKey();
				Object value = e.getValue();
				return removeNode(hash(key), key, value, true, true) != null;
			}
			return false;
		}

		public final Spliterator<Map.Entry<K, V>> spliterator() {
			return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.ORDERED | Spliterator.DISTINCT);
		}

		public final void forEach(Consumer<? super Map.Entry<K, V>> action) {
			if (action == null)
				throw new NullPointerException();
			int mc = modCount;
			for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
				action.accept(e);
			if (modCount != mc)
				throw new ConcurrentModificationException();
		}
	}

	// Map overrides

	public void forEach(BiConsumer<? super K, ? super V> action) {
		if (action == null)
			throw new NullPointerException();
		int mc = modCount;
		for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
			action.accept(e.key, e.value);
		if (modCount != mc)
			throw new ConcurrentModificationException();
	}

	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		if (function == null)
			throw new NullPointerException();
		int mc = modCount;
		for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
			e.value = function.apply(e.key, e.value);
		if (modCount != mc)
			throw new ConcurrentModificationException();
	}

	// Iterators

	abstract class LinkedHashIterator {
		LinkedHashMap.Entry<K, V> next;
		LinkedHashMap.Entry<K, V> current;
		int expectedModCount;

		LinkedHashIterator() {
			next = head;
			expectedModCount = modCount;
			current = null;
		}

		public final boolean hasNext() {
			return next != null;
		}

		final LinkedHashMap.Entry<K, V> nextNode() {
			LinkedHashMap.Entry<K, V> e = next;
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			if (e == null)
				throw new NoSuchElementException();
			current = e;
			next = e.after;
			return e;
		}

		public final void remove() {
			Node<K, V> p = current;
			if (p == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			current = null;
			K key = p.key;
			removeNode(hash(key), key, null, false, false);
			expectedModCount = modCount;
		}
	}

	final class LinkedKeyIterator extends LinkedHashIterator implements Iterator<K> {
		public final K next() {
			return nextNode().getKey();
		}
	}

	final class LinkedValueIterator extends LinkedHashIterator implements Iterator<V> {
		public final V next() {
			return nextNode().value;
		}
	}

	final class LinkedEntryIterator extends LinkedHashIterator implements Iterator<Map.Entry<K, V>> {
		public final Map.Entry<K, V> next() {
			return nextNode();
		}
	}

}
