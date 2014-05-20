package com.webcrawler;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.webdriver.core.Driver;
import org.webdriver.core.GhostDriver;
import org.webdriver.domain.Link;
import org.webdriver.domain.WebPage;

import com.google.common.collect.ImmutableList;
import com.webcrawler.domain.CrawlerSetUp;

public class CrawlerTest {

	@Test
	public void test() throws Exception {
		int max_number_states_to_visit = 5;
		int max_execution_time_seconds = 1000;
		int max_depth = 6;
		String seed_url = "http://www.corelab.com/careers/job-search"; 
		final String CONFIG_FILE_GHOSTDRIVER = "./config/ghostdriver/config.ini";
		Driver ghostDriver = new GhostDriver(CONFIG_FILE_GHOSTDRIVER);

		ImmutableList<String> FRAME_TAG_NAME_LIST = new ImmutableList.Builder<String>()
				.addAll(Arrays.asList("frame","iframe"))
	            .build();
		ImmutableList<String> LINK_TAG_NAME_LIST = new ImmutableList.Builder<String>()
				.addAll(Arrays.asList("a"))
	            .build();
		
		ImmutableList<String> BLACK_LIST_URL = new ImmutableList.Builder<String>()
				.addAll(new ArrayList<String>())
	            .build();
		
		ImmutableList<String> BLACK_LIST_ANCHOR_TEXT = new ImmutableList.Builder<String>()
				.addAll(Arrays.asList("contact"))
	            .build();
		
		CrawlerSetUp crawlerSetUp = new CrawlerSetUp(seed_url, max_depth, max_number_states_to_visit, max_execution_time_seconds, FRAME_TAG_NAME_LIST, LINK_TAG_NAME_LIST, BLACK_LIST_URL, BLACK_LIST_ANCHOR_TEXT); 
		
		
		
		WebCrawlerImpl WebCrawlerImpl = new WebCrawlerImpl(crawlerSetUp, ghostDriver);
		WebCrawlerImpl.start();
		
		System.out.println("#Pages crawled:");
		for(WebPage web:WebCrawlerImpl.getSemanticWebPages())
			System.out.println("\t"+web.getUrl());
		System.out.println("#Crawler info:");
		System.out.println("\t"+WebCrawlerImpl.getInfo().toString());
		System.out.println("#Crawler setup:");
		System.out.println("\t"+WebCrawlerImpl.getConfig().toString());
		
		
		
		fail("Not yet implemented");
	}

}
