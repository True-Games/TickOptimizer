package tickoptimizer.world;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Chunk;
import net.minecraft.server.v1_9_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_9_R1.ChunkRegionLoader;
import net.minecraft.server.v1_9_R1.ChunkSection;
import net.minecraft.server.v1_9_R1.DataConverterManager;
import net.minecraft.server.v1_9_R1.DataConverterTypes;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityTypes;
import net.minecraft.server.v1_9_R1.ExceptionWorldConflict;
import net.minecraft.server.v1_9_R1.FileIOThread;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import net.minecraft.server.v1_9_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagList;
import net.minecraft.server.v1_9_R1.NextTickListEntry;
import net.minecraft.server.v1_9_R1.NibbleArray;
import net.minecraft.server.v1_9_R1.RegionFileCache;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.World;

public class OptimizedChunkRegionLoader extends ChunkRegionLoader {

	private static final Logger logger = LogManager.getLogger();
	
	private final File worldfolder;
	private final DataConverterManager dataconverter;

	public OptimizedChunkRegionLoader(File file, DataConverterManager dataconvertermanager) {
		super(file, dataconvertermanager);
		this.worldfolder = file;
		this.dataconverter = dataconvertermanager;
	}

	private final AvgSizeCounter avgcounter = new AvgSizeCounter(20, 30);
	private volatile HashMap<ChunkCoordIntPair, NBTTagCompound> saveQueue = createMap();
	@SuppressWarnings("rawtypes")
	private final AtomicReferenceFieldUpdater<OptimizedChunkRegionLoader, HashMap> saveQueueUpdater = AtomicReferenceFieldUpdater.newUpdater(OptimizedChunkRegionLoader.class, HashMap.class, "saveQueue");
	private HashMap<ChunkCoordIntPair, NBTTagCompound> createMap() {
		return new HashMap<ChunkCoordIntPair, NBTTagCompound>(avgcounter.getAvg() * 2, 0.75F);
	}

	@Override
	public boolean chunkExists(World world, int x, int z) {
		return this.saveQueue.containsKey(new ChunkCoordIntPair(x, z)) || RegionFileCache.a(this.worldfolder, x, z).chunkExists(x & 0x1F, z & 0x1F);
	}

	@Override
	public Chunk a(World world, int x, int z) throws IOException {
		world.timings.syncChunkLoadDataTimer.startTiming();
		Object[] data = this.loadChunk(world, x, z);
		world.timings.syncChunkLoadDataTimer.stopTiming();
		if (data != null) {
			Chunk chunk = (Chunk) data[0];
			NBTTagCompound nbttagcompound = (NBTTagCompound) data[1];
			this.loadEntities(chunk, nbttagcompound.getCompound("Level"), world);
			return chunk;
		}
		return null;
	}

	@Override
	public Object[] loadChunk(World world, int x, int z) throws IOException {
		ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(x, z);
		NBTTagCompound nbttagcompound = this.saveQueue.get(chunkcoordintpair);
		if (nbttagcompound == null) {
			DataInputStream datainputstream = RegionFileCache.c(this.worldfolder, x, z);
			if (datainputstream == null) {
				return null;
			}
			nbttagcompound = this.dataconverter.a(DataConverterTypes.CHUNK, NBTCompressedStreamTools.a(datainputstream));
		}
		return this.a(world, x, z, nbttagcompound);
	}

