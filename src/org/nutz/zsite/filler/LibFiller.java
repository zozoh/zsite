package org.nutz.zsite.filler;

import java.io.File;
import java.util.Map;

import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.zsite.core.PageRendering;
import org.nutz.zsite.core.ZSiteXml;

public class LibFiller extends Filler {

	private ZSiteXml xml;

	private File libFile;

	private Segment segment;

	public void init(ZSiteXml xml) {
		this.xml = xml;
		this.libFile = xml.dir_libs().getFile(this.getValue());
		this.segment = Segments.read(libFile);
	}

	@Override
	public void fill(Map<String, String> vars, Context context) {

		PageRendering ing = new PageRendering(xml, libFile, segment, vars);

		// 输出自己
		String libText = ing.text();

		// 加入上下文
		context.set(getName(), libText);

	}

}
