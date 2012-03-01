package org.nutz.zsite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Times;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.Disks;
import org.nutz.zsite.core.ZSiteHome;

import static org.nutz.zsite.util.ZSiteLogs.*;

/**
 * 最顶层 zSite 函数运行接口，提供一组静态函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class ZSite {

	private static ThreadLocal<Map<String, String>> _MSGS_ = new ThreadLocal<Map<String, String>>();
	private static ThreadLocal<String> LOCAL_NAME = new ThreadLocal<String>();

	public static void setLocal(String name, Map<String, String> msgs) {
		LOCAL_NAME.set(name);
		_MSGS_.set(msgs);
	}

	public static void clearLocal() {
		setLocal(null, null);
	}

	public static Map<String, String> getLocal() {
		return _MSGS_.get();
	}

	public static String getLocalName() {
		return LOCAL_NAME.get();
	}

	/**
	 * 主区域的占位符名
	 */
	public static final String PH_TMPL_MAIN = "%main";

	/**
	 * 页内导航区域的占位符前缀
	 */
	public static final String PH_TMPL_NAV = "%nav:";

	/**
	 * @see #render(File, File, String, boolean)
	 */
	public static int render(String src, String dest, String pattern, boolean clean) {
		src = Disks.absolute(src);
		dest = Disks.normalize(dest);
		return render(new File(src), new File(dest), pattern, clean);
	}

	/**
	 * 执行一次 zSite 输出
	 * 
	 * @param src
	 *            源目录: 为一个系统全路径（支持 ~）， 表示你的 site 存放的位置
	 * @param dest
	 *            输出目录: 为一个系统全路径（支持 ~）， 表示你的 site 要输出到什么地方
	 * @param regex
	 *            对象过滤: 可选，表示本次输出要输出的对象，是一个正则式，以 "!" 开头，表示非
	 * @param clean
	 *            是否输出前先清空目标目录
	 * 
	 * @return 处理了多少个页面对象
	 */
	public static int render(File src, File dest, String regex, boolean clean) {
		/*
		 * 检查源
		 */
		if (null == src || !src.exists()) {
			log0("src don't exists : " + src);
			System.exit(0);
			return -1;
		}
		/*
		 * 检查目标
		 */
		if (null == dest) {
			log0("null dest!");
			System.exit(0);
			return -1;
		} else if (!dest.exists()) {
			Files.makeDir(dest);
		}

		Stopwatch sw = Stopwatch.create();
		log0f(	"start render '%s' to '%s' (%s) since '%s'",
				src.getAbsolutePath(),
				dest.getAbsolutePath(),
				regex,
				Times.sDTms(Times.now()));
		sw.start();

		/*
		 * 开始实现 ...
		 */
		ZSiteHome siteHome = new ZSiteHome(src);
		int re = siteHome.renderTo(dest, regex, clean);
		/*
		 * 搞定 ^_^
		 */
		sw.stop();
		log0f("done for rendering %d pages in [%s]", re, sw.toString());

		return re;
	}

	/**
	 * @see #render(File, File, String, boolean)
	 */
	public static int renderAll(String src, String dest, boolean clean) {
		return render(new File(Disks.absolute(src)), new File(Disks.absolute(dest)), null, clean);
	}

	/**
	 * @see #render(File, File, String, boolean)
	 */
	public static int renderAll(File src, File dest, boolean clean) {
		return render(src, dest, null, clean);
	}

	/**
	 * 提供给外部程序调用，参数的用法为:
	 * 
	 * <pre>
	 * org.nutz.zsite.ZSite [源目录][输出目录][-p 对象过滤]? [-c 是否清除]?
	 * 
	 *   - 源目录: 为一个系统全路径（支持 ~）， 表示你的 site 存放的位置
	 *   - 输出目录: 为一个系统全路径（支持 ~）， 表示你的 site 要输出到什么地方
	 *   - 对象过滤: 可选，表示本次输出要输出的对象，是一个正则式，以 "!" 开头，表示非
	 *   - 是否清除: 可选，表示是否输出前先清除输出目录
	 * 
	 * 比如:
	 * 
	 * 将目录 A 输出到目录 B，之前先清空 B 目录
	 * 
	 *    org.nutz.zsite.ZSite ~/a ~/b -c
	 * 
	 * 将目录 A 输出到目录 B，仅仅处理 .html
	 * 
	 *    org.nutz.zsite.ZSite ~/a ~/b -p '^.*[.]html$'
	 * 
	 * 将目录 A 输出到目录 B，处理所有除了 html 和 zdoc 的文件
	 * 
	 *    org.nutz.zsite.ZSite ~/a ~/b -p '!^(.*[.])(html|zdoc)$'
	 * 
	 * </pre>
	 * 
	 * @param args
	 *            参数
	 */
	public static void main(String[] args) {
		/*
		 * 分析参数表
		 */
		List<String> strs = new ArrayList<String>(args.length);
		boolean clean = false;
		String regex = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			// -c
			if ("-c".equalsIgnoreCase(arg)) {
				clean = true;
			}
			// -p
			else if ("-p".equalsIgnoreCase(arg)) {
				i++;
				if (i >= args.length) {
					System.out.println(getReadMe());
					System.exit(0);
					return;
				}
				regex = args[i];
			}
			// 其他
			else {
				strs.add(arg);
			}
		}
		/*
		 * 判断必要参数
		 */
		if (2 != strs.size()) {
			System.out.println(getReadMe());
			System.exit(0);
			return;
		}

		/*
		 * 开始渲染
		 */
		render(strs.get(0), strs.get(1), regex, clean);
	}

	private static String getReadMe() {
		String str = Files.read("org/nutz/zsite/README.txt");
		Context context = Lang.context();
		context.set("version", _zSite.version());
		context.set("now", Times.sDT(Times.now()));
		return Segments.replace(str, context);
	}

}
