package com.webcrawler.domain;

public class CrawlerInfo {
	private StringBuilder log;
	private int nr_of_unique_states_visit;
	private int nr_of_semantic_states_visit;
	
	
	public CrawlerInfo() {
		super();
		this.log = new StringBuilder();
	}


	public void increaseByOneSemantics(){
		nr_of_semantic_states_visit++;
	}

	public void increaseByOneUniquePages(){
		nr_of_unique_states_visit++;
	}

	public String getLog() {
		return log.toString();
	}


	public void setLog(StringBuilder log) {
		this.log = log;
	}


	public int getNr_of_unique_states_visit() {
		return nr_of_unique_states_visit;
	}


	public void setNr_of_unique_states_visit(int nr_of_unique_states_visit) {
		this.nr_of_unique_states_visit = nr_of_unique_states_visit;
	}


	public int getNr_of_semantic_states_visit() {
		return nr_of_semantic_states_visit;
	}


	public void setNr_of_semantic_states_visit(int nr_of_semantic_states_visit) {
		this.nr_of_semantic_states_visit = nr_of_semantic_states_visit;
	}

	
	public void appendLog(String log){
		this.log.append(log+"\n");
	}


	@Override
	public String toString() {
		return "CrawlerInfo [\nlog=" + log.toString() + ", \nnr_of_unique_states_visit="
				+ nr_of_unique_states_visit + ", \nnr_of_semantic_states_visit="
				+ nr_of_semantic_states_visit + "]";
	}
	
	
	
}
