package tickoptimizer.usercache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class DualCache<PK, SK, V> {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReadLock rlock = lock.readLock();
	private final WriteLock wlock = lock.writeLock();

	private final long validtime;
	private final int limit;

	private final Map<PK, DualCacheEntry<PK, SK, V>> primary;
	private final Map<SK, DualCacheEntry<PK, SK, V>> secondary;

	public DualCache(int limit, long validtime) {
		this.limit = limit;
		this.validtime = validtime;
		int size = (int) (limit * 1.5);
		primary = new LinkedHashMap<>(size, 0.75F, true);
		secondary = new HashMap<>(size);
	}

	public V getByPrimaryKey(Object primaryKey) {
		rlock.lock();
		try {
			DualCacheEntry<PK, SK, V> entry = primary.get(primaryKey);
			return entry != null ? entry.value : null;
		} finally {
			rlock.unlock();
		}
	}

	public V getBySecondaryKey(Object secondaryKey) {
		DualCacheEntry<PK, SK, V> entry = null;
		rlock.lock();
		try {
			entry = secondary.get(secondaryKey);
		} finally {
			rlock.unlock();
		}
		if (entry != null) {
			if (System.currentTimeMillis() > entry.expiretime) {
				remove(entry.pk);
				return null;
			} else {
				getByPrimaryKey(entry.pk);
				return entry.value;
			}
		} else {
			return null;
		}
	}

	public void put(PK primaryKey, SK secondaryKey, V value) {
		wlock.lock();
		try {
			DualCacheEntry<PK, SK, V> oldEntry = primary.get(primaryKey);
			if (oldEntry != null) {
				secondary.remove(oldEntry.sk);
			}
			DualCacheEntry<PK, SK, V> newEntry = new DualCacheEntry<>(primaryKey, secondaryKey, value, System.currentTimeMillis() + validtime);
			primary.put(primaryKey, newEntry);
			secondary.put(secondaryKey, newEntry);
			if (primary.size() > limit) {
				remove(primary.keySet().iterator().next());
			}
		} finally {
			wlock.unlock();
		}
	}

	public void remove(Object primaryKey) {
		wlock.lock();
		try {
			DualCacheEntry<PK, SK, V> entry = primary.remove(primaryKey);
			if (entry != null) {
				secondary.remove(entry.sk);
			}
		} finally {
			wlock.unlock();
		}
	}

	public List<PK> getPrimaryKeys() {
		rlock.lock();
		try {
			return new ArrayList<>(primary.keySet());
		} finally {
			rlock.unlock();
		}
	}

	public List<SK> getSecondaryKeys() {
		rlock.lock();
		try {
			return new ArrayList<>(secondary.keySet());
		} finally {
			rlock.unlock();
		}
	}

	public void clear() {
		wlock.lock();
		try {
			primary.clear();
			secondary.clear();
		} finally {
			wlock.unlock();
		}
	}

	public void putLoaded(PK primaryKey, SK secondaryKey, V value, long expiretime) {
		wlock.lock();
		try {
			DualCacheEntry<PK, SK, V> newEntry = new DualCacheEntry<>(primaryKey, secondaryKey, value, System.currentTimeMillis() + expiretime);
			primary.put(primaryKey, newEntry);
			secondary.put(secondaryKey, newEntry);
		} finally {
			wlock.unlock();
		}
	}

	public List<DualCacheEntry<PK, SK, V>> getEntries() {
		rlock.lock();
		try {
			return new ArrayList<>(primary.values());
		} finally {
			rlock.unlock();
		}
	}

	public static class DualCacheEntry<PK, SK, V> {
		protected PK pk;
		protected SK sk;
		protected V value;
		protected long expiretime;
		public DualCacheEntry(PK pk, SK sk, V v, long expiretime) {
			this.pk = pk;
			this.sk = sk;
			this.value = v;
			this.expiretime = expiretime;
		}

		public PK getPrimaryKey() {
			return pk;
		}

		public SK getSecondaryKey() {
			return sk;
		}

		public V getValue() {
			return value;
		}

		public long getExpireDate() {
			return expiretime;
		}

		@Override
		public String toString() {
			return
			"PK: "+getPrimaryKey()+", "+
			"SK: "+getSecondaryKey()+", "+
			"Expire: "+getExpireDate()+", "+
			"Value: "+getValue();
		}
	}

}
