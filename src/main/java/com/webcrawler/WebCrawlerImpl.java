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
	private String seed_url;
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
		this.seed_url = seed_url;
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
				success = driver.goToWebPageViaUrlOrSeedUrl(url, this.seed_url, current_state!=null ? current_state.getWebPage().getXpaths_or_frame_index_to_this_page() : null);				
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
				processCurrentState(currentWebPage, link,  frame);
			}
		}catch(WebDriverException e){
			crawlerInfo.appendLog("Exception durring goToNextState:"+Helper.getStackTrace(e)+"\n");
			return false;
		}
		return success;
	}
	
	
	private void processCurrentState(WebPage currentWebPage,Link link_to_this_page, Frame frame_to_this_page) {

		/*
		 * Filter Links and frames
		 */
		// filter the outlinks of this state based on the previous selected links and a static stop anchor text list
		List<Link> state_links = currentWebPage.getLinks();
		state_links.removeIf(lk->linkWeFollowHistoryList.contains(lk));
		//filter frames		
		List<Frame> state_frames = currentWebPage.getFrames();
		state_frames.removeIf(fr->frameWeFollowHistoryList.contains(fr));
		
		
		
		
		//filter links and FRAMES that include stop anchor href or src, such as social network links
		if(!crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT().isEmpty()){
			Helper.filterHtmlElementBasedOnStopAnchorTextList(state_links,crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT());
			Helper.filterHtmlElementBasedOnStopAnchorTextList(state_frames,crawlerSetUp.getBLACK_LIST_ANCHOR_TEXT());
		}
		
		
		// classify web page's links and web page, if there is link classifier
		if(this.crawlerSetUp.getLink_classifier()!=null){
			this.crawlerSetUp.getLink_classifier().setImportanceScoreToLinks(state_links, this.crawlerSetUp.getTokenizer());
			Collections.sort(state_links,Link.LinkScoreComparator);
			
			//set importance score to frames...
			this.crawlerSetUp.getLink_classifier().setImportanceScoreToFrames(state_frames, this.crawlerSetUp.getTokenizer());
			
			// classify web page as semantic or not, if there is a page classifier
			this.crawlerSetUp.getLink_classifier().classifyWebPage(currentWebPage, this.crawlerSetUp.getTokenizer());

			// save current web page if is semantic and is not belong to black list urls...
			if(currentWebPage.getClassification().equals("1") && !urlSetThatWeVisit.contains(currentWebPage.getUrl()) && (this.crawlerSetUp.getBLACK_LIST_URL() != null && !this.crawlerSetUp.getBLACK_LIST_URL().contains(currentWebPage.getUrl()))){
				//save to current web page the links or frame that we follow to come here..
				savePathToThisState(currentWebPage, link_to_this_page, frame_to_this_page);
				semanticWebPageList.add(currentWebPage);
				this.crawlerInfo.increaseByOneSemantics();
			}	
		}
		
		
		
		// increase number of unique visits if is a new one
		if(!urlSetThatWeVisit.contains(currentWebPage.getUrl()))
			this.crawlerInfo.increaseByOneUniquePages();
		
		
		//  if config.keepTopNLinks from each state is set up then keep only the top N links
		state_links.removeIf(lk -> lk.getScore() == 0.0);

		
		//for depth greater or equal to 3 set only one link to extarct
		int nr_of_links_to_extract_from_current_state = this.current_depth >= 3 ? 2 : this.crawlerSetUp.getMax_number_links_to_keep_from_a_state();
		int toIndex = state_links.size() > nr_of_links_to_extract_from_current_state ? nr_of_links_to_extract_from_current_state : state_links.size();
		currentWebPage.setLinks(state_links.subList(0, toIndex));

		
		//keep only the positive scored frames...		
		state_frames.removeIf(f -> f.getScore() == 0.0);
		
		
		// set current state to current web page
		current_state = new StateImpl(currentWebPage);
		// save url of current state in order not to visit again
		urlSetThatWeVisit.add(currentWebPage.getUrl());
	}
	
	/**
	 * Only for the semantic pages we save how we reach them(what links or frames we followed) 
	 * @param currentWebPage
	 * @param link_to_this_page
	 * @param frame_to_this_page
	 */
	private void savePathToThisState(WebPage currentWebPage,Link link_to_this_page, Frame frame_to_this_page){
		//save the link's xpath or 
		if(link_to_this_page != null || frame_to_this_page != null){
			List<String> xpaths_or_frame_index_to_this_page_list = new ArrayList<String>(current_state.getWebPage().getXpaths_or_frame_index_to_this_page());
			if(link_to_this_page != null)
				xpaths_or_frame_index_to_this_page_list.add(link_to_this_page.getXpath_by_id());
			else
				xpaths_or_frame_index_to_this_page_list.add(Integer.toString(frame_to_this_page.getIndex()));
			currentWebPage.setXpaths_or_frame_index_to_this_page(xpaths_or_frame_index_to_this_page_list);
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
