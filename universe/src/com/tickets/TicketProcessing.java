package com.tickets;

import com.server.entity.*;
import com.server.protocol.*;
import com.db.*;


public class TicketProcessing {

    public TicketProcessing() {

    }


    // CUSTOMER RESPONSE CODES
    private static final String RESPONSE_NO_TICKET = "ticketnotfound";
    // EMPLOYEE RESPONSE CODES
    private static final String RESPONSE_IN_PROGRESS = "inprogress";
    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_FAILURE = "failure";

    private void reply(ServerConnection c, String code) {
        c.sendMessage(HTTP.HTTP_OK+"\r\n"+code);
        c.disconnect();
    }


    public void processPOST(ServerConnection c, String uri, int packetID, String[] fields, String[] values) {

        System.out.println("[TicketProcessing] Processing ticket query...");

        switch (packetID) {
            case 500: //customer looks up their info
                reply(c,RESPONSE_NO_TICKET);
                break;
            case 501: //employee enters a ticket
                System.out.println("[TicketProcessing] Create new ticket");
                for (int i=0; i<fields.length; i++) {
                    System.out.println("fields[i]="+fields[i]+", values[i]="+values[i]);
                }
                reply(c,RESPONSE_IN_PROGRESS);
                break;
        }

    }

}