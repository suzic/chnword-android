package com.chnword.chnword.beans;

/**
 * Created by khtc on 15/5/8.
 */
public class Category {

    private String name;
    private boolean isLock;
    private String cname;
    private int rid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean isLock) {
        this.isLock = isLock;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public void setIsLock(boolean isLock) {
        this.isLock = isLock;
    }

}
