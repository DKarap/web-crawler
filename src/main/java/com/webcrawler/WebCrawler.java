package com.webcrawler;

import java.util.List;

import org.webdriver.domain.WebPage;


public interface WebCrawler {

	public void start();
		
	public List<WebPage> getWebPages();
	
	public boolean getInfo();//#states visit, #semantic states, time, log
	
	public boolean getConfig();//#(unique)states to visit, max time, depth, use_page classifier, use_link_classifier
	
}
