package org.nutz.zsite.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.doc.html.HtmlDocRender;
import org.nutz.doc.meta.ZDoc;
import org.nutz.doc.zdoc.ZDocParser;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;
import org.nutz.zsite.util.Regex;

import static org.nutz.zsite.util.ZSiteLogs.*;

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

	/**
	 * 站点配置
	 */
	private ZSiteXml xml;

	public ZSiteHome(File home) {
		// 保存根路径
		this.home = home;

		// 找到 _zsite_.xml，没有的话，采用默认的
		File xmlFile = Files.getFile(home, ZSITE_XML);
		if (!xmlFile.exists())
			xmlFile = Files.findFile("org/nutz/zsite/_zsite_.xml");

		// 解析 XML 文件
		xml = new ZSiteXml(this, xmlFile);
	}

	/**
	 * 执行 zSite 渲染
	 * 
	 * @param dest
	 *            输出目录
	 * @param regex
	 *            要匹配的文件对象，null 表示全匹配
	 * @param clean
	 *            是否输出前先清空目标目录
	 * @return 处理的页面对象数量
	 */
	public int renderTo(final File dest, String regex, boolean clean) {
		if (clean)
			Files.clearDir(dest);

		// 准备变量
		final int[] re = new int[1];
		final Regex reg = Regex.NEW(regex);
		final ZSiteHome siteHome = this;

		// 文件过滤器
		FileFilter flt = new FileFilter() {
			// 过滤这些源目录 ...
			public boolean accept(File f) {
				if (f.isHidden())
					return false;
				// 忽略模板目录
				if (xml.dir_tmpl().contains(f))
					return false;

				// 忽略组件目录
				if (xml.dir_libs().contains(f))
					return false;

				// 匹配
				String path = Disks.getRelativePath(home, f);
				return reg.match(path);
			}
		};

		// 处理单个文件
		FileVisitor visitor = new FileVisitor() {
			public void visit(File f) {
				String suffixName = Files.getSuffixName(f);
				if (null != suffixName)
					suffixName = suffixName.toLowerCase();

				// HTML
				if (suffixName.matches("^(htm|html)$")) {
					log1f(" - html : '%s'", Disks.getRelativePath(home, f));

					// 预处理文件
					String html = siteHome.evalText(f);

					// 准备增加的 JS
					StringBuilder sb = new StringBuilder("\n");
					join_rs(sb,
							"<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">",
							siteHome.xml().dir_css().find(f, ".css"));
					join_rs(sb,
							"<script language=\"Javascript\" src=\"%s\"></script>",
							siteHome.xml().dir_js().find(f, ".js"));

					// 替换 HTML
					int pos = html.indexOf("</head>");
					if (pos > 0) {
						String before = html.substring(0, pos);
						String after = html.substring(pos);
						html = before + sb + after;
					}

					// 写入目标文件
					File df = siteHome.createDestFileIfNoExists(dest, f);
					Files.write(df, html);
					// 记数
					re[0]++;
				}
				// zDoc
				else if (suffixName.matches("^(zdoc|txt)$")) {
					log1f(" - doc : '%s'", Disks.getRelativePath(home, f));

					// 预处理文件
					String text = siteHome.evalText(f);

					// 解析
					ZDocParser parser = new ZDocParser();
					ZDoc doc = parser.parse(Lang.inr(text));

					// 设置 CSS & JS
					doc.setAttr("css", siteHome.xml().dir_css().find(f, ".css"));
					doc.setAttr("js", siteHome.xml().dir_js().find(f, ".js"));

					// 渲染 HTML
					HtmlDocRender docRender = new HtmlDocRender();
					String html = docRender.render(doc).toString();

					// 写入目标文件
					File df = siteHome.createDestFileIfNoExists(dest, f);
					Files.write(df, html);

					// 记数
					re[0]++;
				}
				// 图片 JS 以及 CSS 则 copy
				else if (suffixName.matches("^(js|css|png|jpg|jpeg|gif|swf)$")) {
					log1f(" - copy : %s", siteHome.getSitePath(f));

					File df = siteHome.createDestFileIfNoExists(dest, f);
					Files.copy(f, df);
				}

			}

			private void join_rs(StringBuilder sb, String format, List<File> cssFiles) {
				for (File cssFile : cssFiles) {
					String relPath = Disks.getRelativePath(home, cssFile);
					sb.append("\n").append(String.format(format, relPath));
				}
				sb.append("\n");
			}
		};

		// 开始访问
		Disks.visitFile(home, visitor, flt);

		// 返回
		return re[0];
	}

	/**
	 * 对文件内容进行预处理，替换占位符
	 * 
	 * @param vars
	 *            变量表
	 * @param f
	 *            文件
	 * @return 处理后的内容
	 */
	public String evalText(File f) {
		// 模板 和 当前文件的变量
		TmplSetting tmpl = xml.default_tmpl();

		Map<String, String> vars = new HashMap<String, String>();
		// 设置默认变量
		vars.put("now", Times.sDT(Times.now()));
		vars.put("file.name", f.getName());

		// 得到这个文件的变量以及模板
		vars.putAll(xml.vars());
		for (PageSetting ps : xml.pages()) {
			if (ps.match(f)) {
				if (ps.hasTmplSetting())
					tmpl = ps.getTmplSetting();
				ps.overlapVars(vars);
			}
		}

		// 分析
		Segment seg = Segments.read(f);

		// 设置模板的 context
		Context context = createContext(f, vars, seg);

		// 执行替换并返回
		String mainContent = Segments.replace(seg, context);

		// 包裹模板
		return tmpl.wrapContent(vars, mainContent);
	}

	public Context createContext(File f, Map<String, String> vars, Segment seg) {
		Context context = Lang.context();
		for (String key : seg.keys()) {
			// 图片引入占位符
			if (key.startsWith("img:")) {
				String[] ss = Strings.splitIgnoreBlank(key.substring("img:".length()));
				StringBuilder sb = new StringBuilder();
				for (String s : ss) {
					String src = evalImageSrc(f, s);
					sb.append(String.format("\n<img class=\"site_pic\" src=\"%s\"> ", src));
				}
				context.set(key, sb);
			}
			// 引入组件的占位符
			else if (key.startsWith("@")) {
				String libName = key.substring(1);
				File libFile = xml.dir_libs().getFile(libName + ".html");
				String libContext = evalText(libFile);
				context.set(key, libContext);
			}
			// 普通占位符
			else {
				context.set(key, Strings.sBlank(vars.get(key), "${" + key + "}"));
			}
		}
		return context;
	}

	/**
	 * 根据当前文件，以及文件中某个图片的 src，得到真正相对与这个文件图片的真实相对路径
	 * 
	 * @param f
	 *            文件
	 * @param src
	 *            图片路径
	 * @return 新的路径
	 */
	public String evalImageSrc(File f, String src) {
		// 看看是不是在图片库
		File imgFile = xml.dir_imgs().getFile(src);
		// 看看是不是在本目录
		if (!imgFile.exists()) {
			imgFile = Files.getFile(f, src);
		}
		// 返回新 src
		return Disks.getRelativePath(f, imgFile);
	}

	/**
	 * @return Site 的 XML
	 */
	public ZSiteXml xml() {
		return xml;
	}

	/**
	 * @return Site 的工程主目录
	 */
	public File getHome() {
		return home;
	}

	/**
	 * 根据源文件，在输出目录建立对应的文件
	 * 
	 * @param dest
	 *            输出目录
	 * @param f
	 *            源文件
	 * @return 目标文件
	 */
	public File createDestFileIfNoExists(File dest, File f) {
		String path = getSitePath(f);
		try {
			return Files.createFileIfNoExists(dest.getAbsolutePath() + "/" + path);
		}
		catch (IOException e) {
			throw Lang.wrapThrow(e);
		}
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
