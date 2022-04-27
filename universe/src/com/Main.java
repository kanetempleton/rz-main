package com;

import com.server.*;
import com.server.entity.ServerConnection;
import com.server.protocol.*;
import com.server.web.*;
import com.tickets.*;

import com.db.crud.*;
import java.lang.reflect.Field;





public class Main {

    public static Launcher launcher;
    public static HTTP http_protocol;

    public static void main(String[] args) {
      //  System.out.println("main'd");

        launcher = new Launcher();
        launcher.DEBUG_SERVER_LEVEL = 0;
        launcher.addDatabaseManager();
        launcher.addLoginHandler();
        launcher.addCareTaker(1800000);

        CRUDHandler.DEBUG_CRUD = true;

        TicketProcessing ticketManager = new TicketProcessing();

        http_protocol = new HTTP("res/front/",80) {
            public byte[] processGET(ServerConnection c, String uri, String resource, String[] fields, String[] values) {
                if (uri.contains("/tickets") && fields.length>0) {
                    return ticketManager.processGET(this,c,uri,resource,fields,values);
                }
                return null;
            }
        };
        Server http = new Server(http_protocol,2048);

        WebPackets wp = new WebPackets() {
            public void processPOST(ServerConnection c, String uri, int packetID, String[] fields, String[] values) {
                if (packetID >= 500 && packetID <= 600) {
                    ticketManager.processPOST(http_protocol,c,uri,packetID,fields,values);
                }
            }
        };
        Server tcp = new Server(new TCP(43594),1024);
        tcp.setWebPacketHandler(wp);

        TaskHandler<Task> H = new TaskHandler();
        H.assignClass(Task.class);

        launcher.loadThread(http,"Web Server");
        launcher.loadThread(tcp,"TCP Server");

        //TODO: figure out why this breaks everything
        //whichever of these two threads that loads first becomes the only thread now
        // or something bro wtf idk i wanna go home
        // WAIT NEVERMIND HOLY SHIT I GOT IT
        //I TOTALLY FORGOT ABOUT THIS LITTLE GUY
        //EXTREMELYYYY IMPORTANT!!!!!
        // Main.launcher.nextStage();
        launcher.loadThread(H,"TaskManager"); //FUCK YEAH!!!!!!!!!!!!!
        launcher.loadThread(ticketManager,"Ticketing System");

       // H.create(T);

        //ok fuck it... done for today.
        // CREATE: [x]tables, []objects
        // READ: [x]tables, []objects
        // UPDATE: [*]tables, []objects
        // DELETE: [x]tables, []objects
        /* what i got done:
         - created query for check table existence
         - created query for create table
         - created query for delete table
         - CRUD for those three as well
         ~~ next day ~~
         - generate table structure strings for both database and class
         - check if table structures match
         - auto add/drop of columns to sync database with class
         - identify name/type mismatch in structures (still needs to be handled)
         - query: update table (add/drop columns)
         - query: get table info (column names and data types)
         - fixed some bugs
         todo:
         - query: insert into table
         - query: select from table
         - query: update object in table
         - query: delete object in table
         - CRUD: [obj] store, load, save, delete
         - then the bs with type checks yeah yeah... we doing fine.
         - CRUD: [t] handle name/type mismatches in schema
         */

        //start the handler for tasks
        //Task T = new Task(H,"finish the task system");//H.create(); //no don't use H.create() who the fuck cares

        // new Thread(H).start();
        //launcher.loadThread(H,"TaskManager");
        launcher.startThreads();
    }



}