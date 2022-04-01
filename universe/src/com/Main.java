package com;

import com.server.*;
import com.server.entity.ServerConnection;
import com.server.protocol.*;
import com.server.web.*;
import com.tickets.*;





public class Main {

    public static Launcher launcher;

    public static void main(String[] args) {
        System.out.println("main'd");
       // Server webserver = new Server(new HTTP("./res/front/",4000));
       // new Thread(webserver).start();
        launcher = new Launcher();
        launcher.addDatabaseManager();
        launcher.addLoginHandler();
        launcher.addCareTaker(18000000);

        TicketProcessing ticketManager = new TicketProcessing();

        HTTP http_protocol = new HTTP("res/front/",6969) {
            public byte[] processGET(ServerConnection c, String uri, String[] fields, String[] values) {
                if (uri.endsWith("/tickets.html") && fields.length>0) {
                    return ticketManager.processGET(this,c,uri,fields,values);
                    //c.setNeedsReply(true);
                    //return WAIT_FOR_RESPONSE;
                    //return multiHTMLResponse_noTags(HTTP_OK,new String[]{fileHTML_noTags(uri),"<h1>FREE DATA!!!</h1>"}).getBytes();
                    //return multiHTMLResponse_noTags(HTTP_OK,new String[]{"<h1> RZ BASED </h1>",fileHTML_noTags(uri)}).getBytes();
                    //return multiHTMLResponse(HTTP_OK,new String[]{"<h1> RZ BASED </h1>",fileHTML(uri)}).getBytes();//fileResponse(HTTP_OK,uri).getBytes();
                }
                return null;
            }
        };
     //   http_protocol.addRoute("/employee","/pages/portal/portal.html");
     //   http_protocol.addRoute("/tickets","/pages/tickets/tickets.html");
        Server http = new Server(http_protocol);

        HTTP http2 = new HTTP("res/front/index/",80);
        Server http_redir = new Server(http2);
        launcher.loadThread(http_redir);


        WebPackets wp = new WebPackets() {
            public void processPOST(ServerConnection c, String uri, int packetID, String[] fields, String[] values) {

                if (packetID >= 500 && packetID <= 600) {
                    ticketManager.processPOST(c,uri,packetID,fields,values);
                }

            }
        };
        Server tcp = new Server(new TCP(43594));
        tcp.setWebPacketHandler(wp);
        launcher.loadThread(http);
        launcher.loadThread(tcp);
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