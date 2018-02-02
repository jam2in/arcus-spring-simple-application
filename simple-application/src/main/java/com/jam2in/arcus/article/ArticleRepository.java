package com.jam2in.arcus.article;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Scope("prototype")
@Repository
public class ArticleRepository {

	private static final Logger logger = LoggerFactory.getLogger(ArticleRepository.class);

	private Map<Integer, Article> database = new LinkedHashMap<Integer, Article>();

	public ArticleRepository() {
		logger.info("ArticleRepository()");
	}

	public Article insert(Article article) {
    	logger.info("insert(Article article={})", article);
		database.put(article.getId(), article);
		return article;
	}

	public Article insertIgnoreId(Article article) {
    	logger.info("insertIgnoreId(Article article={})", article);
		database.put(database.size(), article);
		return article;
	}

	public Article update(Article article) {
		logger.info("update(Article article={})", article);
		database.put(article.getId(), article);
		return article;
	}

	public Article select(int id) {
		logger.info("select(int id={})", id);
		return database.get(id);
	}

	public void delete(int id) {
		logger.info("delete(int id={})", id);
		database.remove(id);
	}

	public void deleteAll() {
		logger.info("deleteAll()");
		database.clear();
	}

}
