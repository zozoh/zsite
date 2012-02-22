package org.nutz.zsite.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.zsite.ZSite;
import org.nutz.zsite.filler.Filler;
import org.w3c.dom.Element;

public class TmplSetting extends ZSiteXmlItem {

	private File tmplFile;

	private String name;

	private Segment segment;

	private List<Filler> fillers;

	public Segment segment() {
		return segment;
	}

	public String name() {
		return name;
	}

	/**
	 * 将主内容用本模板包裹
	 * 
	 * @param vars
	 *            变量
	 * @param mainContent
	 *            页面主内容
	 * @return 包裹后的内容
	 */
	public String wrapContent(Map<String, String> vars, String mainContent) {
		// 准备自己的上下文
		Context tmplContext = xml.home().createContext(tmplFile, vars, segment);

		// 填充组件们
		for (Filler filler : fillers)
			filler.fill(vars, tmplContext);

		// 设置主区域
		tmplContext.set(ZSite.PH_TMPL_MAIN, mainContent);

		// 返回
		return Segments.replace(segment, tmplContext);
	}

	public TmplSetting(final ZSiteXml xml, Element ele) {
		super(xml, ele);
		// 名字
		name = ele.getAttribute("name");

		// 预先解析模板
		tmplFile = xml.dir_tmpl().getFile(name);
		segment = Segments.read(tmplFile);

		// 查找预装配的占位符
		fillers = new ArrayList<Filler>(10);
		Xmls.eachChildren(ele, "set", new Each<Element>() {
			@SuppressWarnings("unchecked")
			public void invoke(int index, Element eleSet, int length) {
				String name = eleSet.getAttribute("name");
				String type = eleSet.getAttribute("type");
				String str = Strings.trim(eleSet.getTextContent());

				try {
					Class<Filler> klass = (Class<Filler>) Class.forName("org.nutz.zsite.filler"
																		+ Strings.capitalize(type)
																		+ "Filter");
					Filler filler = Mirror.me(klass).born();
					filler.setName(name);
					filler.setValue(str);
					filler.init(xml);
					fillers.add(filler);
				}
				catch (ClassNotFoundException e) {
					throw Lang.wrapThrow(e);
				}
			}
		});
	}

}
