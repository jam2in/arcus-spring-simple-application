package com.jam2in.arcus.tutorial.article;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepository {

	private static final Logger logger = LoggerFactory.getLogger(ArticleRepository.class);

	private Map<Integer, String> database = new LinkedHashMap<Integer, String>();

	public ArticleRepository() {
		logger.info("ArticleRepository()");
	}

	public String insert(Article article) {
    logger.info("insert(Article article={})", article);
		database.put(article.getId(), article.getContent());
		return article.getContent();
	}

	public String insertIgnoreId(Article article) {
    logger.info("insertIgnoreId(Article article={})", article);
		database.put(database.size(), article.getContent());
		return article.getContent();
	}

	public String update(Article article) {
		logger.info("update(Article article={})", article);
		database.put(article.getId(), article.getContent());
		return article.getContent();
	}

	public String select(int id) {
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
