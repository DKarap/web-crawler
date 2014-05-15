package com.webcrawler;

import java.util.List;

import org.webdriver.domain.WebPage;


public interface WebCrawler {

	public boolean start(String seed_url);
	
	public boolean end();
	
	public List<WebPage> getSemanticPages();
	
	public boolean getInfo();//#states visit, #semantic states, time, log
	
	public boolean getConfig();//#(unique)states to visit, max time, depth, use_page classifier, use_link_classifier
	
}
