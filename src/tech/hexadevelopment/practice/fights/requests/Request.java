package tech.hexadevelopment.practice.fights.requests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;

public interface Request {
	
	public static String REQUESTS = "ToppeBattlesDuelRequest";
	
	public boolean hasExpired();
	
	public Fight getFight();
	
	public static void addDuelRequest(Player p, DuelRequest request) {
		HashSet<DuelRequest> requests = getDuelRequestsForPlayer(p);
		List<DuelRequest> delete = new ArrayList<DuelRequest>();
		for(DuelRequest req : requests) {
			if(req.hasExpired() || request.getDueler().equals(req.getDueler())) {
				delete.add(req);
			}
		}
		for(DuelRequest d : delete) {
			requests.remove(d);
		}
		requests.add(request);
		p.setMetadata(REQUESTS, new FixedMetadataValue(LegionPractice.getInstance(), requests));
	}
	
	public static HashSet<DuelRequest> getDuelRequestsForPlayer(Player p) {
		if(p.hasMetadata(REQUESTS)) {
			MetadataValue m = LegionPractice.getInstance().getMetadata(p, REQUESTS);
			if(m != null && m.value() != null && m.value() instanceof HashSet<?>) {
				HashSet<DuelRequest> hashSet = new HashSet<DuelRequest>();
				for(Object o : ((HashSet<?>)m.value())) {
					if(o instanceof DuelRequest) {
						hashSet.add((DuelRequest) o);
					}
				}
				return hashSet;
			}
		}
		return new HashSet<DuelRequest>();
	}
	
	public static void addPartyRequest(Player p, PartyVsPartyRequest partyVsPartyRequest) {
		HashSet<PartyVsPartyRequest> requests = getPartyRequestsForPlayer(p);
		List<PartyVsPartyRequest> delete = new ArrayList<PartyVsPartyRequest>();
		for(PartyVsPartyRequest req : requests) {
			if(req.hasExpired() || req.getDueler().getOwner().equals(partyVsPartyRequest.getDueler().getOwner())) {
				delete.add(req);
			}
		}
		for(PartyVsPartyRequest d : delete) {
			requests.remove(d);
		}
		requests.add(partyVsPartyRequest);
		p.setMetadata(REQUESTS, new FixedMetadataValue(LegionPractice.getInstance(), requests));
	}
	
	public static HashSet<PartyVsPartyRequest> getPartyRequestsForPlayer(Player p) {
		if(p.hasMetadata(REQUESTS)) {
			MetadataValue m = LegionPractice.getInstance().getMetadata(p, REQUESTS);
			if(m != null && m.value() != null && m.value() instanceof HashSet<?>) {
				HashSet<PartyVsPartyRequest> hashSet = new HashSet<PartyVsPartyRequest>();
				for(Object o : ((HashSet<?>)m.value())) {
					if(o instanceof PartyVsPartyRequest) {
						hashSet.add((PartyVsPartyRequest) o);
					}
				}
				return hashSet;
			}
		}
		return new HashSet<PartyVsPartyRequest>();
	}
}
