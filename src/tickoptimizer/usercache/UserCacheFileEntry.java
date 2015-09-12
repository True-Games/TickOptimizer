package tickoptimizer.usercache;

import java.util.Date;

import com.mojang.authlib.GameProfile;

public class UserCacheFileEntry {

	private GameProfile profile;
	private Date expireDate;

	public UserCacheFileEntry(GameProfile profile, Date date) {
		this.profile = profile;
		this.expireDate = date;
	}

	public long getExpireDate() {
		return expireDate.getTime();
	}

	public GameProfile getProfile() {
		return profile;
	}

}
