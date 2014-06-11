package com.webcrawler;

import java.util.List;
import java.util.Set;

import org.webdriver.domain.WebPage;

import com.webcrawler.domain.CrawlerInfo;
import com.webcrawler.domain.CrawlerSetUp;


public interface WebCrawler {

	public void start(String seed_url);
	
	/**
	 * close the webdriver for now
	 */
	public void end();
	
	public List<WebPage> getSemanticWebPages();
	
	public Set<String> getUrlSetThatWeVisited();
	
	public CrawlerInfo getInfo();//#states visit, #semantic states, time, log
	
	public CrawlerSetUp getConfig();//#(unique)states to visit, max time, depth, use_page classifier, use_link_classifier
	
}
