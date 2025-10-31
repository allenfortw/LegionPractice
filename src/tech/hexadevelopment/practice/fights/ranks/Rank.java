package tech.hexadevelopment.practice.fights.ranks;

public class Rank {


	private int minElo, maxElo;
	private String name;
	private String identifier;

	public Rank(String name, String identifier, int min, int max) {
		this.name = name;
		this.identifier = identifier;
		this.minElo = min;
		this.maxElo = max;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getMaxElo() {
		return maxElo;
	}

	public int getMinElo() {
		return minElo;
	}

	public String getName() {
		return name;
	}
	
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public void setMinElo(int minElo) {
		this.minElo = minElo;
	}
	
	public void setMaxElo(int maxElo) {
		this.maxElo = maxElo;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
