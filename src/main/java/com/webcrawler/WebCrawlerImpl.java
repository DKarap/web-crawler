package com.webcrawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.WebDriverException;
import org.webdriver.core.Driver;
import org.webdriver.domain.FindElementBy;
import org.webdriver.domain.FindFrameBy;
import org.webdriver.domain.Frame;
import org.webdriver.domain.Link;
import org.webdriver.domain.WebPage;

import com.machine_learning.utils.Helper;
import com.webcrawler.domain.CrawlerInfo;
import com.webcrawler.domain.CrawlerSetUp;

public class WebCrawlerImpl implements WebCrawler{
	
	private final CrawlerSetUp crawlerSetUp;
	private final Driver driver;
	private CrawlerInfo crawlerInfo; 
	
	private StateImpl[] last_state_per_depth_level;
	private StateImpl current_state;
	private int current_depth;
	private List<WebPage> semanticWebPageList;//the final output
	private Set<Link> linkWeFollowHistoryList;//in order not to follow the same links
	private Set<Frame> frameWeFollowHistoryList;//in order not to follow the same frames

	private Set<String> urlSetThatWeVisit; //in order not to visit same pages..
	
	
	
	public WebCrawlerImpl(CrawlerSetUp crawlerSetUp, Driver driver) {
		super();
		this.crawlerSetUp = crawlerSetUp;
		this.last_state_per_depth_level = new StateImpl[crawlerSetUp.getMax_depth()+2];
		this.driver = driver;
		this.semanticWebPageList = new ArrayList<WebPage>();
		this.linkWeFollowHistoryList = new HashSet<Link>();
		this.frameWeFollowHistoryList = new HashSet<Frame>();
		this.urlSetThatWeVisit = new HashSet<String>();
		this.crawlerInfo = new CrawlerInfo();
	}
	
	
	
	

