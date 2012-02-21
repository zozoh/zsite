package org.nutz.zsite.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.w3c.dom.Element;

import static org.nutz.zsite.util.ZSiteLogs.*;

/**
 * 封装了对于 _zsite_.xml 的理解
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZSiteXml {

	/**
	 * 网站的工作主目录
	 */
	private ZSiteHome siteHome;

	/**
	 * 站点全局变量
	 */
	private Map<String, String> vars;

	/**
	 * 默认模板设定
	 */
	private TmplSetting defaultTmplSetting;

	/**
	 * 页面配置
	 */
	private List<PageSetting> pageSettings;

	/* ==================================================== 网站的几个关键路径== */
	private SiteDir tmplDir;
	private SiteDir libsDir;
	private SiteDir imgsDir;
	private SiteDir cssDir;
	private SiteDir jsDir;

	/**
	 * 在构造函数中解析 _zsite_.xml
	 * 
	 * @param xmlFile
	 *            xml 文件
	 */
	public ZSiteXml(ZSiteHome siteHome, File xmlFile) {
		this.siteHome = siteHome;

		log0f("open '%s' ...", xmlFile.getName());
		Element root = Xmls.xml(xmlFile).getDocumentElement();

		log0("understand DOM '%s'");
		/*
		 * vars
		 */
		this.vars = new HashMap<String, String>();
		Element eleVars = Xmls.firstChild(root, "vars");
		if (null != eleVars)
			for (Element eleVar : Xmls.children(eleVars, "var")) {
				this.vars.put(	Strings.trim(eleVar.getAttribute("name")),
								Strings.trim(eleVar.getTextContent()));
			}
		/*
		 * Default tmpl
		 */
		this.defaultTmplSetting = new TmplSetting(this, Xmls.firstChild(root, "default-tmpl"));
		/*
		 * Pages
		 */
		this.pageSettings = new ArrayList<PageSetting>();
		Element elePages = Xmls.firstChild(root, "pages");
		if (null != elePages)
			for (Element elePage : Xmls.children(elePages, "page")) {
				this.pageSettings.add(new PageSetting(this, elePage));
			}
		/*
		 * Dirs
		 */
		Mirror<ZSiteXml> mirror = Mirror.me(ZSiteXml.class);
		Element eleDirs = Xmls.firstChild(root, "dirs");
		for (Element eleDir : Xmls.children(eleDirs, "dir")) {
			String name = eleDir.getAttribute("name");
			String value = Strings.trim(eleDir.getTextContent());
			mirror.setValue(this, name + "Dir", value);
		}
		// 检查 ...
		for (Field fld : this.getClass().getDeclaredFields()) {
			String name = fld.getName();
			if (name.endsWith("Dir")) {
				Object value = mirror.getValue(this, fld);
				if (null == value)
					throw Lang.makeThrow("Unkown '%s'", name);
			}
		}

		log0f("... I am now understanded the '%s'", xmlFile.getName());

	}

	public ZSiteHome getSiteHome() {
		return siteHome;
	}

	public Map<String, String> getVars() {
		return vars;
	}

	public TmplSetting getDefaultTmplSetting() {
		return defaultTmplSetting;
	}

	public List<PageSetting> getPageSettings() {
		return pageSettings;
	}

	public SiteDir getTmplDir() {
		return tmplDir;
	}

	public SiteDir getLibsDir() {
		return libsDir;
	}

	public SiteDir getImgsDir() {
		return imgsDir;
	}

	public SiteDir getCssDir() {
		return cssDir;
	}

	public SiteDir getJsDir() {
		return jsDir;
	}

}
