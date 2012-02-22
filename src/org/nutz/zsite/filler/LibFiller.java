package org.nutz.zsite.filler;

import java.io.File;
import java.util.Map;

import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
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

		// 解析在自己的上下文
		Context libContext = xml.home().createContext(libFile, vars, segment);
		// 输出自己
		String libText = Segments.replace(segment, libContext);

		// 加入上下文
		context.set(getName(), libText);

	}

}
