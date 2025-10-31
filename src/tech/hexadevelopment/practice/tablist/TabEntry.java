package tech.hexadevelopment.practice.tablist;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

import tech.hexadevelopment.practice.LegionPractice;

public class TabEntry {

	private PlayerTabList customTab;
	private int x;
	private int y;
	private String text;
	private Object nms;
	private Team team;
	private boolean setup;


	public TabEntry(PlayerTabList customTab, String text, int x, int y){
		this.customTab = customTab;
		this.text = text;
		this.x = x;
		this.y = y;
	}

	private void setup(){
		LegionPractice.getInstance().getNMSAccessProvider().getAccess().setupTabEntry(this);
		setup = true;
	}

	public void send() {
		try{
			if (!setup) {
				setup();
			}
			text = ChatColor.translateAlternateColorCodes('&', text);
			if (text.length() > 16){
				team.setPrefix(text.substring(0, 16));
				String suffix = ChatColor.getLastColors(team.getPrefix()) + text.substring(16, text.length());
				if (suffix.length() > 16){
					if (suffix.length() <= 16){
						suffix = text.substring(16, text.length());
						team.setSuffix(suffix.substring(0, suffix.length()));
					}
					else{
						team.setSuffix(suffix.substring(0, 16));
					}
				}
				else {
					team.setSuffix(suffix);
				}
			}
			else{
				team.setPrefix(text);
				team.setSuffix("");
			}
		}catch(Exception e) {}
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the nms player
	 */
	public Object getNMS() {
		return nms;
	}

	/**
	 * @param nms the nms player to set
	 */
	public void setNMS(Object nms) {
		this.nms = nms;
	}

	/**
	 * @return the team
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * @return the setup
	 */
	public boolean isSetup() {
		return setup;
	}

	/**
	 * @param setup the setup to set
	 */
	public void setSetup(boolean setup) {
		this.setup = setup;
	}

	/**
	 * @return the customTab
	 */
	public PlayerTabList getCustomTab() {
		return customTab;
	}

	/**
	 * @param team the team to set
	 */
	public void setTeam(Team team) {
		this.team = team;
	}

}
