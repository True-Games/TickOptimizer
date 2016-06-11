package tickoptimizer.usercache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import net.minecraft.server.v1_10_R1.UserCache;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;

import org.spigotmc.SpigotConfig;

import tickoptimizer.usercache.DualCache.DualCacheEntry;

public class OptimizedUserCache extends UserCache {

	private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(UserCacheFileEntry.class, new UserCacheEntryJsonSerializer()).create();
	private static final UserCacheEntryType type = new UserCacheEntryType();

	private final DualCache<UUID, String, GameProfile> cache;
	private final File userCacheFile;

	public OptimizedUserCache(GameProfileRepository repo, File file) {
		super(repo, file);
		this.userCacheFile = file;
		this.cache = new DualCache<UUID, String, GameProfile>(SpigotConfig.userCacheCap, 1000L * 60L * 60L * 24L * 30L);
		b();
	}

	@Override
	public void a(GameProfile gameProfile) {
		cache.put(gameProfile.getId(), gameProfile.getName().toLowerCase(Locale.ROOT), gameProfile);
	}

	@SuppressWarnings("deprecation")
	@Override
	public GameProfile getProfile(String name) {
		String playername = name.toLowerCase(Locale.ROOT);
		GameProfile profile = cache.getBySecondaryKey(playername);
		if (profile != null) {
			return profile;
		}
		profile = lookupProfile(MinecraftServer.getServer(), playername);
		if (profile != null) {
			a(profile);
			return profile;
		}
		return null;
	}

	@Override
	public GameProfile a(UUID uuid) {
		return cache.getByPrimaryKey(uuid);
	}

	@Override
	public String[] a() {
		List<String> list = cache.getSecondaryKeys();
		return list.toArray(new String[list.size()]);
	}

	@Override
	public void c() {
		ArrayList<UserCacheFileEntry> list = new ArrayList<UserCacheFileEntry>();
		for (DualCacheEntry<UUID, String, GameProfile> entry : cache.getEntries()) {
			list.add(new UserCacheFileEntry(entry.getValue(), new Date(entry.getExpireDate())));
		}
		String data = GSON.toJson(list);
		try (BufferedWriter writer = Files.newWriter(userCacheFile, Charsets.UTF_8)) {
			writer.write(data);
		} catch (IOException e) {
			userCacheFile.delete();
		}
	}

	@Override
	public void b() {
		if (userCacheFile == null) {
			return;
		}
		try (BufferedReader reader = Files.newReader(userCacheFile, Charsets.UTF_8)) {
			List<UserCacheFileEntry> datalist = GSON.fromJson(reader, type);
			cache.clear();
			for (UserCacheFileEntry entry : datalist) {
				cache.putLoaded(entry.getProfile().getId(), entry.getProfile().getName().toLowerCase(Locale.ROOT), entry.getProfile(), entry.getExpireDate());
			}
		} catch (IOException exception) {
		}
	}

	private static GameProfile lookupProfile(MinecraftServer minecraftserver, String name) {
		GameProfile[] gameProfileArrayHolder = new GameProfile[1];
		GameProfileLookup gameProfileLookup = new GameProfileLookup(gameProfileArrayHolder);
		minecraftserver.getGameProfileRepository().findProfilesByNames(new String[] { name }, Agent.MINECRAFT, gameProfileLookup);
		if ((!minecraftserver.getOnlineMode()) && (gameProfileArrayHolder[0] == null)) {
			UUID uuid = EntityHuman.a(new GameProfile(null, name));
			GameProfile gameprofile = new GameProfile(uuid, name);
			gameProfileLookup.onProfileLookupSucceeded(gameprofile);
		}
		return gameProfileArrayHolder[0];
	}

}
