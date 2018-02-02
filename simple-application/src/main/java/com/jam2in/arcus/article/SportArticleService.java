package com.jam2in.arcus.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@CacheConfig(cacheNames = "sport") // sportArticleCache(ArcusCache)
@Service
public class SportArticleService {

	@Autowired
	private ArticleRepository articleRepository;

	//-------------------------------------------------------
	// 뉴스 기사 생성
	//-------------------------------------------------------
	public Article createIgnoreId(Article article) {
		return articleRepository.insertIgnoreId(article);
	}

	public Article create(Article article) {
		return articleRepository.insert(article);
	}

	public Article create(int id, String content) {
		return articleRepository.insert(new Article(id, content));
	}

	//-------------------------------------------------------
	// 뉴스 기사 수정
	//-------------------------------------------------------
	@CachePut(key = "#article.id")
	public Article update(Article article) {
		return articleRepository.update(article);

/*
		// @CachePut: sportArticleCache의 id(key)에 return 값을 삽입한다. //

		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		sportArticleCache.put(article.getId(), articleRepository.update(article));
*/
	}

	@CachePut(key = "#id")
	public Article update(int id, String content) {
		Article article = new Article(id, content);
		return articleRepository.update(article);

/*
		// @CachePut: sportArticleCache의 id(key)에 return 값을 삽입한다. //

		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		sportArticleCache.put(id, articleRepository.update(article));
*/
	}


	//-------------------------------------------------------
	// 뉴스 기사 가져오기
	//-------------------------------------------------------
	@Cacheable(key = "#id")
	public Article get(int id) {
		return articleRepository.select(id);

/*
		// @Cacheable: sportArticleCache에 id(key)가 없으면 return 값을 sportArticleCache의 id(key)에 삽입한다. //

		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		ValueWrapper vw = sportArticleCache.get(id);
		if (vw == null) {
			sportArticleCache.put(id, arcusRepository.select(id));
		}
*/
	}

	@Cacheable
	public Article getWithKeyGenerator(int id) {
		return articleRepository.select(id);

/*
		// @Cacheable, @CachePut, @CacheEvict: key 속성이 없으면 메소드의 파라미터들을 가지고 StringKeyGenerator에 의해 key를 생성한다. //

		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		StringKeyGenerator articleKeyGenerator = (StringKeyGenerator) simpleContext.getBean("articleKeyGenerator");
		ArcusStringKey arcusStringKey = articleKeyGenerator.generate(target, method, new Object[] {id, ...});

		ValueWrapper vw = sportArticleCache.get(arcusStringKey);
		if (vw == null) {
			sportArticleCache.put(arcusStringKey, arcusRepository.select(id));
		}
*/
	}


	//-------------------------------------------------------
	// 뉴스 기사 삭제
	//-------------------------------------------------------
	@CacheEvict(key = "#id")
	public void remove(int id) {
		articleRepository.delete(id);

/*
		// @CacheEvict:  sportArticleCache에 id(key)를 삭제한다. //

		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		articleRepository.delete(id);
		sportArticleCache.evict(id);
*/
	}

	@CacheEvict(allEntries = true)
	public void removeAll() {
		articleRepository.deleteAll();

/*
		// @CacheEvict(allEntries = true): sportArticleCache에 모든 id(key)를 삭제한다. //

		ArcusCache sportArticleCache = simpleContext.getBean("sportArticleCache");

		articleRepository.deleteAll();
		sportArticleCache.clear();
*/
	}

}
