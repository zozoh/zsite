package org.nutz.zsite.core;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;

/**
 * 封装了一个工程目录，以及里所有的对象
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZSiteHome {

	private static final String ZSITE_XML = "_zsite_.xml";

	/**
	 * 主目录
	 */
	private File home;

	public ZSiteHome(File home) {
		// 保存根路径
		this.home = home;

		// 找到 _zsite_.xml，没有的话，采用默认的
		File xmlFile = Files.getFile(home, ZSITE_XML);
		if (!xmlFile.exists())
			xmlFile = Files.findFile("org/nutz/zsite/_zsite_.xml");

		// 解析 XML 文件
		new ZSiteXml(this, xmlFile);

	}

	/**
	 * 执行 zSite 渲染
	 * 
	 * @param target
	 *            输出目录
	 * @param regex
	 *            要匹配的文件对象，null 表示全匹配
	 * @param clean
	 *            是否输出前先清空目标目录
	 * @return 处理的页面对象数量
	 */
	public int renderTo(File target, String regex, boolean clean) {
		throw Lang.noImplement();
	}

	/**
	 * @return Site 的工程主目录
	 */
	public File getHome() {
		return home;
	}

	/**
	 * 获取某文件相对 site_home 的路径
	 * 
	 * @param f
	 *            文件
	 * @return 相对路径
	 */
	public String getSitePath(File f) {
		return Disks.getRelativePath(home, f);
	}

}
