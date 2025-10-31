package tech.hexadevelopment.practice.placeholders;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.fights.other.FFAFight;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.hostedevents.brackets.Brackets;
import tech.hexadevelopment.practice.hostedevents.juggernaut.Juggernaut;
import tech.hexadevelopment.practice.hostedevents.koth.KOTH;
import tech.hexadevelopment.practice.hostedevents.lms.LMS;
import tech.hexadevelopment.practice.hostedevents.sumo.Sumo;

public enum PlaceholderMode {

	DEFAULT,
	DUEL,
	PARTY_FFA,
	PARTY_SPLIT,
	PARTY_VS_PARTY,
	PARTY_VS_BOTS,
	BRACKETS,
	SUMO,
	JUGGERNAUT,
	LMS,
	QUEUE,
	KOTH,
	SPECTATOR,
	FFA
	;

	@Override
	public String toString() {
		return super.toString().toLowerCase().replace("_", "-");
	}

	public static PlaceholderMode getCurrentMode(Player p) {
		PlaceholderMode type = PlaceholderMode.DEFAULT;
		if(p.hasMetadata(QueueManager.waitingQueue)) {
			return PlaceholderMode.QUEUE;
		}
		PvPEvent event = PvPEvent.getEvent(p);
		if(event != null) {
			if(event instanceof Sumo) {
				type = SUMO;
			}
			else if(event instanceof Brackets) {
				type = PlaceholderMode.BRACKETS;
			}
			else if(event instanceof Juggernaut) {
				type = PlaceholderMode.JUGGERNAUT;
			}
			else if(event instanceof LMS) {
				type = PlaceholderMode.LMS;
			}
			else if(event instanceof KOTH)  {
				type = PlaceholderMode.KOTH;
			}
		}
		else {
			Fight fight = Fight.getCurrentFight(p, LegionPractice.getInstance());
			if(fight != null) {
				if(fight instanceof Duel || fight instanceof BotDuel) {
					type = PlaceholderMode.DUEL;
				}
				else if(fight instanceof PartyFFA) {
					type = PlaceholderMode.PARTY_FFA;
				}
				else if(fight instanceof PartySplit) {
					type = PlaceholderMode.PARTY_SPLIT;
				}
				else if(fight instanceof PartyVsParty) {
					type = PlaceholderMode.PARTY_VS_PARTY;
				}
				else if(fight instanceof PartyVsBots) {
					type = PlaceholderMode.PARTY_VS_BOTS;
				}
				else if(fight instanceof FFAFight) {
					//type = PlaceholderMode.FFA;
				}
			}
			else if(LegionPractice.getInstance().getSpectatorHandler().isSpectator(p)) {
				//only when spectating a fight
				if(LegionPractice.getInstance().getSpectatorHandler().getSpectatingFight().get(p.getUniqueId()) != null) {
					type = PlaceholderMode.SPECTATOR;
				}
			}
		}
		return type;
	}

}
