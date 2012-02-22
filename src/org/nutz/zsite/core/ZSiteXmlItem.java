package org.nutz.zsite.core;

import org.w3c.dom.Element;

/**
 * _zsite_.xml 的某个元素对应的项
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class ZSiteXmlItem {

	protected ZSiteXml xml;

	protected Element ele;

	public ZSiteXmlItem(ZSiteXml xml, Element ele) {
		this.xml = xml;
		this.ele = ele;
	}

}