	@Override
	public void start(String seed_url) {
		//start crawling by going to the initial seed page
		boolean success = goToNextState(null, seed_url,null,true);
		last_state_per_depth_level[current_depth] = current_state;
		//System.out.println("success:"+success+"\t#depth:"+current_depth+"\t"+current_state.getWebPage().getUrl()+"\tlinks:"+current_state.getWebPage().getLinks().size()+"\tframes:"+current_state.getWebPage().getFrames().size()+"\tsuccess:"+success+"\tcurrent_state.hasNext():"+current_state.hasNextLink()+"\tlinkToThis state:\tdriver.getNumberOfOpenWindows():"+driver.getNumberOfOpenWindows());
		
		while(true){
			//deep first 
			while(current_depth <= crawlerSetUp.getMax_depth() && current_state!=null && (current_state.hasNextLink() || current_state.hasNextFrame()) &&	urlSetThatWeVisit.size() < this.crawlerSetUp.getMax_number_states_to_visit()){
				//go to next state; priority to frames
				if(current_state.hasNextFrame()){
					Frame frameToFollow = current_state.nextFrame();
					frameWeFollowHistoryList.add(frameToFollow);
					success = goToNextState(null, null,frameToFollow,true);
					//System.out.println("frameToFollow:"+frameToFollow.getAttributesMap().toString());
				}
				else{
					Link linkToFollow = current_state.nextLink();
					linkWeFollowHistoryList.add(linkToFollow);
					success = goToNextState(linkToFollow, null,null,true);
					//System.out.println("linkToFollow:"+linkToFollow.getText()+"\t"+linkToFollow.getAttributesMap().toString());
				}
				//update depth and current state in current depth
				if(success){
					current_depth++;
					last_state_per_depth_level[current_depth] = current_state;
					//System.out.println("#depth:"+current_depth+"\t"+current_state.getWebPage().getUrl()+"\tlinks:"+current_state.getWebPage().getLinks().size()+"\tframes:"+current_state.getWebPage().getFrames().size()+"\tsuccess:"+success+"\tcurrent_state.hasNext():"+current_state.hasNextLink()+"\tdriver.getNumberOfOpenWindows():"+driver.getNumberOfOpenWindows());
				}
			}
			
			//go back one depth level, except if we are on the seed url(depth = 0)
			current_depth = current_depth == 0 ? 0 : current_depth - 1;
			current_state = last_state_per_depth_level[current_depth];
			//check stopping criteria
			if( (current_depth == 0 && (current_state == null || (!current_state.hasNextLink() && !current_state.hasNextFrame()))) || urlSetThatWeVisit.size() >= this.crawlerSetUp.getMax_number_states_to_visit())
				break;
			//if continue then go to state of the current depth back...
			success = goToNextState(null, current_state.getWebPage().getUrl(),null,false);
			//System.out.println("#depth:"+current_depth+"\t"+current_state.getWebPage().getUrl()+"\tlinks:"+current_state.getWebPage().getLinks().size()+"\tframes:"+current_state.getWebPage().getFrames().size()+"\tsuccess:"+success+"\tcurrent_state.hasNext():"+current_state.hasNextLink()+"\tlinkToThis state:\tdriver.getNumberOfOpenWindows():"+driver.getNumberOfOpenWindows());		
		}
		this.crawlerInfo.appendLog("\n\n##Web-Driver logs:\n"+this.driver.getLog());
	}
	
	
	
	
	private boolean goToNextState(Link link, String url, Frame frame, boolean process_page){
		boolean success = true;
		try{
			//try to go to new state via web driver
			if(url != null){
				success = driver.get(url);
			}
			else if(frame != null){
				success = driver.switchToFrame(FindFrameBy.index, frame.getIndex());
			}
			else if(link !=null){
				success = driver.clickElement(FindElementBy.xpath, link.getXpath(), false);
				// if fail try with the id xpath!!!
				if(!success && link.getXpath_by_id() != null && !link.getXpath_by_id().equals(link.getXpath()))
					success = driver.clickElement(FindElementBy.xpath, link.getXpath_by_id(), false);
			}
			//process current state if successfully manage to go there  
			if(success && process_page){
				WebPage currentWebPage = driver.getCurrentWebPage(0, crawlerSetUp.getFRAME_TAG_NAME_LIST(), crawlerSetUp.getLINK_TAG_NAME_LIST(), crawlerSetUp.getIMG_ATTR_WITH_TEXT_LIST());
				if(link!=null)
					currentWebPage.addLinkToThisWebPage(link);
				processCurrentState(currentWebPage);
			}
		}catch(WebDriverException e){
			crawlerInfo.appendLog("Exception durring goToNextState:"+Helper.getStackTrace(e)+"\n");
			return false;
		}
		return success;
	}
	
	
	private void processCurrentState(WebPage currentWebPage) {
		/*
		 * TODO Filter black list pages
		 */
		//if(this.crawlerSetUp.getBLACK_LIST_URL() != null && !this.crawlerSetUp.getBLACK_LIST_URL().isEmpty()){
			
		
		/*
		 * Filter Links and frames
		 */
		// filter the outlinks of this state based on the previous selected links and a static stop anchor text list
		List<Link> state_links = currentWebPage.getLinks();
		state_links = filterPreviousFollowedLinks(state_links);
		//filter frames
		List<Frame> state_frames = currentWebPage.getFrames();
		state_frames = filterPreviousFollowedFrames(state_frames);
		//TODO filter links and FRAMES that include stop anchor text, such as social network links
//		if(!crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT().isEmpty())
//			state_links = filterStopLinksBasedOnStopAnchorTextList(state_links);
		
		
		// classify web page's links and web page, if there is link classifier
		if(this.crawlerSetUp.getLink_classifier()!=null){
			this.crawlerSetUp.getLink_classifier().setImportanceScoreToLinks(state_links, this.crawlerSetUp.getTokenizer());
			Collections.sort(state_links,Link.LinkScoreComparator);
			
			// classify web page as semantic or not, if there is a page classifier
			this.crawlerSetUp.getLink_classifier().classifyWebPage(currentWebPage, this.crawlerSetUp.getTokenizer());

			// save current web page if is semantic
			if(currentWebPage.getClassification().equals("1") && !urlSetThatWeVisit.contains(currentWebPage.getUrl())){
				semanticWebPageList.add(currentWebPage);
				this.crawlerInfo.increaseByOneSemantics();
			}	
		}
		
		
		
		// increase number of unique visits if is a new one
		if(!urlSetThatWeVisit.contains(currentWebPage.getUrl()))
			this.crawlerInfo.increaseByOneUniquePages();
		
		
		// TODO if config.keepTopNLinks from each state is set up then keep only the top N links
		currentWebPage.setLinks(state_links);
		currentWebPage.setFrames(state_frames);
		
		// set current state to current web page
		current_state = new StateImpl(currentWebPage);
		// save url of current state in order not to visit again
		urlSetThatWeVisit.add(currentWebPage.getUrl());
	}
	

	private List<Link> filterPreviousFollowedLinks(List<Link> links){
		List<Link> filteredLinks = new ArrayList<Link>();
		for(Link link:links){
			if(!linkWeFollowHistoryList.contains(link))
				filteredLinks.add(link);
		}
		return filteredLinks;
	}
	
	private List<Frame> filterPreviousFollowedFrames(List<Frame> frames){
		List<Frame> filteredFrames = new ArrayList<Frame>();
		for(Frame frame:frames){
			if(!frameWeFollowHistoryList.contains(frame))
				filteredFrames.add(frame);			
		}
		return filteredFrames;
	}
	

	
	@Override
	public List<WebPage> getSemanticWebPages() {
		return semanticWebPageList;
	}
	
	@Override
	public CrawlerInfo getInfo() {
		return this.crawlerInfo;
	}
	
	@Override
	public CrawlerSetUp getConfig() {
		return crawlerSetUp;
	}

	@Override
	public void end() {
		this.driver.quit();		
	}





	@Override
	public Set<String> getUrlSetThatWeVisited() {
		return urlSetThatWeVisit;
	}
	
}
