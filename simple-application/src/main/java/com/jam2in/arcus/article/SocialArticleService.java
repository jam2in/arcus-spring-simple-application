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

	<bean id="socialArticleCache" parent="defaultArcusCache">
		<property name="name" value="social"/>
		...
	</bean>
 */
@CacheConfig(cacheNames = "social") // socialArticleCache bean의 name
@Service
public class SocialArticleService {

	@Autowired
	private ArticleRepository articleRepository; // DB

}
