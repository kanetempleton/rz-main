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

        launcher = new Launcher();
        launcher.DEBUG_SERVER_LEVEL = 0;
        launcher.addDatabaseManager();
        launcher.addLoginHandler();
        launcher.addCareTaker(1800000);

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
        launcher.startThreads();
    }


}