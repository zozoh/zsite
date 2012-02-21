package org.nutz.zsite.core;

import org.w3c.dom.Element;

/**
 * _zsite_.xml 的某个元素对应的项
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class ZSiteXmlItem {

	protected ZSiteXml siteXml;

	protected Element ele;

	public ZSiteXmlItem(ZSiteXml siteXml, Element ele) {
		this.siteXml = siteXml;
		this.ele = ele;
	}

}
