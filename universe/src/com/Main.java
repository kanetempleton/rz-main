package com;

import com.server.*;
import com.server.entity.ServerConnection;
import com.server.protocol.*;
import com.server.web.*;
import com.tickets.*;





public class Main {

    public static Launcher launcher;
    public static HTTP http_protocol;

    public static void main(String[] args) {
        System.out.println("main'd");
       // Server webserver = new Server(new HTTP("./res/front/",4000));
       // new Thread(webserver).start();

        launcher = new Launcher();
        launcher.addDatabaseManager();
        launcher.addLoginHandler();
        launcher.addCareTaker(1800000);

        TicketProcessing ticketManager = new TicketProcessing();
        //launcher.loadThread(ticketManager);

        http_protocol = new HTTP("res/front/",80) {
            public byte[] processGET(ServerConnection c, String uri, String[] fields, String[] values) {
                //System.out.println("checking for custom get: "+uri+" and fields = "+fields);
                if (uri.contains("/tickets") && fields.length>0) {
                    return ticketManager.processGET(this,c,uri,fields,values);
                    //c.setNeedsReply(true);
                    //return WAIT_FOR_RESPONSE;
                    //return multiHTMLResponse_noTags(HTTP_OK,new String[]{fileHTML_noTags(uri),"<h1>FREE DATA!!!</h1>"}).getBytes();
                    //return multiHTMLResponse_noTags(HTTP_OK,new String[]{"<h1> RZ BASED </h1>",fileHTML_noTags(uri)}).getBytes();
                    //return multiHTMLResponse(HTTP_OK,new String[]{"<h1> RZ BASED </h1>",fileHTML(uri)}).getBytes();//fileResponse(HTTP_OK,uri).getBytes();
                }
                return null;
            }
        }; //GET res/front/login.js ; GET res/front/pages/login/login.js
        http_protocol.addRoute("/employee","/pages/portal/portal.html");
        http_protocol.addRoute("/portal.js","/pages/portal/portal.js");
        http_protocol.addRoute("/tickets","/pages/tickets/tickets.html");
        http_protocol.addRoute("/tickets.js","/pages/tickets/tickets.js");
        http_protocol.addRoute("/login","/pages/login/login.html");
        http_protocol.addRoute("/login.js","/pages/login/login.js");
        http_protocol.addRoute("/api","/api/api.html");
        http_protocol.addRoute("/api/tickets","/api/tickets/tickets_api.html");
        http_protocol.addRoute("/style","/style/rzstyle.css");
        http_protocol.addRoute("/dev","/dev/dev.html");
        http_protocol.addRoute("/dev/updates","/dev/updates/updates.html");
        http_protocol.addRoute("/dev/updates/current","/dev/updates/current.html");
        http_protocol.addRoute("/dev/todo","/dev/todo.html");
        http_protocol.addRoute("/dev/bugs","/dev/bugs.html");
        http_protocol.addRoute("/dev/changelog","/dev/changes.html");

        Server http = new Server(http_protocol);

        //HTTP http2 = new HTTP("res/front/index/",80);
        //Server http_redir = new Server(http2);
        //launcher.loadThread(http_redir);


        WebPackets wp = new WebPackets() {
            public void processPOST(ServerConnection c, String uri, int packetID, String[] fields, String[] values) {

                if (packetID >= 500 && packetID <= 600) {
                    ticketManager.processPOST(http_protocol,c,uri,packetID,fields,values);
                }

            }
        };
        Server tcp = new Server(new TCP(43594));
        tcp.setWebPacketHandler(wp);
        launcher.loadThread(http);
        launcher.loadThread(tcp);
        launcher.loadThread(ticketManager);
       // launcher.addHTTPServer(6969);
       // launcher.addTCPServer(43594);
        //launcher.addWebSocketServer(42069);
        /*Server wss = new Server(new WebSocket(42069) {
            public void processCustomMessage(ServerConnection c, String m) {
                System.out.println("BALLSBALLSBALLSBALLS: "+m);
            }
        });
        launcher.loadThread(wss);*/
        launcher.startThreads();
    }


}