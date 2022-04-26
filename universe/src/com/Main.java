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
        System.out.println("main'd");

        launcher = new Launcher();
        launcher.DEBUG_SERVER_LEVEL = 0;
        launcher.addDatabaseManager();
        launcher.addLoginHandler();
        launcher.addCareTaker(1800000);

        int x = 4;

        while (x==3) {

        }

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
        launcher.loadThread(http);
        launcher.loadThread(tcp);
        launcher.loadThread(ticketManager);
        //CRUDObject.testCRUDObject();

        TaskHandler<Task> H = new TaskHandler();
        H.assignClass(Task.class);
        H.drop();
        H.run();

        //ok fuck it... done for today.
        // CREATE: [x]tables, []objects
        // READ: [x]tables, []objects
        // UPDATE: [*]tables, []objects
        // DELETE: []tables, []objects
        /* what i got done:
         - created query for check table existence
         - created query for create table
         - created query for delete table
         - CRUD for those three as well
         todo:
         - check table structure
         - query: update table structure {separate case for increase/decrease}
         - query: insert into table
         - query: select from table
         - query: update object in table
         - query: delete object in table
         - CRUD: check table structure
         - CRUD: [obj] store, load, save, delete
         - then the bs with type checks yeah yeah... we doing fine.
         */


        //start the handler for tasks

        //Task T = new Task(H,"finish the task system");//H.create(); //no don't use H.create() who the fuck cares

        // new Thread(H).start();
        launcher.loadThread(H);
        launcher.startThreads();
    }



}