package com.jam2in.arcus.rand;

public enum AppAction {
    /* default action ratio
     * 1. DELETE_ARTICLES_BETWEEN : 00.01% 
     * 2. DELETE_USER             : 00.02%
     * 3. DELETE_ARTICLE          : 00.07%
     * 4. INSERT_USER             : 01.00%
     * 5. GET_USER                : 05.00%
     * 6. GET_ARTICLES_BETWEEN    : 20.30%
     * 7. INSERT_ARTICLE          : 30.00%
     * 8. GET_ARTICLES            : 43.60%
     */
    INSERT_USER(1.00),
    GET_USER(5.00),
    DELETE_USER(0.02),
    INSERT_ARTICLE(30.00),
    GET_ARTICLES(43.60),
    GET_ARTICLES_BETWEEN(20.30),
    DELETE_ARTICLE(0.07),
    DELETE_ARTICLES_BETWEEN(0.01),
    ;
    
    private double ratio;
    
    AppAction(double r) {
        this.ratio = r;
    }
    
    public void setRatio(double r) {
       this.ratio = r;
    }
    
    public double getRatio() {
        return this.ratio;
    }
    
    public StringBuffer getAbbrName() {
        StringBuffer ret = new StringBuffer();
        String fullName = name();
        String[] tmp = fullName.split("_");
        
        for (String s : tmp) {
            ret.append(s.charAt(0) + "_");
        }
        ret.deleteCharAt(ret.length() - 1);
        
        return ret;
    }
}
