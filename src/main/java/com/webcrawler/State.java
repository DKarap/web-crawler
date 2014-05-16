package com.webcrawler;

import org.webdriver.domain.Link;

public interface State {

	public boolean hasNext();
	
	public Link next();
	
}
