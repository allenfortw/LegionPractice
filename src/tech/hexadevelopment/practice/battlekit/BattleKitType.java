package tech.hexadevelopment.practice.battlekit;

public enum BattleKitType {

	ANY,
	DUEL,
	BOT_FIGHT,
	QUEUE,
	PARTY_VS_PARTY,
	PARTY_FFA,
	PARTY_SPLIT,
	PREMIUM_QUEUE;

	public static BattleKitType byName(String t) {
		if(t.contains("any")) {
			return BattleKitType.ANY;
		}
		if(t.contains("ffa")) {
			return BattleKitType.PARTY_FFA;
		}
		if(t.contains("split")) {
			return BattleKitType.PARTY_SPLIT;
		}
		if(t.contains("party")) {
			return BattleKitType.PARTY_VS_PARTY;
		}
		if(t.contains("duel")) {
			return BattleKitType.DUEL;
		}
		if(t.contains("bot")) {
			return BattleKitType.BOT_FIGHT;
		}
		if(t.contains("queue")) {
			return BattleKitType.QUEUE;
		}
		if(t.contains("premium")) {
			return BattleKitType.PREMIUM_QUEUE;
		}
		return null;
	}
}
