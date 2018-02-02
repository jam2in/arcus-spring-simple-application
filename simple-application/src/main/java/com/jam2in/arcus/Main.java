package com.jam2in.arcus;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jam2in.arcus.article.Article;
import com.jam2in.arcus.article.SportArticleService;

public class Main {

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("simpleContext.xml");

		SportArticleService sportArticleService = (SportArticleService) applicationContext.getBean("sportArticleService");

		final int SPORTS_ARTICLE_ID_FOOTBALL = 0;
		final int SPORTS_ARTICLE_ID_BASKETBALL = 1;

		// DB=ArticleRepository
		// CACHE=ArcusCache(sportArticleCache)

		// DB에 key=SPORTS_ARTICLE_ID_FOOTBALL의 Article을 저장한다. CACHE에는 저장안함.
		sportArticleService.create(new Article(SPORTS_ARTICLE_ID_FOOTBALL, "ManUTD 3 : 3 Chelsea"));
		// DB에 key=SPORTS_ARTICLE_ID_BASKETBALL Article을 저장한다. CACHE에는 저장안함.
		sportArticleService.create(new Article(SPORTS_ARTICLE_ID_BASKETBALL, "Chicago Bulls 70 : 70 Utah Jazz"));

		// CACHE에 key=SPORTS_ARTICLE_ID_FOOTBALL이 없으면 DB에 읽어와서 CACHE에 저장하고 값을 받는다.
		// CACHE에 key=SPORTS_ARTICLE_ID_FOOTBALL이 있으면 DB에 읽어오지 않고 CACHE에서 읽어오고 값을 받는다.
		sportArticleService.get(SPORTS_ARTICLE_ID_FOOTBALL);
		// CACHE에 key=SPORTS_ARTICLE_ID_BASKETBALL 없으면 DB에 읽어와서 CACHE에 저장하고 값을 받는다.
		// CACHE에 key=SPORTS_ARTICLE_ID_BASKETBALL 있으면 DB에 읽어오지 않고 CACHE에서 읽어오고 값을 받는다.
		sportArticleService.get(SPORTS_ARTICLE_ID_BASKETBALL);

		// DB에 key=SPORTS_ARTICLE_ID_FOOTBALL의 Article을 갱신하고, CACHE에도 갱신한다.
		sportArticleService.update(new Article(SPORTS_ARTICLE_ID_FOOTBALL, "MANUTD 3 : 4 CHELSEA"));

		// DB에 key=SPORTS_ARTICLE_ID_FOOTBALL의 Article을 읽어오지 않고. CACHE에서 읽어온다. 왜냐하면 CACHE에 key=SPORTS_ARTICLE_ID_FOOTBALL이 존재하기 때문
		sportArticleService.get(SPORTS_ARTICLE_ID_FOOTBALL);

		// DB에 key=SPORTS_ARTICLE_ID_FOOTBALL의 Article을 제거하고, CACHE에도 제거 한다.
		sportArticleService.remove(SPORTS_ARTICLE_ID_FOOTBALL);

		// DB에 모든 Article을 제거하고, CACHE에도 모든 Article을 지운다.
		sportArticleService.removeAll();

	}

}
