package org.nutz.zsite.util;

import java.util.regex.Pattern;

/**
 * 支持 "!" 开头的正则表达式
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Regex {

	public static Regex NEW(String regex) {
		return new Regex(regex);
	}

	private boolean not;

	private Pattern pattern;

	private Regex(String regex) {
		if (null == regex) {
			pattern = null;
		} else {
			if (regex.startsWith("!")) {
				not = true;
				regex = regex.substring(1);
			}
			pattern = Pattern.compile(regex);
		}
	}

	public boolean match(CharSequence cs) {
		if (null == pattern)
			return true;
		return pattern.matcher(cs).find() & !not;
	}
}
