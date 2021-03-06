package org.onetwo.common.web.utils.html;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HtmlFilterConfig implements Serializable {
	private String tagName = "";
	private String regex;
	private String replace;

	public HtmlFilterConfig() {
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getReplace() {
		if("\\n".equals(replace))
			return new String(new char[]{'\n'});
		return replace;
	}

	public void setReplace(String replace) {
		this.replace = replace;
	}

}
