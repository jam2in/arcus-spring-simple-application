package com.jam2in.arcus.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.AdminConnectTimeoutException;
import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.collection.CollectionAttributes;
import net.spy.memcached.collection.CollectionResponse;
import net.spy.memcached.collection.Element;
import net.spy.memcached.collection.ElementValueType;
import net.spy.memcached.internal.CollectionFuture;
import net.spy.memcached.ops.CollectionOperationStatus;

public class ArcusClientWrapper {
    private ArcusClient arcus;
    
    private String ARCUS_ADMIN;
    private String ARCUS_SERVICE_CODE;
    private int ARCUS_EXP_TIME;
    private long ARCUS_OPER_TIMEOUT;
    private int ARCUS_OPTIMEOUT_RETRY_CNT;
    
    private String ARCUS_PREFIX_USER = "ARCUS_TEST_USER:";
    private String ARCUS_PREFIX_USER_ARTICLE = "ARCUS_TEST_USER_ARTICLE:";
    
    private CollectionAttributes articleAttr;
    private ElementValueType articleVType;
    private int USER_ARTICLE_SIZE;
    
    public ArcusClientWrapper(String ARCUS_ADMIN,
                              String ARCUS_SERVICE_CODE,
                              int ARCUS_EXP_TIME,
                              long ARCUS_OPER_TIMEOUT,
                              int ARCUS_OPTIMEOUT_RETRY_CNT,
                              int USER_ARTICLE_SIZE) {
        this.ARCUS_ADMIN = ARCUS_ADMIN;
        this.ARCUS_SERVICE_CODE = ARCUS_SERVICE_CODE;
        this.ARCUS_EXP_TIME = ARCUS_EXP_TIME;
        this.ARCUS_OPER_TIMEOUT = ARCUS_OPER_TIMEOUT;
        this.ARCUS_OPTIMEOUT_RETRY_CNT = ARCUS_OPTIMEOUT_RETRY_CNT;
        this.USER_ARTICLE_SIZE = USER_ARTICLE_SIZE;
    }
    
    public void connect() {
        /* setting log4j for Arcus Java Client
         * use jvm runtime option
         * -Dnet.spy.log.LoggerImpl=net.spy.memcached.compat.log.Log4JLogger
         * or
         * System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
         */
        try {
            /* Connect Arcus Server */
            this.arcus = ArcusClient.createArcusClient(ARCUS_ADMIN,
                                                       ARCUS_SERVICE_CODE,
                                                       new ConnectionFactoryBuilder());
            System.out.println("Connect arcus");
        } catch (AdminConnectTimeoutException e) {
            System.err.println("Can't connect Arcus.");
            throw e;
        } catch (NullPointerException e) {
            System.err.println("Can't connect Arcus");
            throw e;
        } catch (IllegalArgumentException e) {
            System.err.println("Can't connect Arcus");
            throw e;
        }
        
        this.setArticleAttr();
    }
    
    public void disconnect() {
        if (this.arcus != null) {
            this.arcus.shutdown();
        }
        System.out.println("Disconnect arcus");
    }
    
    private void setArticleAttr() {
        this.articleAttr = new CollectionAttributes();
        this.articleAttr.setExpireTime(ARCUS_EXP_TIME);
        this.articleAttr.setMaxCount(USER_ARTICLE_SIZE);
        this.articleVType = ElementValueType.STRING;
    }
    
    public int getArticleSizeAttr() {
        return this.USER_ARTICLE_SIZE;
    }
    
    public boolean insertUser(String uid, String name) {
        Future<Boolean> future = null;
        try {
            future = arcus.add(ARCUS_PREFIX_USER.concat(uid), ARCUS_EXP_TIME, name);
        } catch (IllegalStateException e) {
            throw e;
        }
        
        try {
            if (!future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                System.err.printf("\tFail insert user! - %s:%s\n", uid, name);
                
                return false;
            }
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
            
            return false;
        }
        
        /* for b+tree create */
        CollectionFuture<Boolean> collectionFuture = null;
        
        /* create b+tree for article of user */
        try {
            collectionFuture = arcus.asyncBopCreate(ARCUS_PREFIX_USER_ARTICLE.concat(uid),
                                             this.articleVType,
                                             this.articleAttr);
        } catch (IllegalStateException e) {
            throw e;
        }
        
        try {
            if (!collectionFuture.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                System.err.printf("\tFail create b+tree for user article! - %s : %s\n",
                                  uid, collectionFuture.getOperationStatus().getResponse());
            }
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            collectionFuture.cancel(true);
            
            return false;
        }
        
        return true;
    }
    
