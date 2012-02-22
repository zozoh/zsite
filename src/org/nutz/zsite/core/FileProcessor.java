package org.nutz.zsite.core;

import java.io.File;

/**
 * 分别处理不同类型的文件
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface FileProcessor {

	/**
	 * 处理某种文件
	 * 
	 * @param home
	 *            Site 的 HOME
	 * @param dest
	 *            输出目录
	 */
	void doHandle(ZSiteHome home, File dest);

}
