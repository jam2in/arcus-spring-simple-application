package com.jam2in.arcus;

import com.jam2in.arcus.driver.ArcusClientWrapper;
import com.jam2in.arcus.driver.DummyDBInterface;
import com.jam2in.arcus.driver.DummyDBWrapper;
import com.jam2in.arcus.rand.RandomActionGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.BufferedInputStream;
import java.io.File;
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
            File app = new File("/app.properties");
            if (!app.exists()) {
                app = new File("src/main/resources/app.properties");
            }

            prop.load(new BufferedInputStream(new FileInputStream(app.getAbsolutePath())));

            File xml = new File("/simpleAppContext.xml");
            if (!xml.exists()) {
                xml = new File("src/main/resources/simpleAppContext.xml");
            }

            ApplicationContext context = new FileSystemXmlApplicationContext("file:" + xml.getAbsolutePath());
            this.dbCli = (DummyDBInterface) context.getBean("dbWrapper");

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
            e.printStackTrace();
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

    public void startSimpleApp() throws IOException {
        int numOfApp = this.getNumOfApp();
        numOfApp = 1;

        RandomActionGen actionGen = this.getRandomActionGen();

        CountDownLatch latch = new CountDownLatch(numOfApp);
        StatisticsPrinter statPrinter = new StatisticsPrinter(latch);

        SimpleApp[] app = new SimpleApp[numOfApp];
        Properties arcusProps = new Properties();
        File arcus = new File("/arcus.properties");
        if (!arcus.exists()) {
            arcus = new File("src/main/resources/arcus.properties");
        }

        arcusProps.load(new BufferedInputStream(new FileInputStream(arcus.getAbsolutePath())));
        ArcusClientWrapper arcusCli = new ArcusClientWrapper(
            arcusProps.getProperty("arcus.admin"),
            arcusProps.getProperty("arcus.serviceCode"),
            Integer.parseInt(arcusProps.getProperty("arcus.expireSeconds")),
            Long.parseLong(arcusProps.getProperty("arcus.optimeoutMilliseconds")),
            Integer.parseInt(arcusProps.getProperty("arcus.optimeoutRetryCnt")),
            Integer.parseInt(arcusProps.getProperty("arcus.poolSize"))
        );
        arcusCli.connect();

        for (int i = 0; i < numOfApp; i++) {
            app[i] = new SimpleApp(dbCli, arcusCli, actionGen, latch);
            app[i].setName("SimpleApp " + (i + 1) + " thread");
            statPrinter.setAppStats(app[i].getStatistics());
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

    public static void main(String[] args) throws IOException {
        SimpleAppMain main = new SimpleAppMain();
        Runtime.getRuntime().addShutdownHook(main);
        
        main.getAppProperty(args);
        if (!main.initConnections()) {
            System.err.println("Can't start arcus simple application");
        } else {
            System.out.println("Start arcus simple application");
            main.startSimpleApp();
        }
    }
}
