package org.nutz.zsite.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.nutz.doc.meta.ZDoc;
import org.nutz.doc.zdoc.ZDocParser;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;
import org.nutz.zsite.ZSite;
import org.nutz.zsite.util.Regex;
import org.nutz.zsite.util.SiteZDocRender;

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

				// 目录的话，深层进入
				if (f.isDirectory())
					return true;

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
					// 准备渲染
					PageRendering ing = new PageRendering(xml, f);

					log2f(	" - html : %9s %% '%s' ",
							ing.tmpl().name(),
							Disks.getRelativePath(home, f));

					// 预处理文件
					String html = ing.text();
					html = ing.wrapText(html);
					html = ing.normalizeHtml(html);

					// 写入目标文件
					File df = siteHome.createDestFileIfNoExists(dest, f);
					Files.write(df, html);
					// 记数
					re[0]++;
				}
				// ZDoc
				else if (suffixName.matches("^(zdoc|txt)$")) {
					// 准备渲染
					PageRendering ing = new PageRendering(xml, f);

					log2f(	" - zdoc : %9s %% '%s'",
							ing.tmpl().name(),
							Disks.getRelativePath(home, f));

					// 准备渲染器
					ZDocParser parser = new ZDocParser();
					SiteZDocRender render = new SiteZDocRender();

					// 预处理文件
					String text = ing.text();

					// 解析
					ZDoc doc = parser.parse(Lang.inr(text));

					// 渲染
					String html = render.render(doc).toString();

					html = ing.wrapText(html);
					html = ing.normalizeHtml(html);

					// 写入目标文件
					File df = siteHome.createDestFileIfNoExists(dest, f);
					Files.write(df, html);
					// 记数
					re[0]++;

				}
				// 图片 JS 以及 CSS 则 copy
				else if (suffixName.matches("^(js|css|png|jpg|jpeg|gif|swf)$")) {
					log2f(" ------ : %s", siteHome.getSitePath(f));

					File df = siteHome.createDestFileIfNoExists(dest, f);
					Files.copy(f, df);
				}

			}
		};

		// 带多国语言的输出
		if (xml.hasLocals()) {
			for (String key : xml.locals().keySet()) {
				log1f(" [%s] :", key);

				ZSite.setLocal(key, xml.locals().get(key));

				Disks.visitFile(home, visitor, flt);

				ZSite.clearLocal();
			}
		}
		// 没有多国语言的输出
		else {
			ZSite.clearLocal();
			Disks.visitFile(home, visitor, flt);
		}

		// 返回
		return re[0];
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
	public String normalizeImageSrc(File f, String src) {
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
	 * 根据当前文件，以及文件中某个 href，得到真正相对这个文件的真实相对路径
	 * 
	 * @param f
	 *            文件
	 * @param href
	 *            链接路径
	 * @return 新的路径
	 */
	public String normalizePageLink(File f, String href) {
		if (!href.toLowerCase().matches("^http[s]://")) {
			File lnkFile = Files.getFile(home, href);
			try {
				if (lnkFile.getCanonicalPath().equals(f.getCanonicalPath())) {
					return f.getName();
				}
			}
			catch (IOException e) {
				throw Lang.wrapThrow(e);
			}
			if (lnkFile.exists())
				return Disks.getRelativePath(f, lnkFile);
		}
		return href;
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
		// 如果 path 为 zdoc，变 html
		if (path.endsWith(".zdoc")) {
			path = path.substring(0, path.length() - ".zdoc".length()) + ".html";
		}
		// 看看是不是多国语言 ...
		String localName = ZSite.getLocalName();
		if (!Strings.isBlank(localName)) {
			path = localName + "/" + path;
		}

		try {
			return Files.createFileIfNoExists(dest.getAbsolutePath() + "/" + path);
		}
		catch (IOException e) {
			throw Lang.wrapThrow(e);
		}
	}

	public String getSiteName(File f) {
		String path = getSitePath(f);
		int pos = path.lastIndexOf('.');
		path = path.substring(0, pos);
		return path.replaceAll("[\\\\/]", "_");
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
