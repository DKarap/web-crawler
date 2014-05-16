package com.webcrawler;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.webdriver.core.Driver;
import org.webdriver.core.GhostDriver;

import com.google.common.collect.ImmutableList;

public class CrawlerTest {

	@Test
	public void test() throws Exception {
		int max_depth = 1;
		String seed_url = "http://www.took.nl/beta/"; 
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
				.addAll(new ArrayList<String>())
	            .build();
		
	
		WebCrawlerImpl WebCrawlerImpl = new WebCrawlerImpl(max_depth, seed_url, ghostDriver, FRAME_TAG_NAME_LIST, LINK_TAG_NAME_LIST, BLACK_LIST_URL, BLACK_LIST_ANCHOR_TEXT);
		WebCrawlerImpl.start();
		
		
		
		
		fail("Not yet implemented");
	}

}
