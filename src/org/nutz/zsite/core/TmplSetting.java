package org.nutz.zsite.core;

import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Xmls;
import org.nutz.zsite.filler.LibFiller;
import org.nutz.zsite.filler.Filler;
import org.w3c.dom.Element;

public class TmplSetting extends ZSiteXmlItem {

	private String name;

	private Map<String, Filler> fillers;

	public TmplSetting(ZSiteXml siteXml, Element ele) {
		super(siteXml, ele);
		name = ele.getAttribute("name");
		Xmls.eachChildren(ele, "set", new Each<Element>() {
			public void invoke(int index, Element eleSet, int length) {
				Element eleLib = Xmls.firstChild(eleSet, "lib");
				if (null != eleLib)
					fillers.put(eleSet.getAttribute("name"),
								new LibFiller(eleLib.getTextContent()));
			}
		});
	}

	public String getName() {
		return name;
	}

}
