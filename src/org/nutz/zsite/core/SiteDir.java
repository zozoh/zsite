package org.nutz.zsite.core;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.w3c.dom.Element;

/**
 * 封装了一个 zSite 的关键目录的描述
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class SiteDir extends ZSiteXmlItem {

	private File dir;

	/**
	 * @param ele
	 *            配置元素，期望，其有 name 属性，text 节点为相对 site_home 的路径
	 */
	public SiteDir(ZSiteXml siteXml, Element ele) {
		super(siteXml, ele);
		String path = Strings.trim(ele.getTextContent());

		// 带 "~" 的绝对路径
		if (path.startsWith("~"))
			this.dir = new File(Disks.absolute(path));
		// 绝对路径
		else if (path.matches("^([a-zA-Z]:)?([/\\\\])(.*)"))
			this.dir = new File(path);
		// 相对路径
		else
			this.dir = Files.getFile(siteXml.getSiteHome().getHome(), path);

		if (!dir.exists()) {
			throw Lang.makeThrow("Fail to find dir '%s'", dir);
		}
	}

	public File getFile(String path) {
		return Files.getFile(dir, path);
	}
}
