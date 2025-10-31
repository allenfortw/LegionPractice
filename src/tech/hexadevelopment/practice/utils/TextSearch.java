package tech.hexadevelopment.practice.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import tech.hexadevelopment.practice.LegionPractice;

public class TextSearch {

	private static String[] uselessWords = new String[] {
			"to", "the", "a", "an", "do", "does", "on", "in", "at", "is", "it"
	};
	private static String[] marks = new String[] {
			".", ",", ";", "-", ":", "(", ")", "[", "]", "{", "}", "/"
	};
	private static String[] major = new String[] {
			"sumo", "brackets", "koth", "kit", "arena", "bot",
			"extramaterial", "setup", "create", "kiteditor", "editor",
			"replay", "tab", "api", "placeholders", "chestaccess", "stickspawn", "build", "sign",
			"1v1", "tournament", "lms", "juggernaut", "contact", "discord", "skype",
			"telegram", "channel", "support", "gapple", "goldenapple", "goldenhead", "golden",
			"apple", "void", "battlekit", "/battlekit", "battlekit"

	};


	private static int MIN_OCCURENCY = 3;

	public static boolean isUselessKeyword(String keyword) {
		for(String s : uselessWords) {
			if(s.equals(keyword)) {
				return true;
			}
		}
		return false;
	}

	public static String search(List<String> keywords, ChatColor chatColor, boolean boldSearch) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for(String chapter : getChapters()) {
			int c = 0;
			for(String s : keywords) {
				if(!isUselessKeyword(s)) {
					int occ = +occurrence(chapter, s);
					if(occ > 0) {
						c++;
					}
					if(keywords.size()-c < 2) occ += c/2;
					map.put(chapter, map.getOrDefault(chapter, 0)+occ);
				}
			}
		}
		int i = 0;
		String s = null;
		for(Entry<String, Integer> e : map.entrySet()) {
			double x = 1-e.getKey().split(" ").length/5;
			int result = (int) (e.getValue()*(x < 0.3 ? 0.3 : x));
			if(result > i) {
				s = e.getKey();
				i = result;
			}
			if(e.getValue() > 0) {
				//Bukkit.broadcastMessage(e.getKey().substring(0, e.getKey().length()-1 < 10 ? e.getKey().length()-1 : 10) + " " + result);
			}
		}
		if(s == null || i < MIN_OCCURENCY) {
			return ChatColor.RED + "Sorry, I couldn't understand the questions.";
		}
		for(String k : keywords) {
			s = s.replace(k, chatColor + "" + ChatColor.BOLD + k + chatColor);
		}

		for(String w : s.split(" ")) {
			for(String k : keywords) {
				if(k.equalsIgnoreCase(w) || distance(w, k) <= 1 || (distance(w, k) <= 2 && w.length() > 4)) {
					s = s.replace(w, chatColor + "" + ChatColor.BOLD + w + chatColor);
				}
			}
		}

		return chatColor + s;
	}

	private static int occurrence(String chapter, String keyword) {
		int occ = 0;
		for(String s : marks) {
			chapter = chapter.replace(s, "");
		}
		String[] words = chapter.split(" ");
		List<String> majors = new ArrayList<String>();
		for(String s : words) {
			int i = distance(s, keyword);
			if(i == 0) {
				occ++;
				if(occ < 2) {
					for(String m : major) {
						if(m.equals(s)) {
							if(!majors.contains(s.toLowerCase())) {
								occ+=5;	
							}
							occ+=2;
							majors.add(s.toLowerCase());
						}
						else if(s.contains(m) || m.contains(s)) {
							if(!majors.contains(s.toLowerCase())) {
								occ+=2;	
							}
							occ++;
							majors.add(s.toLowerCase());
						}
					}
				}
			}
			if(i <= 1) {
				occ++;
			}
		}
		if(occ <= 3 && chapter.contains(keyword)) {
			occ++;
		}
		if(occ <= 3 && chapter.replaceAll(" ", "").contains(keyword)) {
			occ++;
		}
		return occ;
	}

	public static int distance(String a, String b) {
		int [] costs = new int [b.length() + 1];
		for (int j = 0; j < costs.length; j++)
			costs[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];
	}

	private static ArrayList<String> getChapters() {
		BufferedReader br = null;
		ArrayList<String> chapters = new ArrayList<String>();
		String chapter = "";
		try {
			InputStreamReader streamReader = new InputStreamReader(LegionPractice.getInstance().getResource("help.txt"), StandardCharsets.UTF_8);
			br = new BufferedReader(streamReader);
			String l;
			while((l = br.readLine()) != null) {
				if(l.length() == 0) {
					if(chapter.length() > 0) {
						chapters.add(chapter);
						chapter = "";
					}
				}
				else chapter += " " + l;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return chapters;
	}
}
