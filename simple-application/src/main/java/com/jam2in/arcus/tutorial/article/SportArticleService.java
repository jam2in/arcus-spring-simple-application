package com.jam2in.arcus.tutorial.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@CacheConfig(cacheNames = "sportArticleCache")
@Service
public class SportArticleService {

	@Autowired
	private ArticleRepository articleRepository;

	//-------------------------------------------------------
	// 뉴스 기사 생성
	//-------------------------------------------------------
	public void createIgnoreId(Article article) {
		articleRepository.insertIgnoreId(article);
	}

	public String create(Article article) {
		return articleRepository.insert(article);
	}

	public String create(int id, String content) {
		return articleRepository.insert(new Article(id, content));
	}

	//-------------------------------------------------------
	// 뉴스 기사 수정
	//-------------------------------------------------------
	@CachePut(key = "#article.id") // sportArticleCache의 id(key)에 return을 삽입한다.
	public String update(Article article) {
		return articleRepository.update(article);
	}

	@CachePut(key = "#id") // sportArticleCache의 id(key)에 return을 삽입한다.
	public void update(int id, String content) {
		articleRepository.update(new Article(id, content));
	}


	//-------------------------------------------------------
	// 뉴스 기사 가져오기
	//-------------------------------------------------------
	@Cacheable(key = "#id") // sportArticleCache에 id(key)가 없으면 return을 sportArticleCache의 id(key)에 삽입한다. (return이 Object인 경우 Object의 toString()이 삽압됨.)
	public String get(int id) {
		return articleRepository.select(id);
	}


	//-------------------------------------------------------
	// 뉴스 기사 삭제
	//-------------------------------------------------------
	@CacheEvict(key = "#id") // sportArticleCache에 id(key)를 삭제한다.
	public void remove(int id) {
		articleRepository.delete(id);
	}

	@CacheEvict(allEntries = true) // sportArticleCache에 모든 id(key)를 삭제한다.
	public void removeAll() {
		articleRepository.deleteAll();
	}

}
