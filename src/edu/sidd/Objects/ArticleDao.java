package edu.sidd.Objects;

import java.util.HashMap;
import java.util.List;

public class ArticleDao {
	
	String articleName;
	String articleURL;
	String articleId;
	HashMap <String,String> sectionContentList;
	
	public String getArticleId() {
		return articleId;
	}
	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}
	public String getArticleName() {
		return articleName;
	}
	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}
	public String getArticleURL() {
		return articleURL;
	}
	public void setArticleURL(String articleURL) {
		this.articleURL = articleURL;
	}
	public HashMap<String, String> getSectionContentList() {
		return sectionContentList;
	}
	public void setSectionContentList(
			HashMap<String, String> sectionContentList) {
		this.sectionContentList = sectionContentList;
	}
	

}
