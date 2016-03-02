package com.jam2in.arcus.rand;

import java.util.*;

public class RandomDataGen {
    private Random rnd = new Random();
    
    /* maybe don't exist duplicate uid.....
     * because of random uuid api maybe maybe ........*/
    private List<String> uidList = new ArrayList<String>();
    private Map<String, TreeSet<Long>> articleDateList = new HashMap<String, TreeSet<Long>>();
    
    public RandomDataGen(int idListSize) {
        for (int i = 0; i < idListSize; i++) {
            getRandomId();
        }
    }
    
    public String getRandomId() {
        String uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        while (uidList.contains(uid)) {
            uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        }
        uidList.add(uid);
        articleDateList.put(uid, new TreeSet<Long>());
        return uid;
    }
    
    public String getRandomExistId() {
        /* for get user action */
        return uidList.get(randBetween(0, uidList.size() - 1));
    }
    
    public List<String> getRandomExistAllIdList() {
        return uidList;
    }
    
    public String getRandomExistIdForDel() {
        /* for delete user action */
        int idx = randBetween(0, uidList.size() - 1);
        articleDateList.remove(uidList.get(idx));
        return uidList.remove(idx);
    }
    
    public String getRandomArticle() {
        StringBuffer buf = new StringBuffer();
        
        for (int i = 0; i < 100; i++) {
            buf.append((char) ((int) (rnd.nextInt(127))));
        }
        
        return buf.toString();
    }
    
    public long getRandomDate(String uid) {
        int year = randBetween(1990, 2014);
        int month = randBetween(1, 12);
        int date = randBetween(1, 30);
        int hourOfDay = randBetween(0, 23);
        int minute = randBetween(0, 59);
        int second = randBetween(0, 59);
        
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date, hourOfDay, minute, second);
        
        long milliDate = cal.getTimeInMillis();
        if (!articleDateList.get(uid).add(milliDate))
            System.err.println("can't make random date");
        
        return milliDate;
    }
    
    public long getRandomExistDate(String uid) {
        Set<Long> dateList = articleDateList.get(uid);
        long milliDate = -1;
        
        if (!dateList.isEmpty()) {
            int rndIdx = randBetween(0, dateList.size() - 1);
            int i = 0;
            for (long m : dateList) {
                if (i == rndIdx) {
                    milliDate = m;
                    break;
                } else {
                    i++;
                }
            }
        }
        
        return milliDate;
    }
    
    public long getRandomExistDateForDel(String uid) {
        long date = getRandomExistDate(uid);
        
        if (date > 0)
            articleDateList.get(uid).remove(date);
        
        return date;
    }
    
    public List<Long> getRandomExitFromToDate(String uid, int fromYear, int toYear) {
        List<Long> fromTo = new ArrayList<Long>(2);
        TreeSet<Long> dateList = (TreeSet<Long>) articleDateList.get(uid);
        
        if (dateList.isEmpty())
            return fromTo;
        
        /* from Date */
        Calendar fromCal = Calendar.getInstance();
        fromCal.set(fromYear, randBetween(1, 12), randBetween(1, 30),
                    randBetween(0, 23), randBetween(0, 59), randBetween(0, 59));
        long fDate = fromCal.getTimeInMillis();
        
        /* to Date */
        Calendar toCal = Calendar.getInstance();
        toCal.set(toYear, randBetween(1, 12), randBetween(1, 30),
                  randBetween(0, 23), randBetween(0, 59), randBetween(0, 59));
        long tDate = toCal.getTimeInMillis();
        
        /* find fromDate */
        for (long a : dateList) {
            if (fDate <= a && a < tDate) {
                fDate = a;
                fromTo.add(fDate);
                break;
            }
        }
        
        /* find toDate at reverse list */
        List<Long> reverseArticleDateList = new ArrayList<Long>(dateList);
        Collections.reverse(reverseArticleDateList);
        for (long a : reverseArticleDateList) {
            if (fDate < a && a <= tDate) {
                tDate = a;
                fromTo.add(tDate);
                break;
            }
        }
        
        if (fromTo.size() == 1) {
            if (fDate == fromCal.getTimeInMillis())
                fromTo.add(0, fDate);
            else
                fromTo.add(tDate);
        }
        
        return fromTo;
    }
    
    public List<Long> getRandomExistFromToDateForDel(String uid, int fromYear, int toYear) {
        List<Long> fromTo = this.getRandomExitFromToDate(uid, fromYear, toYear);
        
        if (fromTo.isEmpty())
            return fromTo;
        
        List<Long> removeList = new ArrayList<Long>();
        Set<Long> dateList = articleDateList.get(uid);
        
        if (dateList.size() > 0) {
            for (long date : dateList) {
                if (fromTo.get(0) <= date && date <= fromTo.get(1)) {
                    removeList.add(date);
                }
            }
            
            if (!removeList.isEmpty())
                dateList.removeAll(removeList);
        }
        return fromTo;
    }
    
    public String getRandomName() {
        StringBuffer buf = new StringBuffer();
        
        for (int i = 0; i < 20; i++) {
            buf.append((char) ((int) (rnd.nextInt(26)) + 97));
        }
        
        return buf.toString();
    }
    
    public int randBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }
}
