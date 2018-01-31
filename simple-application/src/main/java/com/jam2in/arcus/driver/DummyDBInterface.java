package com.jam2in.arcus.driver;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by iceru on 2016. 2. 23..
 */
public interface DummyDBInterface {
  public Map<String, String> getUserMap();
  public Map<String, TreeMap<Long, String>> getArticleMap();
  public void connect();
  public void disconnect();
  public String insertUser(String uid, String name);
  public String getUser(String uid);
  public boolean deleteUser(String uid);
  public String insertArticle(String uid, long date, String contents);
  public Map<Long, String> getArticles(String uid, long date, int count);
  public Map<Long, String> getArticlesBetween(String uid, long fromDate, long toDate);
  public boolean deleteArticle(String uid, long date);
  public void deleteArticlesBetween(String uid, long fromDate, long toDate);
  public void deleteAllUsers();
  public void deleteAllArticles();
}
