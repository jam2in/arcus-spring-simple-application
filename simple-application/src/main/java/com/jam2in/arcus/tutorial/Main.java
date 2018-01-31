package com.jam2in.arcus.tutorial;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jam2in.arcus.tutorial.article.Article;
import com.jam2in.arcus.tutorial.article.SportArticleService;

public class Main {

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("tutorialContext.xml");

		SportArticleService sportArticleService = (SportArticleService) applicationContext.getBean("sportArticleService");

		final int ARTICLE_ID_MANUTD_CHELSEA = 0;
		final int ARTICLE_ID_ARSENAL_LIVERPOOL = 1;

		// DB에 Article 삽입 (Cache에 반영 안됨)
		sportArticleService.create(new Article(ARTICLE_ID_MANUTD_CHELSEA, "MANUTD 3 : 3 CHELSEA"));
		sportArticleService.create(new Article(ARTICLE_ID_ARSENAL_LIVERPOOL, "ARSENAL 2 : 2 LIVERPOOL"));

		// CACHE에 key:ARTICLE_ID_MANUTD_CHELSEA가 없으면 DB(ArticleRepository)에 읽어와서 CACHE에 반영한다.
		// CACHE에 key:ARTICLE_ID_MANUTD_CHELSEA가 있으면 아무런 작업을 하지 않는다.
		sportArticleService.get(ARTICLE_ID_MANUTD_CHELSEA);

		// DB에 해당 Article을 update하고, CACHE에도 update 한다.
		sportArticleService.update(new Article(ARTICLE_ID_MANUTD_CHELSEA, "MANUTD 3 : 4 CHELSEA"));

		//  DB에 해당 Article을 읽어오지 않는다. 왜냐하면 CACHE에 key:ARTICLE_ID_MANUTD_CHELSEA가 존재하기 때문
		sportArticleService.get(ARTICLE_ID_MANUTD_CHELSEA);

		// DB에 해당 Article을 지우고, CACHE에도 해당 key:ARTICLE_ID_MANUTD_CHELSEA를 지운다.
		sportArticleService.remove(ARTICLE_ID_MANUTD_CHELSEA);

		// DB에 모든 Article을 지우고, CACHE에도 모든 key를 지운다.
		sportArticleService.removeAll();

		// CACHE에 key:ARTICLE_ID_MANUTD_CHELSEA는 존재하지 않으므로, DB에 해당 Article을 읽는다.
		sportArticleService.get(ARTICLE_ID_MANUTD_CHELSEA);
	}

}
