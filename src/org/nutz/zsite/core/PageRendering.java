package org.nutz.zsite.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.Disks;
import org.nutz.zsite.ZSite;

public class PageRendering {

	/**
	 * 模板设定
	 */
	private TmplSetting tmpl;

	/**
	 * 变量
	 */
	private Map<String, String> vars;

	private File pageFile;

	private Segment pageSegment;

	private ZSiteXml xml;

	public PageRendering(ZSiteXml xml, File f) {
		this(xml, f, null);
	}

	public PageRendering(ZSiteXml xml, File f, Map<String, String> vars) {
		this(xml, f, Segments.read(f), vars);
	}

	public PageRendering(ZSiteXml xml, File f, Segment pageSegment, Map<String, String> vars) {
		this.xml = xml;
		this.pageFile = f;

		tmpl = xml.default_tmpl();
		if (null == vars) {
			vars = new HashMap<String, String>();
			// 设置默认变量
			vars.put("now", Times.sDT(Times.now()));
			vars.put("file.name", pageFile.getName());

			// 设置 pageName
			String path = xml.home().getSiteName(pageFile);
			String pageName = Strings.sBlank(	ZSite.getLocal().get("pgnm." + path),
												Files.getMajorName(pageFile));
			vars.put("pageName", pageName);

			// 得到这个文件的变量以及模板
			vars.putAll(xml.vars());
			for (PageSetting ps : xml.pages()) {
				if (ps.match(pageFile)) {
					if (ps.hasTmplSetting())
						tmpl = ps.getTmplSetting();
					ps.overlapVars(vars);
				}
			}
		}
		this.vars = vars;

		// 分析
		this.pageSegment = pageSegment;

	}

	public Context createContext() {
		Context context = Lang.context();
		Map<String, String> msgs = ZSite.getLocal();

		for (String key : pageSegment.keys()) {
			// 多国语言占位符
			if (key.startsWith("#")) {
				setLocalToContext(key, context, msgs, key.substring(1));
			}
			// 引入组件的占位符
			else if (key.startsWith("@")) {
				setLibToContext(key, context, key.substring(1));
			}
			// 普通占位符
			else {
				String val = Strings.sBlank(vars.get(key), "${" + key + "}");
				// 变量是多国语言
				if (val.startsWith("#")) {
					setLocalToContext(key, context, msgs, val.substring(1));
				}
				// 变量是组件
				else if (val.startsWith("@")) {
					setLibToContext(key, context, val.substring(1));
				}
				// 变量是普通字符串
				else {
					context.set(key, val);
				}
			}
		}
		return context;
	}

	private void setLibToContext(String key, Context context, String libName) {
		File libFile = xml.dir_libs().getFile(libName + ".html");
		// 找到组件
		if (null != libFile && libFile.exists()) {
			PageRendering libIng = new PageRendering(xml, libFile, vars);
			context.set(key, libIng.text());
		}
		// 未找到组件
		else {
			context.set(key, "${@" + key + "}");
		}
	}

	private void setLocalToContext(	String key,
									Context context,
									Map<String, String> msgs,
									String msgkey) {
		if (null != msgs && msgs.containsKey(msgkey)) {
			context.set(key, msgs.get(msgkey));
		} else {
			context.set(key, "${" + key + "}");
		}
	}

	/**
	 * 格式化 HTML
	 * <ul>
	 * <li>为修改 HTML 所有的 href 属性
	 * <li>为 HTML 增加对应 CSS 和 JS 文件
	 * </ul>
	 * 
	 * @param html
	 *            原始 HTML 代码
	 * 
	 * @return 修改后的 HTML
	 */
	public String normalizeHtml(String html) {
		// 准备增加的 JS
		StringBuilder sb = new StringBuilder("\n");
		join_rs(sb,
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">",
				xml.dir_css().find(pageFile, ".css"));
		join_rs(sb,
				"<script language=\"Javascript\" src=\"%s\"></script>",
				xml.dir_js().find(pageFile, ".js"));

		// 将网页拆成两部分
		String before = "";
		String after = html;

		// 寻找插入 CSS/JS 的点
		int pos = html.indexOf("</head>");
		if (pos > 0) {
			before = html.substring(0, pos);
			after = html.substring(pos);
		}

		// 将网页主体的的 src 和 href 进行修改
		Matcher m = LNK.matcher(after);
		StringBuilder body = new StringBuilder();
		String str = after;
		int off = 0;
		while (m.find()) {
			String tp = m.group(1).toLowerCase();
			String lnk = m.group(2);
			String end = m.group(3);
			body.append(str.subSequence(off, m.start()));
			body.append(tp);
			// 如果是图片
			if (tp.startsWith("src")) {
				body.append(xml.home().normalizeImageSrc(pageFile, lnk));
			}
			// 如果是链接
			else {
				lnk = xml.home().normalizePageLink(pageFile, lnk);
				Matcher ma = HREF.matcher(lnk);
				if (ma.find())
					lnk = ma.group(1) + ".html";
				body.append(lnk);
			}
			body.append(end);
			off = m.end();
		}
		// 增加其余的部分
		body.append(str.subSequence(off, str.length()));

		return before + sb + body.toString();
	}

	private static Pattern HREF = Pattern.compile("^(.*)([.])(zdoc|txt)$");

	private static Pattern LNK = Pattern.compile(	"(src=\"|href=\")([^\"]+)(\")",
													Pattern.CASE_INSENSITIVE);

	private void join_rs(StringBuilder sb, String format, List<File> rsFiles) {
		for (File rsFile : rsFiles) {
			String relPath = Disks.getRelativePath(pageFile, rsFile);
			sb.append("\n").append(String.format(format, relPath));
		}
		sb.append("\n");
	}

	public String text() {
		// 设置模板的 context
		Context context = createContext();

		// 执行替换并返回
		return Segments.replace(pageSegment, context);
	}

	public String wrapText(String text) {
		return tmpl.wrapContent(vars, text);
	}

	public Map<String, String> vars() {
		return vars;
	}

	public TmplSetting tmpl() {
		return tmpl;
	}

}
