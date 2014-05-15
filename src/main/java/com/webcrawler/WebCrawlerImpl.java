package com.webcrawler;

import java.util.List;

import org.webdriver.domain.WebPage;

public class WebCrawlerImpl implements WebCrawler{
	
	int depth;
	State[] last_state_per_depth_level;
	State current_state;
	String seed_url;
	

	
	public WebCrawlerImpl(int depth, String seed_url) {
		super();
		this.depth = depth;
		this.last_state_per_depth_level = new State[depth];
		this.last_state_per_depth_level[0] = new StateImpl(seed_url);
		this.current_state = this.last_state_per_depth_level[0];
		this.seed_url = seed_url;
	}
	
	
	@Override
	public boolean start(String seed_url) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean end() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public List<WebPage> getSemanticPages() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean getInfo() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean getConfig() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
