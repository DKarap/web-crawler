package com.webcrawler;

import org.webdriver.domain.Frame;
import org.webdriver.domain.Link;
import org.webdriver.domain.WebPage;

public class StateImpl implements State{

	private WebPage webPage;
	
	
	

	public StateImpl(WebPage webPage) {
		super();
		this.webPage = webPage;
	}

	@Override
	public boolean hasNextLink() {
		return !webPage.getLinks().isEmpty();
	}

	@Override
	public Link nextLink() {
		return webPage.getLinks().remove(0);
	}


	public WebPage getWebPage() {
		return webPage;
	}

	public void setWebPage(WebPage webPage) {
		this.webPage = webPage;
	}

	@Override
	public boolean hasNextFrame() {		
		return !webPage.getFrames().isEmpty();
	}



	@Override
	public Frame nextFrame() {
		return webPage.getFrames().remove(0);
	}

	
}
