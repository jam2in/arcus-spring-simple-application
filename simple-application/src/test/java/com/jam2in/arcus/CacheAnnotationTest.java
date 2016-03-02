package com.jam2in.arcus;

import com.jam2in.arcus.driver.DummyDBInterface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by iceru on 2016. 2. 25..
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/simpleAppContext.xml")
public class CacheAnnotationTest {
  @Autowired
  ApplicationContext ctx;
  @Autowired
  DummyDBInterface dbWrapper;

  @Before
  public void setup() {
    dbWrapper.getUserMap().put("user2", "name2");
    dbWrapper.getArticleMap().put("user2", new TreeMap<Long, String>());
    dbWrapper.getArticleMap().get("user2").put(234L, "contents2");
    dbWrapper.getArticleMap().get("user2").put(345L, "contents3");
  }

  @After
  public void teardown() {
    dbWrapper.deleteAllUsers();
  }

  @Test
  public void putAndGetTest() {
    dbWrapper.insertUser("user1", "name1");
    assertThat(dbWrapper.getUser("user1"), is("name1"));
    dbWrapper.getUser("user2");

    dbWrapper.insertArticle("user1", 123L, "contents1");
    assertThat(dbWrapper.getArticles("user1", 123L, 10).get(123L), is("contents1"));
    dbWrapper.getArticles("user2", 234L, 10);
    dbWrapper.getArticlesBetween("user2", 344L, 346L);
  }

  @Test
  public void evictTest() {
    dbWrapper.getUser("user2");
    dbWrapper.getArticles("user2", 234L, 10);
    dbWrapper.getArticlesBetween("user2", 344L, 346L);

    dbWrapper.deleteArticle("user2", 234L);
    assertThat(dbWrapper.getArticles("user2", 234L, 10).size(), is(0));
    dbWrapper.deleteArticlesBetween("user2", 344L, 346L);
    assertThat(dbWrapper.getArticlesBetween("user2", 344L, 346L).size(), is(0));
    dbWrapper.deleteUser("user2");
    assertNull(dbWrapper.getUser("user2"));
  }

  @Test
  public void evictAllArticlesTest() {
    dbWrapper.getUser("user2");
    dbWrapper.getArticles("user2", 234L, 10);
    dbWrapper.getArticlesBetween("user2", 344L, 346L);

    dbWrapper.deleteAllArticles();
    assertNull(dbWrapper.getArticles("user2", 234L, 10));
    assertNull(dbWrapper.getArticlesBetween("user2", 344L, 346L));
    assertThat(dbWrapper.getUser("user2"), is("name2"));
  }

  @Test
  public void evictAllUsersTest() {
    dbWrapper.getUser("user2");
    dbWrapper.getArticles("user2", 234L, 10);
    dbWrapper.getArticlesBetween("user2", 344L, 346L);

    dbWrapper.deleteAllUsers();
    assertNull(dbWrapper.getArticles("user2", 234L, 10));
    assertNull(dbWrapper.getArticlesBetween("user2", 344L, 346L));
    assertNull(dbWrapper.getUser("user2"));
  }
}
