package com.jam2in.arcus;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import com.jam2in.arcus.driver.ArcusClientWrapper;
import com.jam2in.arcus.driver.DummyDBWrapper;
import com.jam2in.arcus.rand.AppAction;
import com.jam2in.arcus.rand.RandomActionGen;
import com.jam2in.arcus.rand.RandomDataGen;

public class SimpleApp extends Thread {
    private boolean running = true; 
    
    private ArcusClientWrapper arcusCli;
    private DummyDBWrapper dbCli;
    
    private RandomDataGen dataGen;
    private RandomActionGen actionGen;
    
    private Map<AppAction, Integer> statistics = 
                                    new LinkedHashMap<AppAction, Integer>(AppAction.values().length);
    
    private CountDownLatch latch;
    
    public SimpleApp(DummyDBWrapper d,
                     ArcusClientWrapper a,
                     RandomActionGen c,
                     CountDownLatch l) {
        this.arcusCli = a;
        this.dbCli = d;
        this.actionGen = c;
        this.latch = l;
        
        for (AppAction action : AppAction.values())
            statistics.put(action, 0);
    }
    
    private void buildInitData() {
        this.dataGen = new RandomDataGen(10000);
        
        for (String uid : dataGen.getRandomExistAllIdList()) {
            String name = dataGen.getRandomName();
            if (dbCli.insertUser(uid, name) != null)
                System.err.println(uid + " user initial data build failed at database");
            if (!arcusCli.insertUser(uid, name))
                System.err.println(uid + " user initial data build failed at arcus");
        }
        
        this.latch.countDown();
    }
    
    public void run() {
        System.out.println(this.getName() + " build initial data...");
        buildInitData();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        while (running) {
            try {
                AppAction action = actionGen.getRandomAction();
                switch(action) {
                    case INSERT_USER: {
                        /* insert user */
                        // System.out.println("inserts user");
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        insertUser(dataGen.getRandomId(), dataGen.getRandomName());
                        break;
                    }
                    case GET_USER: {
                        /* get user by uid */
                        // System.out.println("gets user");
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        @SuppressWarnings("unused")
                        String getName = getUser(dataGen.getRandomExistId());
                        break;
                    }
                    case DELETE_USER: {
                        /* deletes user by uid */
                        // System.out.println("deletes user");
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        deleteUser(dataGen.getRandomExistIdForDel());
                        break;
                    }
                    case INSERT_ARTICLE: {
                        /* insert article */
                        String uid = dataGen.getRandomExistId();
                        long articleDate = dataGen.getRandomDate(uid);
                        String article = dataGen.getRandomArticle();
                        
                        // System.out.println("inserts article");
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        insertArticle(uid, articleDate, article);
                        break;
                    }
                    case GET_ARTICLES: {
                        /* gets article */
                        String uid = dataGen.getRandomExistId();
                        long articleDate = dataGen.getRandomExistDate(uid);
                        if (articleDate < 0)
                            break;
                        int count = dataGen.randBetween(1, 10);
                        
                        // System.out.printf("get %d article\n", count);
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        @SuppressWarnings("unused")
                        Map<Long, String> articles = getArticles(uid, articleDate, count);
                        break;
                    }
                    case GET_ARTICLES_BETWEEN: {
                        /* get articles between date to date*/
                        String uid = dataGen.getRandomExistId();
                        List<Long> fromToDate = dataGen.getRandomExitFromToDate(uid, 1990, 2014);
                        if (fromToDate.isEmpty())
                            break;
                        long fromDate = fromToDate.get(0);
                        long toDate = fromToDate.get(1);
                        
                        /* System.out.printf("get articles between %s to %s\n", 
                                                                new Date(fromDate), new Date(toDate)); */
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        @SuppressWarnings("unused")
                        Map<Long, String> articles = getArticlesBetween(uid, fromDate, toDate);
                        break;
                    }
                    case DELETE_ARTICLE: {
                        /* deletes article */
                        String uid = dataGen.getRandomExistId();
                        long articleDate = dataGen.getRandomExistDateForDel(uid);
                        if (articleDate < 0)
                            break;
                        
                        // System.out.println("deletes article");
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        deleteArticle(uid, articleDate);
                        break;
                    }
                    case DELETE_ARTICLES_BETWEEN: {
                        /* delete articles between date to date*/
                        String uid = dataGen.getRandomExistId();
                        List<Long> fromToDate = dataGen.getRandomExistFromToDateForDel(uid, 1990, 2014);
                        if (fromToDate.isEmpty())
                            break;
                        long fromDate = fromToDate.get(0);
                        long toDate = fromToDate.get(1);
                        
                        /* System.out.printf("delete articles between %s to %s\n", 
                                          new Date(fromDate), new Date(toDate)); */
                        int actionCount = statistics.get(action);
                        statistics.put(action, ++actionCount);
                        deleteArticlesBetween(uid, fromDate, toDate);
                        break;
                    }
                }
            } catch (Exception e) { /* maybe IllegalStateException */
                e.printStackTrace();
                running = false;
            }
        }
    }
    
    public Map<AppAction, Integer> getStatistics() {
        return statistics;
    }
    
    private void insertUser(String uid, String name) {
        /* insert user to Database */
        if (dbCli.insertUser(uid, name) != null) {
            System.err.println("insert " + uid + " user failed at Database");
        }
        
        /* insert user to Arcus */
        if (!arcusCli.insertUser(uid, name)) {
            // System.err.println("insert " + uid + " user failed at Arcus");
        }
    }
    
