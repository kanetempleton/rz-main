package com;

import com.db.crud.*;
import java.lang.reflect.Field;

public class Task extends CRUDObject {

    private String taskName;
    private String taskStatus;
    private String nonsense;
    private String nonsense2;
    private String nonsense3;
    private String nonsense4;

    public Task(CRUDHandler H, String n, String s) {
        super(H);
        taskName = n;
        taskStatus=s;
        nonsense="nonsense";
        nonsense2="nonsense2";
        nonsense3 = "333";
    }

    public Task(CRUDHandler H, String n) {
        super(H);
        taskName = n;
        taskStatus="incomplete";
        nonsense="nonsense";
        nonsense3 = "333";
    }

    @Override
    public void load() {
        System.out.println("task loading finished!");
    }

    public String getName(){return taskName;}
    public String getStatus(){return taskStatus;}
    public void setName(String n) {taskName=n;}
    public void setStatus(String s){taskStatus=s;}


}