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
        Server http = new Server(new HTTP("res/front/",6969) {
            public void processCustomMessage(ServerConnection c, String m) {
                //System.out.println("BALLSBALLSBALLSBALLS: "+m);
            }
        });
        TicketProcessing ticketManager = new TicketProcessing();
        WebPackets wp = new WebPackets() {
            public void processPOST(ServerConnection c, String uri, int packetID, String[] fields, String[] values) {

                System.out.println("[from the app] Custom POST processing for uri="+uri+", packetID="+packetID);

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