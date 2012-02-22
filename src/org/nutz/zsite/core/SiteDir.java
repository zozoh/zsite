package org.nutz.zsite.core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

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

	private String path;

	private File dir;

	/**
	 * @param ele
	 *            配置元素，期望，其有 name 属性，text 节点为相对 site_home 的路径
	 */
	public SiteDir(ZSiteXml xml, Element ele) {
		super(xml, ele);
		this.path = Strings.trim(ele.getTextContent());

		// 带 "~" 的绝对路径
		if (path.startsWith("~"))
			this.dir = new File(Disks.absolute(path));
		// 绝对路径
		else if (path.matches("^([a-zA-Z]:)?([/\\\\])(.*)"))
			this.dir = new File(path);
		// 相对路径
		else
			this.dir = Files.getFile(xml.home().getHome(), path);

		if (!dir.exists()) {
			throw Lang.makeThrow("Fail to find dir '%s'", dir);
		}
	}

	public List<File> find(File pageFile, final String suffix) {
		// 列出所有文件
		File[] fs = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(suffix);
			}
		});

		// 准备返回列表
		String pageName = Files.getMajorName(pageFile);
		String pagePath = file_path_name(pageFile);
		List<File> list = new ArrayList<File>(fs.length);
		for (File f : fs) {
			// 得到当前文件路径和名称信息
			String fnm = Files.getMajorName(f);
			String fpath = file_path_name(f);

			// page.xxx
			if (fnm.equalsIgnoreCase("page")) {
				list.add(f);
			}
			// 符合路径开头
			else if (pagePath.startsWith(fpath)) {
				list.add(f);
			}
			// 精确匹配
			else if (fnm.equals("p_" + pageName)) {
				list.add(f);
			}
			// 名称包含
			else if (fpath.startsWith("p___")) {
				String nm = fpath.substring("p___".length());
				if (pageName.contains(nm))
					list.add(f);
			}
		}

		// 返回
		return list;
	}

	private String file_path_name(File f) {
		String relPath = Disks.getRelativePath(xml.home().getHome(), f);
		relPath = Files.getMajorName(relPath);
		return relPath.replaceAll("[/\\\\]", "_");
	}

	public boolean contains(File f) {
		String relativePath = Disks.getRelativePath(dir, f);
		return null != relativePath && relativePath.startsWith("..");
	}

	public File getFile(String path) {
		return Files.getFile(dir, path);
	}
}
