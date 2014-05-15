package com.webcrawler;

import org.webdriver.domain.WebPage;

public interface State {

	public boolean hasNext();
	
	public State next();
	
	public boolean isSemantic();

	public WebPage getWebPage();
}
