package com;

import com.db.crud.*;
import com.console.*;

public class TaskHandler<T> extends CRUDHandler implements Runnable {


    public TaskHandler() {
        super("tasks","taskid");
        setIgnoreMode(false);
       // setIgnoreFields(new String[]{"nonsense","nonsense2"});
        setSaveFields(new String[]{"taskName","taskStatus"});
    }

    public void start() {
        Console.output("Started Task Handler!");
        testTaskObjects(this);
    }


    /// TEST METHODS ///

    public static void testTaskObjects(TaskHandler H) {
        System.out.println("testing task objects:");

        //create_tasks(H);

      /*  Task T1 = new Task(H,"apples");
        Task T2 = new Task(H,"bananas");
        Task T3 = new Task(H,"cherries");
        T1.setID("10");
        T2.setID("20");
        T3.setID("30");
        T1.store();
        T2.store();
        T3.store(); */

      H.delete("10");
      H.delete("30");



       // load_task(H);

       // Task loadT = H.load_task("20");
        Task loadT = new Task(H,"20") {
            public void load() {
                this.setID(""+(Integer.parseInt(this.getID())*2));
                System.out.println("task id is now "+this.getID());
            }
        };


        // Task loadT = H.load_task("20");
        Task delT = new Task(H,"20") {
            public void load() {
                this.delete();
            }
        };
        delT.setID("20");
        H.delete(delT);
      //  H.update(loadT);

      //  H.update(loadT);


        //Task T1 = H.load_task("20");
        //Task T2 = H.load_task("40");

    }


    //private methods

    private static void create_tasks(TaskHandler H) {
        Task T1 = new Task(H,"specialtask");
        T1.setID("20");
        Task T2 = new Task(H,"fortytask");
        T2.setID("40");
        T1.store();
        H.create(T2);
       // T1.store();
    }

    private static void load_task(TaskHandler H) {
        Task T3 = new Task(H,null) {
            public void load() {
                System.out.println("task name for id="+this.getID()+": "+this.getName());
            }
        };
        T3.setID("20");
        H.read(T3);
    }

    //this is seriously stupid lmao
    private Task load_task(String id) {
        Task T = new Task(this,null) {
            public void load() {
                System.out.println("Loaded task \""+this.getName()+"\" successfully.");
            }
        };
        read(T);
        return T;
    }





}