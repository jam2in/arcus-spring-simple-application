package com.jam2in.arcus.tutorial;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jam2in.arcus.tutorial.article.Article;
import com.jam2in.arcus.tutorial.article.SportArticleService;

public class Main {

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("tutorialContext.xml");

		SportArticleService sportArticleService = (SportArticleService) applicationContext.getBean("sportArticleService");

		final int SPORTS_ARTICLE_ID_FOOTBALL = 0;
		final int SPORTS_ARTICLE_ID_BASKETBALL = 1;

		// DB에 Article 삽입 (Cache에 저장 안됨)
		sportArticleService.create(new Article(SPORTS_ARTICLE_ID_FOOTBALL, "ManUTD 3 : 3 Chelsea"));
		sportArticleService.create(new Article(SPORTS_ARTICLE_ID_BASKETBALL, "Chicago Bulls 70 : 70 Utah Jazz"));

		// CACHE에 key=SPORTS_ARTICLE_ID_FOOTBALL가 없으면 DB(ArticleRepository)에 읽어와서 CACHE에 저장한다.
		// CACHE에 key=SPORTS_ARTICLE_ID_FOOTBALL가 있으면 아무런 작업을 하지 않는다.
		sportArticleService.get(SPORTS_ARTICLE_ID_FOOTBALL);
		sportArticleService.get(SPORTS_ARTICLE_ID_BASKETBALL);

		// DB에 해당 Article을 update하고, CACHE에도 update 한다.
		sportArticleService.update(new Article(SPORTS_ARTICLE_ID_FOOTBALL, "MANUTD 3 : 4 CHELSEA"));

		//  DB에 해당 Article을 읽어오지 않는다. 왜냐하면 CACHE에 key=SPORTS_ARTICLE_ID_FOOTBALL가 존재하기 때문
		sportArticleService.get(SPORTS_ARTICLE_ID_FOOTBALL);

		// DB에 해당 Article을 지우고, CACHE에도 해당 key=SPORTS_ARTICLE_ID_FOOTBALL를 지운다.
		sportArticleService.remove(SPORTS_ARTICLE_ID_FOOTBALL);

		// DB에 모든 Article을 지우고, CACHE에도 모든 key를 지운다.
		sportArticleService.removeAll();

		// CACHE에 key=SPORTS_ARTICLE_ID_FOOTBALL는 존재하지 않으므로, DB에 해당 Article을 읽는다.
		sportArticleService.get(SPORTS_ARTICLE_ID_FOOTBALL);

	}

}