	@Override
	protected Object[] a(World world, int x, int z, NBTTagCompound nbttagcompound) {
		if (!nbttagcompound.hasKeyOfType("Level", 10)) {
			OptimizedChunkRegionLoader.logger.error("Chunk file at " + x + "," + z + " is missing level data, skipping");
			return null;
		}
		NBTTagCompound levelnbttagcompound = nbttagcompound.getCompound("Level");
		if (!levelnbttagcompound.hasKeyOfType("Sections", 9)) {
			OptimizedChunkRegionLoader.logger.error("Chunk file at " + x + "," + z + " is missing block data, skipping");
			return null;
		}
		Chunk chunk = createChunk(world, levelnbttagcompound);
		if (!chunk.a(x, z)) {
			OptimizedChunkRegionLoader.logger.error("Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + z + ", got " + chunk.locX + ", " + chunk.locZ + ")");
			levelnbttagcompound.setInt("xPos", x);
			levelnbttagcompound.setInt("zPos", z);
			NBTTagList tileEntities = nbttagcompound.getCompound("Level").getList("TileEntities", 10);
			if (tileEntities != null) {
				for (int te = 0; te < tileEntities.size(); ++te) {
					NBTTagCompound tileEntity = tileEntities.get(te);
					int teX = tileEntity.getInt("x") - chunk.locX * 16;
					int teZ = tileEntity.getInt("z") - chunk.locZ * 16;
					tileEntity.setInt("x", x * 16 + teX);
					tileEntity.setInt("z", z * 16 + teZ);
				}
			}
			chunk = createChunk(world, levelnbttagcompound);
		}
		Object[] data = { chunk, nbttagcompound };
		return data;
	}

	@Override
	public void a(World world, Chunk chunk) throws IOException, ExceptionWorldConflict {
		world.checkSession();
		try {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			NBTTagCompound levelnbttagcompound = new NBTTagCompound();
			nbttagcompound.set("Level", levelnbttagcompound);
			nbttagcompound.setInt("DataVersion", 169);
			writeChunkToNBT(chunk, world, levelnbttagcompound);
			this.a(chunk.k(), nbttagcompound);
		} catch (Exception exception) {
			OptimizedChunkRegionLoader.logger.error("Failed to save chunk", exception);
		}
	}

