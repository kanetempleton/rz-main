package com.tickets;

import com.server.entity.*;
import com.server.protocol.*;
import com.db.*;


public class TicketProcessing extends DatabaseUtility implements Runnable {

    public TicketProcessing() {

    }

    public void run() {

    }

    public void serverAction(ServerQuery q) {
        switch (q.type()) {
            case QUERYTYPE_NEWTICKET:
                System.out.println("testing testing!! 123456!!!");
                break;
            case QUERYTYPE_GENERATE_TICKET_ID:
                System.out.println("testing testing!! 123!!!");
                break;
        }
    }

    public static final int QUERYTYPE_NEWTICKET = 1000;
    public static final int QUERYTYPE_GENERATE_TICKET_ID = 1001;


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
                reply(c, RESPONSE_NO_TICKET);
                break;
            case 501: //employee enters a ticket
                System.out.println("[TicketProcessing] Create new ticket");
                int[] ticketid = {0};
                generateNewTicketID(ticketid);
                while (ticketid[0] == 0) {

                }
                String title, name, email, phone, info, due, status;
                title = name = email = phone = info = due = status = "";
                for (int i = 0; i < fields.length; i++) {
                    System.out.println("fields[i]=" + fields[i] + ", values[i]=" + values[i]);
                    if (fields[i].equals("customerName"))
                        name = values[i];
                    if (fields[i].equals("customerPhone"))
                        phone = values[i];
                    if (fields[i].equals("customerEmail"))
                        email = values[i];
                    if (fields[i].equals("title"))
                        title = values[i];
                    if (fields[i].equals("info"))
                        info = values[i];
                    if (fields[i].equals("due"))
                        due = values[i];
                }
                Ticket T = new Ticket(ticketid[0], title, name, email, phone, info, due);
                System.out.println("ticket created!");
                storeTicket(T,c,QUERYTYPE_NEWTICKET);
              /*  new ServerQuery(this,c,QUERYTYPE_NEWTICKET,"insert into tickets(ticket_id,title,customerName,customerPhone,customerEmail,info,status,dueDate) VALUES("+ticketid[0]+","+title+","+name+","+) {
                    public void done() {
                        System.out.println("selected all tickets! response = ");
                        for (String r: this.getResponses()) {
                            System.out.println(r);
                        }
                    }
                };*/
                //    public Ticket(int id, String title, String customerName, String customerEmail, String customerPhone, String info, String due) {

                //reply(c, RESPONSE_IN_PROGRESS);
                break;
        }

    }

    private void generateNewTicketID(int[] buf) {
        int x = 1+(int)(Math.random()*100000000);
        boolean found = false;
        boolean found2 = false;
        new ServerQuery(this,"select ticket_id from tickets where ticket_id='"+x+"'") {
            public void done() {
                if (this.getResponses().size() > 0) {
                    generateNewTicketID(buf);
                    System.out.println("retrying new ticket id...");
                } else {
                    buf[0] = x;
                    System.out.println("ticket id found: "+x);
                }
            }
        };
        System.out.println("generating new customer ticket id...");
    }

    public void storeTicket(Ticket T, ServerConnection c, int type) {
        new ServerQuery(this,c,type,"INSERT INTO tickets(ticket_id,title,customerName,customerPhone,customerEmail,info,status,dueDate) VALUES('"+T.getId()+"','"+T.getTitle()+"','"+T.getCustName()+"','"+T.getCustPhone()+"','"+T.getCustEmail()+"','"+T.getInfo()+"','"+T.getStatus()+"','"+T.getDue()+"')") {
            public void done() {
                System.out.println("inserted ticket "+T.getId()+"!");
                reply(c,RESPONSE_SUCCESS+":"+T.getId());
               // c.sendMessage(HTTP.HTTP_OK+"\r\n"+RESPONSE_SUCCESS);
               // c.disconnect();
            }
        };
    }

}