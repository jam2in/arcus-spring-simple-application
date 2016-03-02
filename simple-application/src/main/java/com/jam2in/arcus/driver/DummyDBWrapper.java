package com.jam2in.arcus.driver;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.Map.Entry;

@CacheConfig(keyGenerator = "arcusKeyGenerator")
public class DummyDBWrapper implements DummyDBInterface {

  /* Map<User ID, User Name> */
  private Map<String, String> userMap =
          Collections.synchronizedMap(new HashMap<String, String>());
  /* Map<User ID, Map<Article Date, Article>> */
  private Map<String, TreeMap<Long, String>> articleMap =
          Collections.synchronizedMap(new HashMap<String, TreeMap<Long, String>>());

  public Map<String, String> getUserMap() {
    return userMap;
  }

  public Map<String, TreeMap<Long, String>> getArticleMap() {
    return articleMap;
  }

  public void connect() {
    // System.err.println("Can't connect Database");
    System.out.println("Connect database");
  }

  public void disconnect() {
    System.out.println("Disconnect database");
  }

  @CachePut(cacheNames = "user")
  public String insertUser(String uid, String name) {
    if (userMap.containsKey(uid) || articleMap.containsKey(uid))
      return null;

    userMap.put(uid, name);
    articleMap.put(uid, new TreeMap<Long, String>());

    return name;
  }

  @Cacheable(cacheNames = "user")
  public String getUser(String uid) {
    return userMap.get(uid);
  }

  @CacheEvict(cacheNames = {"user", "article"})
  public boolean deleteUser(String uid) {
    articleMap.remove(uid);
    if (userMap.remove(uid) == null)
      return false;
    return true;
  }

  @CachePut(cacheNames = "article")
  public String insertArticle(String uid, long date, String contents) {
    assert userMap.get(uid) != null;

    Map<Long, String> allArticles = articleMap.get(uid);

    if (allArticles == null) {
      articleMap.put(uid, new TreeMap<Long, String>());
      //System.err.printf("Can't find articles list of %s for insert article from database.\n", uid);
      //return null;
    }

    if (allArticles.containsKey(date)) {
      return null;
    }

    allArticles.put(date, contents);
    return contents;
  }

  @Cacheable(cacheNames = "article")
  public Map<Long, String> getArticles(String uid, long date, int count) {
    TreeMap<Long, String> allArticles = articleMap.get(uid);

    if (allArticles == null) {
      System.err.printf("Can't find articles list of %s for get article from database.\n", uid);
      return null;
    }

    Map<Long, String> articles = new HashMap<Long, String>();
    Iterator<Entry<Long, String>> iter = allArticles.descendingMap().entrySet().iterator();
    Entry<Long, String> e = null;
    for (int i = 0; i < count && iter.hasNext(); i++) {
      e = iter.next();
      if (e.getKey() <= date)
        articles.put(e.getKey(), e.getValue());
    }

    return articles;
  }

  @Cacheable(cacheNames = "article")
  public Map<Long, String> getArticlesBetween(String uid, long fromDate, long toDate) {
    TreeMap<Long, String> allArticles = articleMap.get(uid);

    if (allArticles == null) {
      System.err.printf("Can't find articles list of %s for get article between from database.\n", uid);
      return null;
    }

    Map<Long, String> articles = allArticles.subMap(fromDate, toDate);
    if (articles.isEmpty()) {
      System.err.printf("don't exist articles between from database\n", uid);
    }
    return articles;
  }

  @CacheEvict(cacheNames = "article")
  public boolean deleteArticle(String uid, long date) {
    Map<Long, String> allArticles = articleMap.get(uid);

    if (allArticles == null) {
      System.err.printf("Can't find articles list of %s for delete article form database.\n", uid);
      return false;
    }

    if (allArticles.remove(date) == null) {
      System.err.printf("Can't find articles (%s : %s) to delete from database.\n", uid, new Date(date));
      return false;
    }

    return true;
  }

  @CacheEvict(cacheNames = "article")
  public void deleteArticlesBetween(String uid, long fromDate, long toDate) {
    TreeMap<Long, String> allArticles = articleMap.get(uid);

    if (allArticles == null) {
      System.err.printf("Can't find articles list of %s for delete article between from database.\n", uid);
    } else {
      Map<Long, String> toDeleteSubMap = allArticles.subMap(fromDate, toDate);

      for (long date : toDeleteSubMap.keySet())
        allArticles.remove(date);
    }
  }

  @CacheEvict(cacheNames = {"user", "article"}, allEntries = true)
  public void deleteAllUsers() {
    articleMap.clear();
    userMap.clear();
  }

  @CacheEvict(cacheNames = "article", allEntries = true)
  public void deleteAllArticles() {
    articleMap.clear();
  }
}
