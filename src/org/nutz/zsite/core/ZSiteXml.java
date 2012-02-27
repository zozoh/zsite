package org.nutz.zsite.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.MultiLineProperties;
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
	private ZSiteHome home;

	/**
	 * 站点全局变量
	 */
	private Map<String, String> vars;

	/**
	 * 默认模板设定
	 */
	private TmplSetting default_tmpl;

	/**
	 * 页面配置
	 */
	private List<PageSetting> pages;

	/**
	 * 本站点将输出成多少中语言。 如果为 null 或者 size 为 0, 则没有多国语言设定
	 */
	private Map<String, Map<String, String>> locals;

	/* ==================================================== 网站的几个关键路径== */
	private SiteDir dir_msgs;
	private SiteDir dir_tmpl;
	private SiteDir dir_libs;
	private SiteDir dir_imgs;
	private SiteDir dir_css;
	private SiteDir dir_js;

	/**
	 * 在构造函数中解析 _zsite_.xml
	 * 
	 * @param xmlFile
	 *            xml 文件
	 */
	public ZSiteXml(ZSiteHome siteHome, File xmlFile) {
		this.home = siteHome;

		log0f("open '%s' ...", xmlFile.getName());
		Element root = Xmls.xml(xmlFile).getDocumentElement();

		log0("understand DOM");
		/*
		 * Dirs
		 */
		Mirror<ZSiteXml> mirror = Mirror.me(ZSiteXml.class);
		Element eleDirs = Xmls.firstChild(root, "dirs");
		for (Element eleDir : Xmls.children(eleDirs, "dir")) {
			String name = eleDir.getAttribute("name");
			SiteDir value = new SiteDir(this, eleDir);
			mirror.setValue(this, "dir_" + name, value);
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

		// 检查多国语言设定
		locals = new HashMap<String, Map<String, String>>();
		File[] localDirs = dir_msgs.listFolders();
		for (File localDir : localDirs) {
			String key = localDir.getName();
			Map<String, String> msgs = locals.get(key);
			if (null == msgs) {
				msgs = new HashMap<String, String>();
				locals.put(key, msgs);
			}

			for (File f : localDir.listFiles()) {
				if (f.isFile() && f.getName().endsWith(".properties")) {
					MultiLineProperties pp = new MultiLineProperties();
					try {
						pp.load(Streams.fileInr(f));
					}
					catch (IOException e) {
						throw Lang.wrapThrow(e);
					}
					msgs.putAll(pp);
				}
			}
		}

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
		this.default_tmpl = new TmplSetting(this, Xmls.firstChild(root, "default-tmpl"));
		/*
		 * Pages
		 */
		this.pages = new ArrayList<PageSetting>();
		Element elePages = Xmls.firstChild(root, "pages");
		if (null != elePages)
			for (Element elePage : Xmls.children(elePages, "page")) {
				this.pages.add(new PageSetting(this, elePage));
			}

		log0f("... I am now understanded the '%s'", xmlFile.getName());

	}

	public ZSiteHome home() {
		return home;
	}

	public Map<String, Map<String, String>> locals() {
		return locals;
	}

	public boolean hasLocals() {
		return null != locals && locals.size() > 0;
	}

	public Map<String, String> vars() {
		return vars;
	}

	public TmplSetting default_tmpl() {
		return default_tmpl;
	}

	public List<PageSetting> pages() {
		return pages;
	}

	public SiteDir dir_msgs() {
		return dir_msgs;
	}

	public SiteDir dir_tmpl() {
		return dir_tmpl;
	}

	public SiteDir dir_libs() {
		return dir_libs;
	}

	public SiteDir dir_imgs() {
		return dir_imgs;
	}

	public SiteDir dir_css() {
		return dir_css;
	}

	public SiteDir dir_js() {
		return dir_js;
	}

}
