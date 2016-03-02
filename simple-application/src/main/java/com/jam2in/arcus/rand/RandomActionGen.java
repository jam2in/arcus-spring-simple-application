package com.jam2in.arcus.rand;

import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class RandomActionGen {
    private Random rnd = new Random();
    private Set<AppAction> actionSet = 
                           new TreeSet<AppAction>(new Comparator<AppAction>() {
                               public int compare(AppAction o1, AppAction o2) {
                                   if (o1.getRatio() < o2.getRatio())
                                       return -1;
                                   else if (o1.getRatio() == o2.getRatio())
                                       return 0;
                                   else
                                       return 1;
                               }
                           });
    
    public RandomActionGen() {
        actionSet.add(AppAction.INSERT_USER);
        actionSet.add(AppAction.GET_USER);
        actionSet.add(AppAction.DELETE_USER);
        actionSet.add(AppAction.INSERT_ARTICLE);
        actionSet.add(AppAction.GET_ARTICLES);
        actionSet.add(AppAction.GET_ARTICLES_BETWEEN);
        actionSet.add(AppAction.DELETE_ARTICLE);
        actionSet.add(AppAction.DELETE_ARTICLES_BETWEEN);
    }
    
    public boolean setActionRatio(AppAction action, double ratio) {
        if (actionSet.contains(action))
            return false;
        
        action.setRatio(ratio);
        actionSet.add(action);
        
        return true;
    }
    
    public AppAction getRandomAction() {
        double actionCum = 0;
        double prob = rnd.nextDouble() * 100.00;
        
        for (AppAction action : this.actionSet) {
            actionCum += action.getRatio();
            if (prob <= actionCum) {
                return action;
            }
        }
        
        /* cumulative ratio mabye greater than 100%
         * default action
         */
        return AppAction.GET_ARTICLES;
    }
}
