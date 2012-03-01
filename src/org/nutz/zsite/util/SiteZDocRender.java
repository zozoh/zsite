package org.nutz.zsite.util;

import static org.nutz.lang.util.Tag.tag;
import org.nutz.doc.DocRender;
import org.nutz.doc.html.HtmlRenderSupport;
import org.nutz.doc.meta.ZBlock;
import org.nutz.doc.meta.ZDoc;
import org.nutz.lang.util.Tag;

public class SiteZDocRender extends HtmlRenderSupport implements DocRender<StringBuilder> {

	public SiteZDocRender() {
		this.skip_index_block = true;
		this.hide_anchor_for_header = true;
		this.hide_top_for_each_section = true;
	}

	@Override
	public StringBuilder render(ZDoc doc) {
		Tag container = tag("div").attr("class", "zdoc_body");

		// Render doc contents
		ZBlock[] ps = doc.root().children();
		for (ZBlock p : ps)
			renderBlock(container, p);

		return new StringBuilder().append(container.toString());
	}

}
