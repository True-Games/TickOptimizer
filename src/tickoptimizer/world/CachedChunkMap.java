package tickoptimizer.world;

import org.bukkit.craftbukkit.v1_9_R1.util.LongObjectHashMap;

import net.minecraft.server.v1_9_R1.Chunk;
import net.minecraft.server.v1_9_R1.MinecraftServer;

public class CachedChunkMap extends LongObjectHashMap<Chunk> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	private static final Thread mainThread = MinecraftServer.getServer().primaryThread;

	private Chunk lastAccChunk;
	private long lastAccKey;

	@Override
	public Chunk get(long key) {
		if (Thread.currentThread() != mainThread) {
			return super.get(key);
		}
		if (this.lastAccChunk != null && this.lastAccKey == key) {
			return lastAccChunk;
		}
		Chunk chunk = super.get(key);
		if (chunk == null) {
			return null;
		}
		this.lastAccChunk = chunk;
		this.lastAccKey = key;
		return chunk;
	}

	@Override
	public Chunk remove(long key) {
		if (this.lastAccKey == key) {
			this.lastAccChunk = null;
		}
		return super.remove(key);
	}

	@Override
	public Chunk put(long key, Chunk chunk) {
		this.lastAccChunk = chunk;
		this.lastAccKey = key;
		return super.put(key, chunk);
	}

}
