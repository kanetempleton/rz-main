package com;

import com.db.crud.*;
import java.lang.reflect.Field;

public class Task extends CRUDObject {

    private String taskName;
    private String taskStatus;

    public Task(CRUDHandler H, String n, String s) {
        super(H);
        taskName = n;
        taskStatus=s;
    }

    public Task(CRUDHandler H, String n) {
        super(H);
        taskName = n;
        taskStatus="incomplete";
    }

    public Task(CRUDHandler H) {
        super(H);
        taskName="null";
        taskStatus="null";
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