package tickoptimizer.network;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FakeReadWriteLock extends ReentrantReadWriteLock {

	private static final long serialVersionUID = 1L;

	private final FakeReadLock fakereadlock = new FakeReadLock(this);
	private final FakeWriteLock fakewritelock = new FakeWriteLock(this);

	@Override
	public ReadLock readLock() {
		return fakereadlock;
	}

	@Override
	public WriteLock writeLock() {
		return fakewritelock;
	}

	public static class FakeReadLock extends ReadLock {
		private static final long serialVersionUID = 1L;

		protected FakeReadLock(ReentrantReadWriteLock lock) {
			super(lock);
		}

		@Override
		public void lock() {
		}

		@Override
		public void unlock() {
		}
		
	}

	public static class FakeWriteLock extends WriteLock {
		private static final long serialVersionUID = 1L;

		protected FakeWriteLock(ReentrantReadWriteLock lock) {
			super(lock);
		}

		@Override
		public void lock() {
		}

		@Override
		public void unlock() {
		}
		
	}

}
