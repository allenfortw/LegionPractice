package tech.hexadevelopment.practice.strikecheat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FutureAPI {

	public static List<UUID> cheatbreakers = new ArrayList<UUID>();
	
	
	public static boolean isBanned(UUID uuid) {
		return false;
	}


	public static boolean has(UUID uuid) {
		return cheatbreakers.contains(uuid);
	}

}
