package tech.hexadevelopment.practice.permissions;

public enum Permission {

	STAFF("staff"),
	ADMIN("admin"),
	COOLDOWN_BYPASS("cooldownbypass"),
	BRACKETS_HOST("brackets.host"),
	AUTOMATIC_BRACKETS("hostevent.brackets"),
	SUMO_HOST("sumo.host"),
	AUTOMATIC_SUMO("hostevent.sumo"),
	JUGGERNAUT_HOST("juggernaut.host"),
	AUTOMATIC_JUGGERNUT("hostevent.juggernaut"),
	LMS_HOST("lms.host"),
	AUTOMATIC_LMS("hostevent.lms"),
	KOTH_HOST("koth.host"),
	AUTOMATIC_KOTH("hostevent.koth"),
	PUBLIC_PARTY("publicparty"),
	PARTY_LIMIT_BYPASS("partylimit"),
	CUSTOM_KIT_ARMOR("customkit.armor"),
	CUSTOM_KIT_COMBO("customkit.combo"),
	CUSTOM_KIT_HORSE("customkit.horse"),
	CUSTOM_KIT_BUILD("customkit.build"),
	CUSTOM_KIT_BOW("customkit.bow"),
	UPDATE("update"),
	PARTY_PLAYBACK("partyplayback"),
	PARTY_BOTS("partybots"),
	PLAYBACK("playback"),
	HOST_EVENT_KIT("hostevent.anykit"),
	PREMIUM_QUEUE("premiumqueue"),
	OPEN_PARTY("openparty");
	//"strikepractice.partylimit.maxvalue." + party.getSettings().getMaxPlayerLimit())
	
	private String permission;
	
	private Permission(String permission) {
		this.permission = permission;
	}
	
	public String getPermission() {
		return permission;
	}
	
	@Override
	public String toString() {
		return "strikepractice." + permission;
	}
	
}
