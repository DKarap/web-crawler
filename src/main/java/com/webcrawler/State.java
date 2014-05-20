package com.webcrawler;

import org.webdriver.domain.Frame;
import org.webdriver.domain.Link;

public interface State {

	public boolean hasNextLink();
	
	public Link nextLink();
	
	public boolean hasNextFrame();
	
	public Frame nextFrame();
	
	
}
