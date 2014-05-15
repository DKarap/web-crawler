package com.webcrawler;

import org.webdriver.domain.WebPage;

public class StateImpl implements State{

	private WebPage webPage;
	private String url;
	
	
	
	public StateImpl(String url) {
		super();
		this.url = url;
	}

	@Override
	public boolean hasNext() {
		return !webPage.getLinks().isEmpty();
	}

	@Override
	public State next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSemantic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WebPage getWebPage() {
		// TODO Auto-generated method stub
		return null;
	}

}
