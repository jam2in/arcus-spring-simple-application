package com.jam2in.arcus;

import com.jam2in.arcus.driver.ArcusClientWrapper;
import com.jam2in.arcus.driver.DummyDBInterface;
import com.jam2in.arcus.driver.DummyDBWrapper;
import com.jam2in.arcus.rand.RandomActionGen;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class SimpleAppMain extends Thread {
    /* Application Property */
    private Properties prop = new Properties();
    private int USER_ARTICLE_SIZE = 1000;
    private int NUM_OF_APP = 10;

    List<SimpleApp> appList = new ArrayList<SimpleApp>();

    @Autowired
    private DummyDBInterface dbCli;
    private RandomActionGen actionGen;
    
    /* Application shutdown thread method */
    public void run() {
        /* stop all application thread */ 
        for (SimpleApp app : appList) {
            //app.shutdown();
        }
        
        /* disconnect arcus and database */
        this.finalConnections();
        System.out.println("Stop arcus simple application");
    }
    
    public void getAppProperty(String[] args) {
        /* get Application property */
        try {
            prop.load(new BufferedInputStream(new FileInputStream("/app.properties")));
            
            /* don't consider wrong property
             * ex : ArcusAdmin=
             * ex : don't exist
             */
            if (args.length > 0) {
                NUM_OF_APP = Integer.parseInt(args[0]);
            } else {
                NUM_OF_APP = Integer.parseInt(prop.getProperty("NumberOfApplication"));
            }

            USER_ARTICLE_SIZE = Integer.parseInt(prop.getProperty("UserArticleSize"));
        } catch (FileNotFoundException e) {
            // FIXME : maybe java 7 or higher version... use multiple exception catch
            System.err.println("Application properties file don't exist.");
        } catch (IOException e) {
            System.err.println("Application properties file can't read.");
        } catch (NumberFormatException e) {
            System.err.println("Can't find some application property.");
        }
    }
    
    public boolean initConnections() {
        try {
            /* Connect Database */
            this.dbCli.connect();
            this.actionGen = new RandomActionGen();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    public void finalConnections() {
        if (this.dbCli != null) {
            this.dbCli.disconnect();
            this.dbCli = null;
        }
    }
    
    public DummyDBInterface getDatabaseClient() {
        return this.dbCli;
    }
    
    public RandomActionGen getRandomActionGen() {
        return this.actionGen;
    }
    
    public int getNumOfApp() {
        return this.NUM_OF_APP;
    }

    public static void main(String[] args) {
        SimpleAppMain main = new SimpleAppMain();
        Runtime.getRuntime().addShutdownHook(main);
        
        main.getAppProperty(args);
        if (!main.initConnections()) {
            System.err.println("Can't start arcus simple application");
        } else {
            System.out.println("Start arcus simple application");
            
            int numOfApp = main.getNumOfApp();
            
            RandomActionGen actionGen = main.getRandomActionGen();
            
            CountDownLatch latch = new CountDownLatch(numOfApp);
            StatisticsPrinter statPrinter = new StatisticsPrinter(latch);
            
            SimpleApp[] app = new SimpleApp[numOfApp];
            for (int i = 0; i < numOfApp; i++) {
                //app[i] = new SimpleApp(dbCli, arcusCli, actionGen, latch);
                app[i].setName("SimpleApp " + (i + 1) + " thread");
                //statPrinter.setAppStats(app[i].getStatistics());
            }
            
            statPrinter.start();
            for (int i = 0; i < numOfApp; i++) {
                app[i].start();
            }
            
            for (int i = 0; i < numOfApp; i++) {
                try {
                    app[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
