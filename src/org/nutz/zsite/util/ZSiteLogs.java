package org.nutz.zsite.util;

import org.nutz.lang.Strings;

/**
 * 日志帮助函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class ZSiteLogs {

	public static void log3f(String format, Object... args) {
		logf(3, format, args);
	}

	public static void log3(String msg) {
		log(3, msg);
	}

	public static void log2f(String format, Object... args) {
		logf(2, format, args);
	}

	public static void log2(String msg) {
		log(2, msg);
	}

	public static void log1f(String format, Object... args) {
		logf(1, format, args);
	}

	public static void log1(String msg) {
		log(1, msg);
	}

	public static void log0f(String format, Object... args) {
		logf(0, format, args);
	}

	public static void log0(String msg) {
		log(0, msg);
	}

	/**
	 * 输入日志
	 * 
	 * @param indent
	 *            缩进级别
	 * 
	 * @param format
	 *            模板
	 * @param args
	 *            参数
	 */
	public static void logf(int indent, String format, Object... args) {
		log(indent, String.format(format, args));
	}

	/**
	 * 输入日志
	 * 
	 * @param indent
	 *            缩进级别
	 * 
	 * @param msg
	 *            信息
	 */
	public static void log(int indent, String msg) {
		System.out.println(Strings.dup(' ', indent * 2) + msg);
	}

}
