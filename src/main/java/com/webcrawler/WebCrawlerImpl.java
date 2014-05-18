package com.webcrawler;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriverException;
import org.webdriver.core.Driver;
import org.webdriver.domain.FindElementBy;
import org.webdriver.domain.Link;
import org.webdriver.domain.WebPage;

import com.webcrawler.domain.CrawlerSetUp;

public class WebCrawlerImpl implements WebCrawler{
	
	private final CrawlerSetUp crawlerSetUp;
	private final Driver driver;

	
	private StateImpl[] last_state_per_depth_level;
	private StateImpl current_state;
	private int current_depth;
	private StringBuilder log_msg;
	private List<WebPage> outputWebPageList;
	private List<Link> linkWeFollowHistoryList;
	private List<String> urlListThatWeVisit;
	
	public WebCrawlerImpl(CrawlerSetUp crawlerSetUp, Driver driver) {
		super();
		this.crawlerSetUp = crawlerSetUp;
		this.last_state_per_depth_level = new StateImpl[crawlerSetUp.getMax_depth()+2];
		this.driver = driver;
		this.log_msg = new StringBuilder();
		this.outputWebPageList = new ArrayList<WebPage>();
		this.linkWeFollowHistoryList = new ArrayList<Link>();
		this.urlListThatWeVisit = new ArrayList<String>(); 
	}
	
	
	
	

	@Override
	public void start() {
		//start crawling by going to the initial seed page
		boolean success = goToNextState(null, crawlerSetUp.getSeed_url());
		while(true){
			//deep first 
			while(current_depth <= crawlerSetUp.getMax_depth() && current_state!=null && current_state.hasNext()){
				System.out.println("Current State:"+this.current_state.getWebPage().getUrl()+"\tdepth:"+this.current_depth);
				Link linkToFollow = current_state.next();
				success = goToNextState(linkToFollow, null);
				linkWeFollowHistoryList.add(linkToFollow);
				if(success){
					current_depth++;
					last_state_per_depth_level[current_depth] = current_state;
					System.out.println("\tgot to  State:"+this.current_state.getWebPage().getUrl()+"\tdepth:"+this.current_depth);
				}else{
					System.out.println("\tFail to go to  State:"+linkToFollow.getText()+"\t"+linkToFollow.getAttributesMap().toString()+"\tlogs:"+getLog());

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
				WebPage currentWebPage = driver.getCurrentWebPage(0, crawlerSetUp.getFRAME_TAG_NAME_LIST(), crawlerSetUp.getLINK_TAG_NAME_LIST());
				currentWebPage.addLinkToThisWebPage(link);
				success = processCurrentState(currentWebPage);
			}
		}catch(WebDriverException e){
			log_msg.append(e.getMessage()+"\n");
			return false;
		}
		return success;
	}
	
	
	private boolean processCurrentState(WebPage currentWebPage) {
		
		//1. check if belongs to the black list urls or we already visit that page
		if(crawlerSetUp.getBLACK_LIST_URL().contains(currentWebPage.getUrl()) || this.urlListThatWeVisit.contains(currentWebPage.getUrl()))
			return false;
		
		//2. filter the outlinks of this state based on the previous selected links and a static stop anchor text list
		List<Link> state_links = currentWebPage.getLinks();
		state_links = filterPreviousFollowedLinks(state_links);
		if(!crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT().isEmpty())
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
		//9. save url of current state in order not to visit again
		urlListThatWeVisit.add(currentWebPage.getUrl());
		
		return true;
	}
	
	
	private List<Link> filterPreviousFollowedLinks(List<Link> links){
		List<Link> filteredLinks = new ArrayList<Link>();
		for(Link link:links){
			if(!linkWeFollowHistoryList.contains(link))
				filteredLinks.add(link);
			else
				System.out.println("link was followed before:"+link.getAttributesMap().toString());
		}
		return filteredLinks;
	}
	
	
	private List<Link> filterStopLinksBasedOnStopAnchorTextList(List<Link> links){
		List<Link> filteredLinks = new ArrayList<Link>();
		for(Link link:links){
			if(link.getText() != null && !link.getText().isEmpty()){
				if(!crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT().contains(link.getText().toLowerCase()))
					filteredLinks.add(link);
			}
		}
		return filteredLinks;
	}

	public String getLog(){
		return this.log_msg.toString();
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
	public CrawlerSetUp getConfig() {
		return crawlerSetUp;
	}	
}