    private String getUser(String uid) {
        String name = null;
        
        try {
            name = arcusCli.getUser(uid);
            if (name == null) {
                /* cache miss
                 * get user from Database
                 */
                name = dbCli.getUser(uid);
                if (name != null) {
                    /* insert user to Arcus
                     * user article list deleted maybe because of expired time
                     * but don't build article list
                     * 
                     * because user article list expire time more late than user expire time
                     */
                    arcusCli.insertUser(uid, name);
                }
            }
        } catch (TimeoutException e) {
            /* get user from Database */
            name = dbCli.getUser(uid);
        }
        
        return name;
    }
    
    private void deleteUser(String uid) {
        /* delete user from Arcus
         * delete all article of user from Arcus
         */
        if (!arcusCli.deleteUser(uid)) {
            /* because expired user data maybe.... can't delete user */
            // System.err.println("delete user failed at Arcus");
        }
        
        /* delete user from Database 
         * delete all article of user from Database
         */
        if (!dbCli.deleteUser(uid))
            System.err.println("delete " + uid + " user failed at Database");
    }
    
    private void insertArticle(String uid, long articleDate, String article) {
        /* insert article to Database */
        if (dbCli.insertArticle(uid, articleDate, article) != null)
            System.err.println("insert " + uid + " user article failed at Database");
        
        /* insert article to Arcus */
        if (!arcusCli.insertArticle(uid, articleDate, article)) {
            long toDay = Calendar.getInstance().getTimeInMillis();
            Map<Long, String> articles = dbCli.getArticles(uid, toDay, arcusCli.getArticleSizeAttr());
            articles.put(articleDate, article);
            
            arcusCli.buildArticle(uid, articles);
        }
    }
    
    private Map<Long, String> getArticles(String uid, long articleDate, int count) {
        Map<Long, String> articles = null;
        
        try {
            articles = arcusCli.getArticles(uid, articleDate, count);
        } catch (RuntimeException e) {
            /* maybe UNREADABLE */
            articles = dbCli.getArticles(uid, articleDate, count);
        } catch (TimeoutException e) {
            articles = dbCli.getArticles(uid, articleDate, count);
        }
        
        /* cache miss */
        if (articles == null) {
            /* build articles because of NOT_FOUND*/
            long toDay = Calendar.getInstance().getTimeInMillis();
            articles = dbCli.getArticles(uid, toDay, arcusCli.getArticleSizeAttr() + 1);
            arcusCli.buildArticle(uid, articles);
            
            /* articles get from Database */
            articles = dbCli.getArticles(uid, articleDate, count);
        } else if (articles.isEmpty()) {
            /* articles get from Database */
            articles = dbCli.getArticles(uid, articleDate, count);
            
            if (articles.size() > 0) {
                arcusCli.insertArticles(uid, articles);
            }
        }
        return articles;
    }
    
    private Map<Long, String> getArticlesBetween(String uid, long fromDate, long toDate) {
        Map<Long, String> articles = null;
        try {
            articles = arcusCli.getArticlesBetween(uid, fromDate, toDate);
        } catch (RuntimeException e) {
            /* maybe UNREADABLE */
            articles = dbCli.getArticlesBetween(uid, fromDate, toDate);
        } catch (TimeoutException e) {
            articles = dbCli.getArticlesBetween(uid, fromDate, toDate);
        }
        
        /* cache miss */
        if (articles == null) {
            /* build articles because of NOT_FOUND */
            long toDay = Calendar.getInstance().getTimeInMillis();
            articles = dbCli.getArticles(uid, toDay, arcusCli.getArticleSizeAttr() + 1);
            
            arcusCli.buildArticle(uid, articles);
            
            /* get articles of user from Database */
            articles = dbCli.getArticlesBetween(uid, fromDate, toDate);
        } else if (articles.isEmpty()) {
            /* get articles of user from Database */
            articles = dbCli.getArticlesBetween(uid, fromDate, toDate);
            
            if (articles.size() > 0) {
                /* insert to Arcus */
                arcusCli.insertArticles(uid, articles);
            }
        }
        
        return articles;
    }
    
    private void deleteArticle(String uid, long articleDate) {
        /* delete article from Arcus
         * because don't build article list at getUser method when cache miss
         * sometimes occur NOT_FOUND_ELEMENT error
         */
        if (arcusCli.deleteArticle(uid, articleDate) == false) {
            /* NOT_FOUND */
            long toDay = Calendar.getInstance().getTimeInMillis();
            Map<Long, String> articles = dbCli.getArticles(uid, toDay, arcusCli.getArticleSizeAttr() + 1);
            
            arcusCli.buildArticle(uid, articles);
        }
        
        /* delete article from Database */
        if (!dbCli.deleteArticle(uid, articleDate))
            System.err.println("delete " + uid + " user article failed at Database");
    }
    
    private void deleteArticlesBetween(String uid, long fromDate, long toDate) {
        /* delete articles from Arcus */
        if (arcusCli.deleteArticlesBetween(uid, fromDate, toDate) == false) {
            long toDay = Calendar.getInstance().getTimeInMillis();
            Map<Long, String> articles = dbCli.getArticles(uid, toDay, arcusCli.getArticleSizeAttr() + 1);
            
            arcusCli.buildArticle(uid, articles);
        }
        
        /* delete articles from Database */
        dbCli.deleteArticlesBetween(uid, fromDate, toDate);
    }
    
    public void shutdown() {
        this.running = false;
        System.out.println(statistics);
    }
}