    public String getUser(String uid) throws TimeoutException {
        Future<Object> future = null;
        String name = null;
       
        try {
            future = arcus.asyncGet(ARCUS_PREFIX_USER.concat(uid));
        } catch (IllegalStateException e) {
            throw e;
        }
        
        try {
            name = (String)future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
        } catch (ExecutionException e) {
            e.printStackTrace();
            future.cancel(true);
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw e;
        }
        
        return name;
    }
    
    public boolean deleteUser(String uid) {
        Future<Boolean> future = null;
        try {
            future = arcus.delete(ARCUS_PREFIX_USER.concat(uid));
        } catch (IllegalStateException e) {
            throw e;
        }
        
        /* delete user */
        int retry = 0;
        while (retry < this.ARCUS_OPTIMEOUT_RETRY_CNT) {
            try {
                if (!future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    return false;
                }
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                future.cancel(true);
                break;
            } catch (ExecutionException e) {
                e.printStackTrace();
                future.cancel(true);
                break;
            } catch (TimeoutException e) {
                e.printStackTrace();
                System.err.printf("\tRetry(%d/%d) %s user deletion\n", 
                        ++retry, this.ARCUS_OPTIMEOUT_RETRY_CNT, uid);
            }
        }
        if (retry == this.ARCUS_OPTIMEOUT_RETRY_CNT) {
            System.err.printf("\t%s user delete failed!\n", uid);
            
            return false;
        }
        
        /* delete all articles(b+tree) of user */
        retry = 0;
        while (retry < this.ARCUS_OPTIMEOUT_RETRY_CNT) {
            try {
                future = arcus.delete(ARCUS_PREFIX_USER_ARTICLE.concat(uid));
            } catch (IllegalStateException e) {
                throw e;
            }
            
            try {
                if (!future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    System.err.printf("\tFail delete articles of %s\n", uid);
                    
                    return false;
                }
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                future.cancel(true);
                break;
            } catch (ExecutionException e) {
                e.printStackTrace();
                future.cancel(true);
                break;
            } catch (TimeoutException e) {
                e.printStackTrace();
                System.err.printf("\tRetry(%d/%d) b+tree all deletion of %s\n", 
                        ++retry, this.ARCUS_OPTIMEOUT_RETRY_CNT, uid);
            }
        }
        if (retry == this.ARCUS_OPTIMEOUT_RETRY_CNT) {
            System.err.printf("\t%s b+tree all deletion is failed!\n", uid);
            
            return false;
        }
        
        return true;
    }
    
    public boolean insertArticle(String uid, long articleDate, String article) {
        CollectionFuture<Boolean> future = null;
        byte[] eflag = new byte[]{ 0 }; /* we don't use eflag. */
        
        try {
            future = arcus.asyncBopInsert(ARCUS_PREFIX_USER_ARTICLE.concat(uid), articleDate,
                                            eflag, article, null);
        } catch (IllegalStateException e) {
            throw e;
        }
        
        try {
            if (!future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                CollectionResponse response = future.getOperationStatus().getResponse();
                if (response == CollectionResponse.NOT_FOUND)
                    return false;
                else 
                    System.err.printf("\tArcus b+tree insert failed! - %s, %s : %s\n",
                                                                    uid, new Date(articleDate),
                                                                    response);
            }
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
        }
        
        return true;
    }
    
