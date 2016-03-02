package com.jam2in.arcus;

import com.jam2in.arcus.rand.AppAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class StatisticsPrinter extends Thread {
    private List<Map<AppAction, Integer>> eachStat= new ArrayList<Map<AppAction, Integer>>();
    private Map<AppAction, Integer> allStat = new LinkedHashMap<AppAction, Integer>();
    
    private long startTime;
    private long currTime;
    
    private CountDownLatch latch;
    
    public StatisticsPrinter(CountDownLatch latch) {
        super();
        this.latch = latch;
        setName("Statistics printer thread");
        
        for (AppAction action : AppAction.values())
            allStat.put(action, 0);
    }
    
    public void setAppStats(Map<AppAction, Integer> stat) {
        this.eachStat.add(stat);
    }
    
    public void run() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis();
        System.out.println();
        
        StringBuilder abbrHelp = new StringBuilder();
        for (AppAction action : AppAction.values()) {
            abbrHelp.append(action.getAbbrName() + " : " + action.name() + "   ");
        }
        
        System.out.printf("%d thread statistics\n", eachStat.size());
        int elapsCount = 0;
        while (!eachStat.isEmpty()) {
            try {
                Thread.sleep(1000);
                currTime = System.currentTimeMillis();
                System.out.println(abbrHelp);
                System.out.printf("%d sec statistics. req/sec (cumulative req)\n", ++elapsCount);
                
                int appCount = 0;
                /* print stat by app thread */
                for (Map<AppAction, Integer> stat : eachStat) {
                    appCount++;
                    if (appCount == 1)
                        System.out.printf("%5dst thread - ", appCount);
                    else if (appCount == 2)
                        System.out.printf("%5dnd thread - ", appCount);
                    else
                        System.out.printf("%5dth thread - ", appCount);
                    
                    for (Map.Entry<AppAction, Integer> e : stat.entrySet()) {
                        AppAction act = e.getKey();
                        int actStat = e.getValue();
                        System.out.printf("%s : %7.1f(%9d)  ", 
                                           act.getAbbrName(), calStat(actStat), actStat);
                        int actAllStat = allStat.get(act);
                        actAllStat += actStat;
                        allStat.put(act, actAllStat);
                    }
                    System.out.println();
                }
                
                /* print total stat */
                System.out.print("  Total thread - ");
                int totalReq = 0;
                for (Map.Entry<AppAction, Integer> e : allStat.entrySet()) {
                    AppAction act = e.getKey();
                    int stat = e.getValue();
                    System.out.printf("%s : %7.1f(%9d)  ", act.getAbbrName(), calStat(stat), stat);
                    
                    totalReq += stat;
                }
                System.out.println();
                printPercPerAct(totalReq);
                System.out.printf("Total %d request, %d sec. %7.1f req/sec\n", 
                                   totalReq, elapsCount, (double)totalReq/(double)elapsCount);
                System.out.println();
                
                /* init all stat */
                for (Map.Entry<AppAction, Integer> e : allStat.entrySet()) {
                    AppAction act = e.getKey();
                    allStat.put(act, 0);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private double calStat(int stat) {
        return ((double)stat / (double)((currTime - startTime) / 1000));
    }
    
    private void printPercPerAct(int totalReq) {
        System.out.print("   App Percent - ");
        
        for (AppAction a : AppAction.values()) {
            int stat = allStat.get(a);
            System.out.printf("%s : %6.2f%%             ", a.getAbbrName(),
                                             (double)stat / (double)totalReq * 100);
        }
        System.out.println();
    }
}
