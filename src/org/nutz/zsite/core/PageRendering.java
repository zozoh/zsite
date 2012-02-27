package org.nutz.zsite.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
				String msgkey = key.substring(1);
				if (null != msgs && msgs.containsKey(msgkey)) {
					context.set(key, msgs.get(msgkey));
				} else {
					context.set(key, "${" + key + "}");
				}
			}
			// 图片引入占位符
			else if (key.startsWith("img:")) {
				String[] ss = Strings.splitIgnoreBlank(key.substring("img:".length()));
				StringBuilder sb = new StringBuilder();
				for (String s : ss) {
					String src = xml.home().normalizeImageSrc(pageFile, s);
					sb.append(String.format("\n<img class=\"site_pic\" src=\"%s\"> ", src));
				}
				context.set(key, sb);
			}
			// 引入组件的占位符
			else if (key.startsWith("@")) {
				String libName = key.substring(1);
				File libFile = xml.dir_libs().getFile(libName + ".html");
				// 找到组件
				if (null != libFile && libFile.exists()) {
					PageRendering libIng = new PageRendering(xml, libFile, vars);
					context.set(key, libIng.text());
				}
				// 未找到组件
				else {
					context.set("key", "${@" + key + "}");
				}
			}
			// 普通占位符
			else {
				context.set(key, Strings.sBlank(vars.get(key), "${" + key + "}"));
			}
		}
		return context;
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
				body.append(xml.home().normalizePageLink(pageFile, lnk));
			}
			body.append(end);
			off = m.end();
		}
		// 增加其余的部分
		body.append(str.subSequence(off, str.length()));

		return before + sb + body.toString();
	}

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
