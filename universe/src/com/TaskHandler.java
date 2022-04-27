package com;

import com.db.crud.*;

public class TaskHandler<T> extends CRUDHandler implements Runnable {


    public TaskHandler() {
        super("tasks","taskid");
        setIgnoreMode(false);
       // setIgnoreFields(new String[]{"nonsense","nonsense2"});
        setSaveFields(new String[]{"taskName","taskStatus"});
    }


}