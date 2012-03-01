package org.nutz.zsite.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.Tag;
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
	 * @param html
	 *            页面主内容
	 * @return 包裹后的内容
	 */
	public String wrapContent(Map<String, String> vars, String html) {
		// 准备自己的上下文
		PageRendering ing = new PageRendering(xml, tmplFile, vars);
		Context tmplContext = ing.createContext();

		// 填充组件们
		for (Filler filler : fillers)
			filler.fill(vars, tmplContext);

		// 记录曾经找到的列表
		Map<String, List<String>> map = new HashMap<String, List<String>>();

		// 寻找导航区
		for (String key : segment.keys()) {
			// 找到了一个 ...
			if (key.startsWith(ZSite.PH_TMPL_NAV)) {
				List<String> navs = map.get(key);
				// 曾经寻找过，不用再找了 ...
				if (null != navs)
					continue;
				// 记录
				navs = new LinkedList<String>();
				map.put(key, navs);

				// 判断到底要搜索哪个标题级别
				String hn = key.substring(ZSite.PH_TMPL_NAV.length());

				// 设置正则表达式
				String regex = String.format("(<%s>)([^<]+)(</%s>)", hn, hn);
				Pattern p = Pattern.compile(regex);

				// 记录拆分点
				List<Integer> poss = new LinkedList<Integer>();

				// 循环匹配，并记录拆分点 ...
				Matcher m = p.matcher(html);
				while (m.find()) {
					navs.add(m.group(2));
					poss.add(m.start());
				}

				// 开始拆分，插入锚点
				StringBuilder sb = new StringBuilder();
				int off = 0;
				int index = 0;
				for (int pos : poss) {
					sb.append(html.subSequence(off, pos));
					off = pos;
					sb.append("<a name=\"a").append(index++).append("\"></a>");
				}
				sb.append(html.subSequence(off, html.length()));

				// 记录新的 HTML
				html = sb.toString();
			}
		}

		// 设置导航区
		for (String key : map.keySet()) {
			List<String> navs = map.get(key);
			if (navs.isEmpty())
				continue;

			Tag ul = Tag.tag("ul", ".zsite_page_nav");
			int index = 0;
			for (String nav : navs) {
				Tag li = Tag.tag("li");
				Tag a = Tag.tag("a");
				a.attr("href", "#a" + (index++));
				ul.add(li.add(a.setText(nav)));
			}

			tmplContext.set(key, ul.toString());
		}

		// 设置主区域
		tmplContext.set(ZSite.PH_TMPL_MAIN, html);

		// 返回
		return Segments.replace(segment, tmplContext);
	}

	public TmplSetting(final ZSiteXml xml, Element ele) {
		super(xml, ele);
		// 名字
		name = ele.getAttribute("name");

		// 预先解析模板
		tmplFile = xml.dir_tmpl().getFile(name, ".html");
		if (!tmplFile.exists())
			Files.write(tmplFile, "${%main}");
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
					Class<Filler> klass = (Class<Filler>) Class.forName("org.nutz.zsite.filler."
																		+ Strings.capitalize(type)
																		+ "Filler");
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
