package org.nutz.zsite.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.w3c.dom.Element;

/**
 * 叠加网页的配置信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class PageSetting extends ZSiteXmlItem {

	/**
	 * 可以匹配的网页： 匹配站点全路径
	 */
	private Pattern regex;

	/**
	 * 这些网页用什么模板
	 */
	private TmplSetting tmplSetting;

	/**
	 * 这些网页有什么变量
	 */
	private Map<String, String> vars;

	public PageSetting(ZSiteXml xml, Element ele) {
		super(xml, ele);
		regex = Pattern.compile(Xmls.get(ele, "regex"));

		Element eleTmpl = Xmls.firstChild(ele, "tmpl");
		if (null != eleTmpl)
			tmplSetting = new TmplSetting(xml, eleTmpl);

		vars = new HashMap<String, String>();
		Element eleVars = Xmls.firstChild(ele, "vars");
		if (null != eleVars) {
			for (Element eleVar : Xmls.children(eleVars, "var")) {
				this.vars.put(	Strings.trim(eleVar.getAttribute("name")),
								Strings.trim(eleVar.getTextContent()));
			}
		}
	}

	public boolean match(File f) {
		return regex.matcher(xml.home().getSitePath(f)).find();
	}

	public boolean hasTmplSetting() {
		return null != tmplSetting;
	}

	public TmplSetting getTmplSetting() {
		return tmplSetting;
	}

	public void overlapVars(Map<String, String> map) {
		if (null != vars && vars.size() > 0)
			map.putAll(vars);
	}

}
