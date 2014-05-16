package com.webcrawler;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriverException;
import org.webdriver.core.Driver;
import org.webdriver.domain.FindElementBy;
import org.webdriver.domain.Link;
import org.webdriver.domain.WebPage;

import com.google.common.collect.ImmutableList;

public class WebCrawlerImpl implements WebCrawler{
	
	private final String seed_url;
	private final int max_depth;
	private final Driver driver;
	private final ImmutableList<String> FRAME_TAG_NAME_LIST;
	private final ImmutableList<String> LINK_TAG_NAME_LIST;
	private final ImmutableList<String> BLACK_LIST_URL; //
	private final ImmutableList<String> BLACK_LIST_ANCHOR_TEXT; //contac us

	
	private StateImpl[] last_state_per_depth_level;
	private StateImpl current_state;
	private int current_depth;
	private StringBuilder log_msg;
	private List<WebPage> outputWebPageList;
	private List<Link> linkWeFollowHistoryList;
	private List<String> urlListThatWeVisit;
	
	public WebCrawlerImpl(int max_depth, String seed_url, Driver driver,ImmutableList<String> FRAME_TAG_NAME_LIST, ImmutableList<String> LINK_TAG_NAME_LIST, 
			ImmutableList<String> BLACK_LIST_URL, ImmutableList<String> BLACK_LIST_ANCHOR_TEXT) {
		super();
		this.seed_url = seed_url;
		this.max_depth = max_depth;
		this.last_state_per_depth_level = new StateImpl[max_depth+2];
		this.driver = driver;
		this.log_msg = new StringBuilder();
		this.FRAME_TAG_NAME_LIST = FRAME_TAG_NAME_LIST;
		this.LINK_TAG_NAME_LIST = LINK_TAG_NAME_LIST;
		this.BLACK_LIST_URL = BLACK_LIST_URL;
		this.BLACK_LIST_ANCHOR_TEXT = BLACK_LIST_ANCHOR_TEXT;
		this.outputWebPageList = new ArrayList<WebPage>();
		this.linkWeFollowHistoryList = new ArrayList<Link>();
		this.urlListThatWeVisit = new ArrayList<String>(); 
	}
	
	
	
	

	@Override
	public void start() {
		//start crawling by going to the initial seed page
		boolean success = goToNextState(null, this.seed_url);
		while(true){
			//deep first 
			while(current_depth <= max_depth && current_state!=null && current_state.hasNext()){
				Link linkToFollow = current_state.next();
				success = goToNextState(linkToFollow, null);
				linkWeFollowHistoryList.add(linkToFollow);
				if(success){
					current_depth++;
					last_state_per_depth_level[current_depth] = current_state;
				}
			}
			
			//go back one depth level, except if we are on the seed url(depth = 0)
			current_depth = current_depth == 0 ? 0 : current_depth - 1;
			current_state = last_state_per_depth_level[current_depth];
			//check stopping criteria
			if(current_depth == 0 && (current_state == null || !current_state.hasNext()))
				break;
		}
	}
	
	
	private boolean goToNextState(Link link, String url){
		boolean success = true;
		try{
			//try to go to new state via web driver
			if(url != null)
				success = driver.get(url);
			else if(link !=null){
				success = driver.clickElement(FindElementBy.xpath, link.getXpath(), false);
//				TODO if fail try with the relative xpath!!!
//				if(!success)
//					success = driver.clickElement(FindElementBy.xpath, link.getRelativeXpath(), false);
			}
			//process current state if successfully manage to go there  
			if(success){
				success = processCurrentState(link);
			}
		}catch(WebDriverException e){
			log_msg.append(e.getMessage()+"\n");
			return false;
		}
		return success;
	}
	
	
	private boolean processCurrentState(Link linkToThisState) throws WebDriverException{
		WebPage currentWebPage = driver.getCurrentWebPage(0, FRAME_TAG_NAME_LIST, LINK_TAG_NAME_LIST);
		currentWebPage.addLinkToThisWebPage(linkToThisState);
		
		//1. check if belongs to the black list urls
		if(BLACK_LIST_URL.contains(currentWebPage.getUrl()))
			return false;
		
		//2. filter the outlinks of this state based on the previous selected links and a static stop anchor text list
		List<Link> state_links = currentWebPage.getLinks();
		state_links = filterPreviousFollowedLinks(state_links);
		if(!BLACK_LIST_ANCHOR_TEXT.isEmpty())
			state_links = filterStopLinksBasedOnStopAnchorTextList(state_links);
		//3. TODO if config.keepTopNLinks from each state is set up then keep only the top N links
		currentWebPage.setLinks(state_links);
		
		boolean page_is_semantic_page = true;
		//4. TODO detect language of current page
		//5. TODO classify web page's links, if there is link classifier
		//6. TODO classify web page as semantic or not, if there is a page classifier
		//7. save current web page if is semantic
		if(page_is_semantic_page)
			outputWebPageList.add(currentWebPage);
		
		//8. set current state to current web page
		current_state = new StateImpl(currentWebPage);
		return true;
	}
	
	
	private List<Link> filterPreviousFollowedLinks(List<Link> links){
		List<Link> filteredLinks = new ArrayList<Link>();
		for(Link link:links){
			if(!linkWeFollowHistoryList.contains(link))
				filteredLinks.add(link);
			else
				System.out.println("link was followed before:"+link.getText());
		}
		return filteredLinks;
	}
	
	
	private List<Link> filterStopLinksBasedOnStopAnchorTextList(List<Link> links){
		List<Link> filteredLinks = new ArrayList<Link>();
		for(Link link:links){
			if(link.getText() != null && !link.getText().isEmpty()){
				if(!this.BLACK_LIST_ANCHOR_TEXT.contains(link.getText().toLowerCase()))
					filteredLinks.add(link);
			}
		}
		return filteredLinks;
	}

	
	@Override
	public List<WebPage> getWebPages() {
		return outputWebPageList;
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
