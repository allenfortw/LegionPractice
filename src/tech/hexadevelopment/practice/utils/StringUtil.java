package tech.hexadevelopment.practice.utils;

import org.apache.commons.lang.StringUtils;

public class StringUtil {

	public static boolean isAlphaNumeric(String s){
	    String pattern= "^[a-zA-Z0-9]*$";
	    return s.matches(pattern);
	}
	
	public static double similarity(String str1, String str2) {
		int i = str1.length();
		int changes = StringUtils.getLevenshteinDistance(str1, str2);
		return 1-(changes/i);
	}
	
}
