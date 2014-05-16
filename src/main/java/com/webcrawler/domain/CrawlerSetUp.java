package com.webcrawler.domain;

import com.google.common.collect.ImmutableList;

public class CrawlerSetUp {	
	
	private final String seed_url;
	private final int max_depth;
	private final int max_number_states_to_visit;
	private final int max_execution_time_seconds;
	private final ImmutableList<String> FRAME_TAG_NAME_LIST;
	private final ImmutableList<String> LINK_TAG_NAME_LIST;
	private final ImmutableList<String> BLACK_LIST_URL; //
	private final ImmutableList<String> BLACK_LIST_ANCHOR_TEXT; //contac us
	
	
	public CrawlerSetUp(String seed_url, int max_depth,
			int max_number_states_to_visit, int max_execution_time_seconds,
			ImmutableList<String> fRAME_TAG_NAME_LIST,
			ImmutableList<String> lINK_TAG_NAME_LIST,
			ImmutableList<String> bLACK_LIST_URL,
			ImmutableList<String> bLACK_LIST_ANCHOR_TEXT) {
		super();
		this.seed_url = seed_url;
		this.max_depth = max_depth;
		this.max_number_states_to_visit = max_number_states_to_visit;
		this.max_execution_time_seconds = max_execution_time_seconds;
		FRAME_TAG_NAME_LIST = fRAME_TAG_NAME_LIST;
		LINK_TAG_NAME_LIST = lINK_TAG_NAME_LIST;
		BLACK_LIST_URL = bLACK_LIST_URL;
		BLACK_LIST_ANCHOR_TEXT = bLACK_LIST_ANCHOR_TEXT;
	}


	public String getSeed_url() {
		return seed_url;
	}


	public int getMax_depth() {
		return max_depth;
	}


	public int getMax_number_states_to_visit() {
		return max_number_states_to_visit;
	}


	public int getMax_execution_time_seconds() {
		return max_execution_time_seconds;
	}


	public ImmutableList<String> getFRAME_TAG_NAME_LIST() {
		return FRAME_TAG_NAME_LIST;
	}


	public ImmutableList<String> getLINK_TAG_NAME_LIST() {
		return LINK_TAG_NAME_LIST;
	}


	public ImmutableList<String> getBLACK_LIST_URL() {
		return BLACK_LIST_URL;
	}


	public ImmutableList<String> getBLACK_LIST_ANCHOR_TEXT() {
		return BLACK_LIST_ANCHOR_TEXT;
	}
	
}
