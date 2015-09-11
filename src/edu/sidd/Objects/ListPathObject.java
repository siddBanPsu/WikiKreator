package edu.sidd.Objects;

import java.util.List;

public class ListPathObject {

	List<String> pathList;
	List<Double> tfIdfscoreList;
	List<Double> txRankScoreList;
	public List<String> getPathList() {
		return pathList;
	}
	public void setPathList(List<String> pathList) {
		this.pathList = pathList;
	}
	public List<Double> getTfIdfscoreList() {
		return tfIdfscoreList;
	}
	public void setTfIdfscoreList(List<Double> tfIdfscoreList) {
		this.tfIdfscoreList = tfIdfscoreList;
	}
	public List<Double> getTxRankScoreList() {
		return txRankScoreList;
	}
	public void setTxRankScoreList(List<Double> txRankScoreList) {
		this.txRankScoreList = txRankScoreList;
	}
	
	
}
