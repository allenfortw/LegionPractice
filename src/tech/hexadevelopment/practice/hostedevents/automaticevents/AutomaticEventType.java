package tech.hexadevelopment.practice.hostedevents.automaticevents;

public enum AutomaticEventType {

	BRACKETS,
	KOTH,
	LMS,
	JUGGERNAUT,
	SUMO;
	
	
	public static AutomaticEventType fromString(String automaticEvent) {
		String s = automaticEvent.toLowerCase().replace(" ", "").replace("_", "").replace("-", "");
		if(s.equals("lms") || s.equals("lastmanstanding")) {
			return AutomaticEventType.LMS;
		}
		if(s.equals("koth") || s.equals("kingofthehill")) {
			return AutomaticEventType.KOTH;
		}
		if(s.equals("juggernaut")) {
			return AutomaticEventType.JUGGERNAUT;
		}
		if(s.equals("brackets") || s.equals("1v1tournament")) {
			return AutomaticEventType.BRACKETS;
		}
		if(s.equals("sumo") || s.equals("sumoevent")) {
			return AutomaticEventType.SUMO;
		}
		return null;
	}
	
	@Override
	public String toString() {
		if(this.equals(KOTH) || this.equals(LMS)) return super.toString();
		String string = super.toString().toLowerCase();
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}
}
