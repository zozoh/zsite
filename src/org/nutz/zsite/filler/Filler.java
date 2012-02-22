package org.nutz.zsite.filler;

import java.util.Map;

import org.nutz.lang.util.Context;
import org.nutz.zsite.core.ZSiteXml;

public abstract class Filler {

	private String name;

	private String value;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public abstract void init(ZSiteXml xml);

	public abstract void fill(Map<String, String> vars, Context context);

}
