package com.jam2in.arcus.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/*
	<bean id="defaultArcusCache" class="com.navercorp.arcus.spring.cache.ArcusCache"
		abstract="true">
		...
	</bean>

	<bean id="sportArticleCache" parent="defaultArcusCache">
		<property name="name" value="sport"/>
		...
	</bean>
 */
@CacheConfig(cacheNames = "sport") // sportArticleCache bean의 name
@Service
public class SportArticleService {

	@Autowired
	private ArticleRepository articleRepository; // DB

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

	//--------------------------------------------------------------------------------------------------------------
	// 뉴스 기사 수정
	//--------------------------------------------------------------------------------------------------------------

	// @CachePut
	// sportArticleCache의 key(#article.id)에 return 값을 삽입한다.
	@CachePut(key = "#article.id")
	public Article update(Article article) {
		return articleRepository.update(article);

		/*
		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		sportArticleCache.put(article.getId(), articleRepository.update(article));
		return article;
		*/
	}

	// @CachePut
	// sportArticleCache의 key(#id)에 return 값을 삽입한다.
	@CachePut(key = "#id")
	public Article update(int id, String content) {
		return articleRepository.update(new Article(id, content));

		/*
		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		sportArticleCache.put(id, articleRepository.update(article));
		return article;
		*/
	}


	//--------------------------------------------------------------------------------------------------------------
	// 뉴스 기사 가져오기
	//--------------------------------------------------------------------------------------------------------------

	// @Cacheable
	// sportArticleCache에 key(#id)가 없으면 articleRepository.select(id)의 return 값을 sportArticleCache의 key(#id)에 삽입하고 return 한다.
	// sportArticleCache에 key(#id)가 있으면 articleRepository.select(id)를 실행하지 않고, sportArticleCache의 key(#id)를 가져와서 return 한다.
	@Cacheable(key = "#id")
	public Article get(int id) {
		return articleRepository.select(id);

		/*
		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		ValueWrapper vw = sportArticleCache.get(id);
		if (vw == null) {
			Article article = articleRepository.select(id);
			sportArticleCache.put(id, article);
			vw = new SimpleValueWrapper(article);
		}

		return vw.get();
		*/
	}

	// @Cacheable, @CachePut, @CacheEvict
	// key 속성이 없으면 메소드의 파라미터들을 가지고 StringKeyGenerator에 의해 key를 생성한다.
	@Cacheable
	public Article getWithKeyGenerator(int id) {
		return articleRepository.select(id);

		/*
		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		StringKeyGenerator articleKeyGenerator = (StringKeyGenerator) simpleContext.getBean("articleKeyGenerator");
		ArcusStringKey arcusStringKey = articleKeyGenerator.generate(target, method, new Object[] {id, ...});

		ValueWrapper vw = sportArticleCache.get(arcusStringKey);
		if (vw == null) {
			Article article = articleRepository.select(id);
			sportArticleCache.put(arcusStringKey, article);
			vw = new SimpleValueWrapper(article);
		}

		return vw.get();
		*/
	}


	//--------------------------------------------------------------------------------------------------------------
	// 뉴스 기사 삭제
	//--------------------------------------------------------------------------------------------------------------

	// @CacheEvict
	// sportArticleCache에 key(#id)를 삭제한다.
	@CacheEvict(key = "#id")
	public void remove(int id) {
		articleRepository.delete(id);

		/*
		ArcusCache sportArticleCache = (ArcusCache) simpleContext.getBean("sportArticleCache");

		articleRepository.delete(id);
		sportArticleCache.evict(id);
		*/
	}

	// @CacheEvict(allEntries = true)
	// sportArticleCache에 모든 key를 삭제한다.
	@CacheEvict(allEntries = true)
	public void removeAll() {
		articleRepository.deleteAll();

		/*
		ArcusCache sportArticleCache = simpleContext.getBean("sportArticleCache");

		articleRepository.deleteAll();
		sportArticleCache.clear();
		*/
	}

}