	@Override
	protected void a(ChunkCoordIntPair chunkcoordintpair, NBTTagCompound nbttagcompound) {
		if (nbttagcompound != null) {
			this.saveQueue.put(chunkcoordintpair, nbttagcompound);
		}
		FileIOThread.a().a(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean c() {
		if (this.saveQueue.isEmpty()) {
			return false;
		}
		HashMap<ChunkCoordIntPair, NBTTagCompound> toSave = saveQueueUpdater.getAndSet(this, createMap());
		avgcounter.addSize(toSave.size());
		for (Entry<ChunkCoordIntPair, NBTTagCompound> entry : toSave.entrySet()) {
			ChunkCoordIntPair chunkcoordintpair = entry.getKey();
			NBTTagCompound nbttagcompound = entry.getValue();
			try {
				this.saveChunkData(chunkcoordintpair, nbttagcompound);
			} catch (Exception exception) {
				OptimizedChunkRegionLoader.logger.error("Failed to save chunk", exception);
			}
		}
		return true;
	}

	private void saveChunkData(ChunkCoordIntPair chunkcoordintpair, NBTTagCompound nbttagcompound) throws IOException {
		try (DataOutputStream dataoutputstream = RegionFileCache.d(this.worldfolder, chunkcoordintpair.x, chunkcoordintpair.z)) {
			NBTCompressedStreamTools.a(nbttagcompound, (DataOutput) dataoutputstream);
		}
	}

	@Override
	public void b(World world, Chunk chunk) throws IOException {
	}

	@Override
	public void a() {
	}

	@Override
	public void b() {
		while (c()) {
		}
	}

	private static void writeChunkToNBT(Chunk chunk, World world, NBTTagCompound nbttagcompound) {
		nbttagcompound.setInt("xPos", chunk.locX);
		nbttagcompound.setInt("zPos", chunk.locZ);
		nbttagcompound.setLong("LastUpdate", world.getTime());
		nbttagcompound.setIntArray("HeightMap", chunk.r());
		nbttagcompound.setBoolean("TerrainPopulated", chunk.isDone());
		nbttagcompound.setBoolean("LightPopulated", chunk.v());
		nbttagcompound.setLong("InhabitedTime", chunk.x());
		ChunkSection[] sections = chunk.getSections();
		NBTTagList sectionsnbt = new NBTTagList();
		boolean flag = !world.worldProvider.m();
		for (int i = sections.length, j = 0; j < i; ++j) {
			ChunkSection chunksection = sections[j];
			if (chunksection != Chunk.a) {
				NBTTagCompound sectionnbt = new NBTTagCompound();
				sectionnbt.setByte("Y", (byte) (chunksection.getYPosition() >> 4 & 0xFF));
				byte[] blocksidsmain = new byte[4096];
				NibbleArray blockdata = new NibbleArray();
				NibbleArray blockidsadd = chunksection.getBlocks().exportData(blocksidsmain, blockdata);
				sectionnbt.setByteArray("Blocks", blocksidsmain);
				sectionnbt.setByteArray("Data", blockdata.asBytes());
				if (blockidsadd != null) {
					sectionnbt.setByteArray("Add", blockidsadd.asBytes());
				}
				sectionnbt.setByteArray("BlockLight", chunksection.getEmittedLightArray().asBytes());
				if (flag) {
					sectionnbt.setByteArray("SkyLight", chunksection.getSkyLightArray().asBytes());
				} else {
					sectionnbt.setByteArray("SkyLight", new byte[chunksection.getEmittedLightArray().asBytes().length]);
				}
				sectionsnbt.add(sectionnbt);
			}
		}
		nbttagcompound.set("Sections", sectionsnbt);
		nbttagcompound.setByteArray("Biomes", chunk.getBiomeIndex());
		chunk.g(false);
		NBTTagList entitiesnbt = new NBTTagList();
		for (int i = 0; i < chunk.getEntitySlices().length; ++i) {
			for (Entity entity : chunk.getEntitySlices()[i]) {
				NBTTagCompound entitynbt = new NBTTagCompound();
				if (entity.d(entitynbt)) {
					chunk.g(true);
					entitiesnbt.add(entitynbt);
				}
			}
		}
		nbttagcompound.set("Entities", entitiesnbt);
		NBTTagList tilesnbt = new NBTTagList();
		for (TileEntity tileentity : chunk.getTileEntities().values()) {
			NBTTagCompound tilenbt = new NBTTagCompound();
			tileentity.save(tilenbt);
			tilesnbt.add(tilenbt);
		}
		nbttagcompound.set("TileEntities", tilesnbt);
		List<NextTickListEntry> list = world.a(chunk, false);
		if (list != null) {
			long wtime = world.getTime();
			NBTTagList nbttaglist4 = new NBTTagList();
			for (NextTickListEntry nextticklistentry : list) {
				NBTTagCompound tileticknbt = new NBTTagCompound();
				MinecraftKey minecraftkey = Block.REGISTRY.b(nextticklistentry.a());
				tileticknbt.setString("i", (minecraftkey == null) ? "" : minecraftkey.toString());
				tileticknbt.setInt("x", nextticklistentry.a.getX());
				tileticknbt.setInt("y", nextticklistentry.a.getY());
				tileticknbt.setInt("z", nextticklistentry.a.getZ());
				tileticknbt.setInt("t", (int) (nextticklistentry.b - wtime));
				tileticknbt.setInt("p", nextticklistentry.c);
				nbttaglist4.add(tileticknbt);
			}
			nbttagcompound.set("TileTicks", nbttaglist4);
		}
	}

	private static Chunk createChunk(World world, NBTTagCompound nbttagcompound) {
		int x = nbttagcompound.getInt("xPos");
		int z = nbttagcompound.getInt("zPos");
		Chunk chunk = new Chunk(world, x, z);
		chunk.a(nbttagcompound.getIntArray("HeightMap"));
		chunk.d(nbttagcompound.getBoolean("TerrainPopulated"));
		chunk.e(nbttagcompound.getBoolean("LightPopulated"));
		chunk.c(nbttagcompound.getLong("InhabitedTime"));
		NBTTagList nbttaglist = nbttagcompound.getList("Sections", 10);
		ChunkSection[] sections = new ChunkSection[16];
		boolean hasSkyLight = !world.worldProvider.m();
		for (int i = 0; i < nbttaglist.size(); ++i) {
			NBTTagCompound sectionnbt = nbttaglist.get(i);
			byte hindex = sectionnbt.getByte("Y");
			ChunkSection chunksection = new ChunkSection(hindex << 4, hasSkyLight);
			byte[] blocksidsmain = sectionnbt.getByteArray("Blocks");
			NibbleArray blockidsadd = sectionnbt.hasKeyOfType("Add", 7) ? new NibbleArray(sectionnbt.getByteArray("Add")) : null;
			NibbleArray blockdata = new NibbleArray(sectionnbt.getByteArray("Data"));
			chunksection.getBlocks().a(blocksidsmain, blockdata, blockidsadd);
			chunksection.a(new NibbleArray(sectionnbt.getByteArray("BlockLight")));
			if (hasSkyLight) {
				chunksection.b(new NibbleArray(sectionnbt.getByteArray("SkyLight")));
			}
			chunksection.recalcBlockCounts();
			sections[hindex] = chunksection;
		}
		chunk.a(sections);
		if (nbttagcompound.hasKeyOfType("Biomes", 7)) {
			chunk.a(nbttagcompound.getByteArray("Biomes"));
		}
		return chunk;
	}

	@Override
	public void loadEntities(Chunk chunk, NBTTagCompound nbttagcompound, World world) {
		world.timings.syncChunkLoadEntitiesTimer.startTiming();
		NBTTagList entities = nbttagcompound.getList("Entities", 10);
		if (entities != null) {
			for (int i = 0; i < entities.size(); ++i) {
				NBTTagCompound entity = entities.get(i);
				createEntityWithPassengers(entity, world, chunk);
				chunk.g(true);
			}
		}
		world.timings.syncChunkLoadEntitiesTimer.stopTiming();
		world.timings.syncChunkLoadTileEntitiesTimer.startTiming();
		NBTTagList tiles = nbttagcompound.getList("TileEntities", 10);
		if (tiles != null) {
			for (int i = 0; i < tiles.size(); ++i) {
				NBTTagCompound tilenbt = tiles.get(i);
				TileEntity tileentity = TileEntity.a(world.getMinecraftServer(), tilenbt);
				if (tileentity != null) {
					chunk.a(tileentity);
				}
			}
		}
		world.timings.syncChunkLoadTileEntitiesTimer.stopTiming();
		world.timings.syncChunkLoadTileTicksTimer.startTiming();
		if (nbttagcompound.hasKeyOfType("TileTicks", 9)) {
			NBTTagList tileticks = nbttagcompound.getList("TileTicks", 10);
			if (tileticks != null) {
				for (int i = 0; i < tileticks.size(); ++i) {
					NBTTagCompound tiletick = tileticks.get(i);
					Block block;
					if (tiletick.hasKeyOfType("i", 8)) {
						block = Block.getByName(tiletick.getString("i"));
					} else {
						block = Block.getById(tiletick.getInt("i"));
					}
					world.b(new BlockPosition(tiletick.getInt("x"), tiletick.getInt("y"), tiletick.getInt("z")), block, tiletick.getInt("t"), tiletick.getInt("p"));
				}
			}
		}
		world.timings.syncChunkLoadTileTicksTimer.stopTiming();
	}

	public static Entity createEntityWithPassengers(NBTTagCompound nbttagcompound, World world, Chunk chunk) {
		Entity entity = createEntity(nbttagcompound, world);
		if (entity == null) {
			return null;
		}
		chunk.a(entity);
		if (nbttagcompound.hasKeyOfType("Passengers", 9)) {
			NBTTagList passengers = nbttagcompound.getList("Passengers", 10);
			for (int i = 0; i < passengers.size(); ++i) {
				Entity passenger = createEntityWithPassengers(passengers.get(i), world, chunk);
				if (passenger != null) {
					passenger.a(entity, true);
				}
			}
		}
		return entity;
	}

	protected static Entity createEntity(NBTTagCompound nbttagcompound, World world) {
		try {
			return EntityTypes.a(nbttagcompound, world);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	private static class AvgSizeCounter {

		private int allsize;
		private final ArrayDeque<Integer> sizes = new ArrayDeque<>();

		public AvgSizeCounter(int count, int initial) {
			allsize = count * initial;
			for (int i = 0; i < count; i++) {
				sizes.add(initial);
			}
		}

		public void addSize(int size) {
			allsize += size;
			allsize -= sizes.poll();
			sizes.add(size);
		}

		public int getAvg() {
			return allsize / sizes.size();
		}

	}

}