    public void insertArticles(String uid, Map<Long, String> articles) {
        CollectionFuture<Map<Integer, CollectionOperationStatus>> future = null;
        byte[] eflag = new byte[]{ 0 }; /* we don't use eflag. */
        /* if EFLAG parameter of Element constructor is null
         * then this arcus-java-client version(1.8.2) occur null pointer exception error
         */
        List<Element<Object>> elements = new ArrayList<Element<Object>>();
        for (Map.Entry<Long, String> entry : articles.entrySet()) {
            elements.add(new Element<Object>(entry.getKey(), 
                                             entry.getValue(),
                                             eflag));
        }
        
        try {
            future = arcus.asyncBopPipedInsertBulk(ARCUS_PREFIX_USER_ARTICLE.concat(uid),
                                                   elements, null);
        } catch (IllegalStateException e) {
            throw e;
        }
        
        Map<Integer, CollectionOperationStatus> result;
        try {
            result = future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!result.isEmpty()) {
                System.err.println("\t\t" + uid + " user insert articles failed!");
                for (Map.Entry<Integer, CollectionOperationStatus> e : result.entrySet()) {
                    System.err.printf("\t\tFailed item = \"%s\", ", elements.get(e.getKey()).getLongBkey());
                    System.err.printf("Failed reason = \"%s\"\n", e.getValue().getResponse());
                }
            }
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
        }
    }
    
    public void buildArticle(String uid, Map<Long, String> articles) {
        /* create b+tree for article of user */
        CollectionFuture<Boolean> collectionFuture = null;
        
        /* article b+tree set unreadable */
        CollectionAttributes readableAttrs = new CollectionAttributes();
        readableAttrs.setReadable(false);
        try {
            collectionFuture = arcus.asyncBopCreate(ARCUS_PREFIX_USER_ARTICLE.concat(uid),
                                                    this.articleVType,
                                                    readableAttrs);
        } catch (IllegalStateException e) {
            throw e;
        }
        
        try {
            if (!collectionFuture.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                System.err.printf("\tArcus b+tree create failed! - %s : %s\n",
                                  uid, collectionFuture.getOperationStatus().getResponse());
            }
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            collectionFuture.cancel(true);
        }
        
        /* build article at b+tree */
        if (articles.size() > 0)
            this.insertArticles(uid, articles);
        
        /* article b+tree set readable */
        readableAttrs.setReadable(true);
        collectionFuture = arcus.asyncSetAttr(ARCUS_PREFIX_USER_ARTICLE.concat(uid),
                                              readableAttrs);
        try {
            if (!collectionFuture.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS))
                System.err.printf("\tArcus b+tree readable attribute failed - %s : %s\n",
                                  uid, collectionFuture.getOperationStatus().getResponse());
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            collectionFuture.cancel(true);
        }
    }
    
    public Map<Long, String> getArticles(String uid, long date, int count) 
                                                                     throws IllegalStateException,
                                                                            RuntimeException,
                                                                            TimeoutException {
        Map<Long, String> articles = new HashMap<Long, String>();
        
        CollectionFuture<Map<Long, Element<Object>>> future = null;
        
        try {
            future = arcus.asyncBopGet(ARCUS_PREFIX_USER_ARTICLE.concat(uid),
                                       date, 0,  /* descending */
                                       null, 0, count, false, false);
        } catch (IllegalStateException e) {
        	throw e;
        }
        
        try {
            Map<Long, Element<Object>> result = 
                                       future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS);
            if (result != null) {
                for (Map.Entry<Long, Element<Object>> entry : result.entrySet()) {
                    articles.put(entry.getKey(), (String)entry.getValue().getValue());
                }
            } else {
                /* cache miss */
                CollectionResponse response = future.getOperationStatus().getResponse();
                if (response == CollectionResponse.NOT_FOUND_ELEMENT) {
                    // System.err.println("NOT_FOUND_ELEMENT error when get articles");
                } else if (response == CollectionResponse.NOT_FOUND) {
                    articles = null;
                } else if (response == CollectionResponse.UNREADABLE) {
                    throw new RuntimeException();
                } else {
                    System.err.printf("\tArcus b+tree get failed! - %s : %s\n", uid, response);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
        }
        return articles;
    }
    
    public Map<Long, String> getArticlesBetween(String uid, long fromDate, long toDate) 
                                                                      throws IllegalStateException,
                                                                             RuntimeException,
                                                                             TimeoutException {
        Map<Long, String> articles = new HashMap<Long, String>();
        
        CollectionFuture<Map<Long, Element<Object>>> future = null;
        try {
            future = arcus.asyncBopGet(ARCUS_PREFIX_USER_ARTICLE.concat(uid),
                                       fromDate, toDate,
                                       null, 0, 0 /* all */, false, false);
        } catch (IllegalStateException e) {
            throw e;
        }
        
        try {
            Map<Long, Element<Object>> result = 
                                       future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS);
            if (result != null) {
                for (Map.Entry<Long, Element<Object>> entry : result.entrySet()) {
                    articles.put(entry.getKey(), (String)entry.getValue().getValue());
                }
            } else {
                /* cache miss */
                CollectionResponse response = future.getOperationStatus().getResponse();
                if (response == CollectionResponse.NOT_FOUND_ELEMENT) {
                    // System.err.println("NOT_FOUND_ELEMENT error when get articles between");
                } else if (response == CollectionResponse.NOT_FOUND) {
                    articles = null;
                } else if (response == CollectionResponse.UNREADABLE) {
                    throw new RuntimeException();
                } else {
                    System.err.printf("\tArcus b+tree get failed! - %s : %s\n", uid, response);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
        }
        
        return articles;
    }
    
    public boolean deleteArticle(String uid, long date) {
        CollectionFuture<Boolean> future = null;
        
        try {
            future = arcus.asyncBopDelete(ARCUS_PREFIX_USER_ARTICLE.concat(uid), date, null, false);
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
        }
        
        try {
            if (!future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                CollectionResponse response = future.getOperationStatus().getResponse();
                if (response == CollectionResponse.NOT_FOUND_ELEMENT) {
                    // System.err.println(uid + " user NOT_FOUND_ELEMENT error when delete " + date + " article");
                } else if (response == CollectionResponse.NOT_FOUND) {
                    return false;
                } else {
                    System.err.printf("\tArcus b+tree delete failed! - %s, %s : %s\n",
                                                        uid, new Date(date), response);
                }
            }
        } catch (Exception e) {
            /* Interrupted, Execution, Timeout Exception */
            e.printStackTrace();
            future.cancel(true);
        }
        
        return true;
    }
    
    public boolean deleteArticlesBetween(String uid, long fromDate, long toDate) {
        int retry = 0;
        CollectionFuture<Boolean> future = null;
        while (retry < this.ARCUS_OPTIMEOUT_RETRY_CNT) {
            try {
                future = arcus.asyncBopDelete(ARCUS_PREFIX_USER_ARTICLE.concat(uid),
                                              fromDate, toDate, null, 0, false);
            } catch (IllegalStateException e) {
                throw e;
            }
            
            try {
                if (!future.get(ARCUS_OPER_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    CollectionResponse response = future.getOperationStatus().getResponse();
                    if (response == CollectionResponse.NOT_FOUND_ELEMENT) {
                        // System.err.println(uid + " user NOT_FOUND_ELEMENT error when delete articles between");
                    } else if (response == CollectionResponse.NOT_FOUND) {
                        return false;
                    } else {
                        System.err.printf("\tArcus b+tree delete failed! - %s : %s ~ %s : %s\n", 
                                            uid, new Date(fromDate), new Date(toDate), response);
                    }
                }
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                future.cancel(true);
                break;
            } catch (ExecutionException e) {
                e.printStackTrace();
                future.cancel(true);
                break;
            } catch (TimeoutException e) {
                e.printStackTrace();
                System.err.printf("\tRetry(%d/%d) b+tree deletion of %s (%s ~ %s)\n", 
                                   ++retry, this.ARCUS_OPTIMEOUT_RETRY_CNT,
                                   uid, new Date(fromDate), new Date(toDate));
            }
        }
        if (retry == this.ARCUS_OPTIMEOUT_RETRY_CNT)
            System.err.printf("\t%s b+tree deletion is failed!\n", uid);
        
        return true;
    }
}
