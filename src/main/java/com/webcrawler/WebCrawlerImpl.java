package com.webcrawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
	
	private final long startTime;
	
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
		startTime = System.currentTimeMillis()/1000;
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
					//System.out.println(success+" frameToFollow:"+frameToFollow.getAttributesMap().toString());
				}
				else{
					Link linkToFollow = current_state.nextLink();
					linkWeFollowHistoryList.add(linkToFollow);
					success = goToNextState(linkToFollow, null,null,true);
					//System.out.println(success+" linkToFollow:"+linkToFollow.getText()+"\t"+linkToFollow.getAttributesMap().toString());
				}
				//update depth and current state in current depth
				if(success){
					current_depth++;
					last_state_per_depth_level[current_depth] = current_state;
					//System.out.println("#depth:"+current_depth+"\t"+current_state.getWebPage().getUrl()+"\tlinks:"+current_state.getWebPage().getLinks().size()+"\tframes:"+current_state.getWebPage().getFrames().size()+"\tsuccess:"+success+"\tcurrent_state.hasNext():"+current_state.hasNextLink()+"\tdriver.getNumberOfOpenWindows():"+driver.getNumberOfOpenWindows());
				}	
				//else we are in the same state as before to try to go to next state
			}
			//go back one depth level, except if we are on the seed url(depth = 0)
			current_depth = current_depth == 0 ? 0 : current_depth - 1;
			current_state = last_state_per_depth_level[current_depth];

			//check stopping criteria
			if(stopCrawling())
				break;
			
			//if continue then go to state of the current depth back...
			success = goToNextState(null, current_state.getWebPage().getUrl(),null,false);
			//System.out.println("#depth:"+current_depth+"\t"+current_state.getWebPage().getUrl()+"\tlinks:"+current_state.getWebPage().getLinks().size()+"\tframes:"+current_state.getWebPage().getFrames().size()+"\tsuccess:"+success+"\tcurrent_state.hasNext():"+current_state.hasNextLink()+"\tlinkToThis state:\tdriver.getNumberOfOpenWindows():"+driver.getNumberOfOpenWindows());		
		}
		this.crawlerInfo.appendLog("\n\n#Web-Driver logs:\n"+this.driver.getLog());
	}
	
	private boolean stopCrawling(){
		long time = (System.currentTimeMillis()/1000) - startTime;
		if( (time > this.crawlerSetUp.getMax_execution_time_seconds()) || (current_depth == 0 && (current_state == null || (!current_state.hasNextLink() && !current_state.hasNextFrame()))) || urlSetThatWeVisit.size() >= this.crawlerSetUp.getMax_number_states_to_visit())
			return true;
		else
			return false;
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
			//close alerts
			driver.closeAlerts();
			//go to new window if there is and close the current one.. 
			driver.switchToNewWindow(true);

			
			
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
		filterPreviousFollowedLinks(state_links);
		//filter frames
		List<Frame> state_frames = currentWebPage.getFrames();
		filterPreviousFollowedFrames(state_frames);
		
		
		//filter links and FRAMES that include stop anchor href or src, such as social network links
		if(!crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT().isEmpty()){
			filterLinksBasedOnStopAnchorTextList(state_links);
			filterFramesBasedOnStopAnchorTextList(state_frames);
		}
		
		
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

	
	
	private void filterFramesBasedOnStopAnchorTextList(List<Frame> frames){
		Iterator<Frame> frameIter = frames.iterator();
		while(frameIter.hasNext()){
			Frame frame = frameIter.next();
			String src = frame.getAttributesMap().get("src");
			if(src!=null){
				src = src.toLowerCase();
				for(String bad_text:crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT()){
					if(src.contains(bad_text)){
						frameIter.remove();
					}
				}	
			}
		}
	}

	private void filterLinksBasedOnStopAnchorTextList(List<Link> links){
		Iterator<Link> linkIter = links.iterator();
		while(linkIter.hasNext()){
			Link link = linkIter.next();
			String href = link.getAttributeValue("href");
			if(href!=null){
				href = href.toLowerCase();
				for(String bad_text:crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT()){
					if(href.contains(bad_text)){
						linkIter.remove();
					}
				}	
			}
		}
	}
	
	private void filterPreviousFollowedLinks(List<Link> links){
		Iterator<Link> linkIter = links.iterator();
		while(linkIter.hasNext()){
			Link link = linkIter.next();
			if(linkWeFollowHistoryList.contains(link))
				linkIter.remove();
		}
	}
	
	private void filterPreviousFollowedFrames(List<Frame> frames){
		Iterator<Frame> frameIter = frames.iterator();
		while(frameIter.hasNext()){
			Frame frame = frameIter.next();
			if(frameWeFollowHistoryList.contains(frame))
				frameIter.remove();			
		}
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
