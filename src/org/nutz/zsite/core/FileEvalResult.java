package org.nutz.zsite.core;

import java.util.HashMap;
import java.util.Map;

public class FileEvalResult {

	public FileEvalResult() {
		vars = new HashMap<String, String>();
	}

	private String content;

	private TmplSetting tmpl;

	private Map<String, String> vars;

	public String content() {
		return content;
	}

	public void content(String content) {
		this.content = content;
	}

	public TmplSetting tmpl() {
		return tmpl;
	}

	public void tmpl(TmplSetting tmpl) {
		this.tmpl = tmpl;
	}

	public Map<String, String> vars() {
		return vars;
	}

	public String var(String name) {
		return vars.get(name);
	}

}
