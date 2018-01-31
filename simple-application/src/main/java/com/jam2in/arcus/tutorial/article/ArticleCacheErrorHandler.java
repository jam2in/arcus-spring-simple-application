package com.jam2in.arcus.tutorial.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class ArticleCacheErrorHandler implements CacheErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(ArticleCacheErrorHandler.class);
	
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		logger.error("handleCacheGetError(RuntimeException exception={}, Cache cache={}, Object key={})", 
			exception.getMessage(), 
			cache.getName(), 
			key
		);
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		logger.error("handleCachePutError(RuntimeException exception=%s, Cache cache={}, Object key={}, Object value={})", 
			exception.getMessage(), 
			cache.getName(), 
			key,
			value
		);
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		logger.error("handleCacheEvictError(RuntimeException exception={}, Cache cache={}, Object key={})", 
			exception.getMessage(), 
			cache.getName(), 
			key
		);	
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		logger.error("handleCacheClearError(RuntimeException exception={}, Cache cache={})", 
			exception.getMessage(), 
			cache.getName() 
		);	
	}

}
