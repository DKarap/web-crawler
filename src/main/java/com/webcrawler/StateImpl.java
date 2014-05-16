package com.webcrawler;

import org.webdriver.domain.Link;
import org.webdriver.domain.WebPage;

public class StateImpl implements State{

	private WebPage webPage;
	
	
	

	public StateImpl(WebPage webPage) {
		super();
		this.webPage = webPage;
	}

	@Override
	public boolean hasNext() {
		return !webPage.getLinks().isEmpty();
	}

	@Override
	public Link next() {
		return webPage.getLinks().remove(0);
	}


	public WebPage getWebPage() {
		return webPage;
	}

	public void setWebPage(WebPage webPage) {
		this.webPage = webPage;
	}

	
}
