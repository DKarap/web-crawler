package com.webcrawler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.webdriver.core.Driver;
import org.webdriver.core.GhostDriver;
import org.webdriver.domain.WebPage;

import com.google.common.collect.ImmutableList;
import com.machine_learning.core.analysis.tokenizer.Tokenizer;
import com.machine_learning.core.classification.StanfordClassifier;
import com.machine_learning.utils.Helper;
import com.webcrawler.domain.CrawlerSetUp;

public class CrawlerTest {

	@Test
	public void test() throws Exception {
		int max_number_states_to_visit = 2;
		int max_execution_time_seconds = 1000;
		int max_depth = 6;
		String seed_url = "http://www.trifork.nl/en/home.html"; 
		final String CONFIG_FILE_GHOSTDRIVER = "./config/ghostdriver/config.ini";
		Driver ghostDriver = new GhostDriver(CONFIG_FILE_GHOSTDRIVER);
		Tokenizer tokenizer = new Tokenizer(true, Helper.getFileContentLineByLine("./data/stop_words/all_stopwords.txt"));
		
		String CONFIG_FILE = "./config/classification/single_field_classifier.prop";
		String classifierFilePath = "./data/classification/link.ser.gz";
		StanfordClassifier stanfordClassifier = new StanfordClassifier(CONFIG_FILE);
		stanfordClassifier.loadClassifier(classifierFilePath);
		
		ImmutableList<String> FRAME_TAG_NAME_LIST = new ImmutableList.Builder<String>()
				.addAll(Arrays.asList("frame","iframe"))
	            .build();
		ImmutableList<String> LINK_TAG_NAME_LIST = new ImmutableList.Builder<String>()
				.addAll(Arrays.asList("a"))
	            .build();
		
		ImmutableList<String> BLACK_LIST_URL = new ImmutableList.Builder<String>()
				.addAll(Arrays.asList("http://www.piedpiper.com/#hello"))
	            .build();
		
		ImmutableList<String> BLACK_LIST_ANCHOR_TEXT = new ImmutableList.Builder<String>()
				.addAll(Arrays.asList("contact"))
	            .build();
		
		ImmutableList<String> IMG_ATTR_WITH_TEXT_LIST = new ImmutableList.Builder<String>().addAll(Arrays.asList("alt","src","value","title","name", "id")).build();
		
		
		CrawlerSetUp crawlerSetUp = new CrawlerSetUp( max_depth, max_number_states_to_visit, max_execution_time_seconds, FRAME_TAG_NAME_LIST, LINK_TAG_NAME_LIST, BLACK_LIST_URL, BLACK_LIST_ANCHOR_TEXT,IMG_ATTR_WITH_TEXT_LIST,stanfordClassifier,tokenizer); 
		
		
		
		
		
		/**
		 * Main.....
		 */
		WebCrawlerImpl WebCrawlerImpl = new WebCrawlerImpl(crawlerSetUp, ghostDriver);
		WebCrawlerImpl.start(seed_url);
		WebCrawlerImpl.end();
		
		
		
		
		System.out.println("#Pages crawled:");
		for(WebPage web:WebCrawlerImpl.getSemanticWebPages())
			System.out.println("\t"+web.getUrl());
		System.out.println("#Crawler info:");
		System.out.println("\t"+WebCrawlerImpl.getInfo().toString());
		System.out.println("#Crawler setup:");
		System.out.println("\t"+WebCrawlerImpl.getConfig().toString());
		
		assertEquals("wrong nr of crawled states:",2,WebCrawlerImpl.getInfo().getNr_of_unique_states_visit());
	}

}
