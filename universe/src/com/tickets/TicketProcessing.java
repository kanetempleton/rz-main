package com.tickets;

import com.server.entity.*;
import com.server.protocol.*;
import com.db.*;
import com.util.html.*;


public class TicketProcessing extends DatabaseUtility implements Runnable {

    public TicketProcessing() {
        super("tickets");
    }

    public void run() {
        initTable();
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
    public static final int QUERYTYPE_SEARCH_TICKETS = 1002;
    public static final int QUERYTYPE_VIEW_TICKET = 1003;
    public static final int QUERYTYPE_DELETE_TICKET = 1004;

    public static final int PACKETTYPE_CUSTOMER_VIEW_TICKET = 500;
    public static final int PACKETTYPE_EMPLOYEE_NEW_TICKET = 501;
    public static final int PACKETTYPE_EMPLOYEE_VIEW_ALL_TICKETS = 502;


    // CUSTOMER RESPONSE CODES
    private static final String RESPONSE_NO_TICKET = "ticketnotfound";
    // EMPLOYEE RESPONSE CODES
    private static final String RESPONSE_IN_PROGRESS = "inprogress";
    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_FAILURE = "failure";
    private static final String RESPONSE_SHOW_TICKETS = "alltickets";
    private static final String RESPONSE_SHOW_TICKET = "ticketfound";

    private void reply(ServerConnection c, String code) {
        //System.out.println("sending reply: ["+code+"]");
        c.sendMessage(HTTP.HTTP_OK+"\r\n"+code);
        c.disconnect();
    }


    public void processPOST(HTTP http, ServerConnection c, String uri, int packetID, String[] fields, String[] values) {

        System.out.println("[TicketProcessing] Processing ticket query...");

        switch (packetID) {
            case PACKETTYPE_CUSTOMER_VIEW_TICKET: //customer looks up their info
               // reply(c, RESPONSE_NO_TICKET);
                searchCustomerTicket(http,c,uri,fields,values);
                break;
            case PACKETTYPE_EMPLOYEE_NEW_TICKET: //employee enters a ticket
                enterNewTicket(c,fields,values);
                break;
            case PACKETTYPE_EMPLOYEE_VIEW_ALL_TICKETS: //view all tickets
                queryAllTicketsToTable(c);
                break;
        }

    }

    public byte[] processGET(HTTP http, ServerConnection c, String uri, String[] fields, String[] values) {
        boolean properFormat = false;
        String function = "get";
        String ticketid = "";
        for (int i=0; i<fields.length; i++) {
            if (fields[i].equals("id")) {
                properFormat=true;
                ticketid = values[i];
                function = "id";
            }
            if (fields[i].equals("delete")) {
                properFormat=true;
                ticketid = values[i];
                function = "delete";
            }
            if (fields[i].equals("modify")) {
                properFormat=true;
                ticketid = values[i];
                function = "modify";
            }
        }
        if (!properFormat)
            return null;

        c.setNeedsReply(true);
        if (function.equals("id"))
            queryTicketByID(http,c,uri,ticketid);
        if (function.equals("delete"))
            deleteTicket(http,c,uri,ticketid);
        if (function.equals("modify"))
            editTicket(http,c,uri,ticketid);
       /* String buildHTML = "";
        public void done() {
            HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
            T.addBasicBorders();
            T.addHrefToColumn("ticket_id","tickets.html");
            buildHTML = T.toString();//HTMLGenerator.generateTable(this.response_getFields(),this.response_getValues());
            reply(c,RESPONSE_SHOW_TICKETS+";;;"+buildHTML);
        }*/
       /* new ServerQuery(this,c,QUERYTYPE_VIEW_TICKET, "select * from tickets where id='"+ticketid+"'") {
            public void done() {
                if (this.responseSize()!=1) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                T.appendColumnToEnd("modify","<a href=/tickets?update>edit ticket</a>");
                T.appendColumnToEnd("delete","<a href=/tickets?delete>remove ticket</a>");
                //T.appendStyle(" class='center'");
                //reply();
                String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),T.toString()});
               // reply(c,r);
                c.sendMessage(r);
                c.disconnect();
            }
        };*/
        return http.WAIT_FOR_RESPONSE.getBytes();
    }

    private void editTicket(HTTP http, ServerConnection c, String uri, String ticketid) {
        new ServerQuery(this,c,QUERYTYPE_VIEW_TICKET, "select * from tickets where id='"+ticketid+"'") {
            public void done() {
                if (this.responseSize()!=1) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                T.addFormToRow("id",ticketid);
               // T.addHrefToColumn("id","tickets");

                if (c.getCookie("usr").equals("rzadmin")) {
                    //  T.appendColumnToEnd("modify", "edit ticket");
                    T.appendColumnToEnd("modify", "<button id=\"submitChangesButton\" onclick=\"tryEditQuery()\">Submit Changes</button>");
                   // T.addHrefToColumn("delete","tickets","id");
                }


                //T.appendStyle(" class='center'");
                //reply();
                 String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),T.toString()});
                 //reply(c,r);
                 c.sendMessage(r);
                 c.disconnect();
                //System.out.println("sending ticket response...");
                //reply(c,RESPONSE_SHOW_TICKET+";;;"+T.toString());

            }
        };
        // <input type="text" value="default value">
    }

    private void searchCustomerTicket(HTTP http, ServerConnection c, String uri, String[] fields, String[] values) {
        String ticketid = "";
        String customerName = "";
        int i=0;
        for (String f: fields) {
            if (f.equals("ticket"))
                ticketid=cleanseInput(values[i]);
            if (f.equals("customer"))
                customerName=cleanseInput(values[i]);
            i++;
        }
        queryTickets(http,c,uri,"id='"+ticketid+"' AND customerName='"+customerName+"'");
    }

    private void deleteTicket(HTTP http, ServerConnection c, String uri, String ticketid) {
        String username = c.getCookie("usr");
        System.out.println("checking rights for "+username);
        boolean canDoThis=false;
        if (username.equalsIgnoreCase("rzadmin")) {
            System.out.println("rights check success!");
            canDoThis=true;
        } else {
            System.out.println("rights check failed!");
        }
        if (!canDoThis)
            return;
        new ServerQuery(this,c,QUERYTYPE_DELETE_TICKET, "DELETE FROM tickets WHERE id='"+ticketid+"'") {
            public void done() {
                /*if (this.responseSize()!=1) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }*/
                String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),"TICKET #"+ticketid+" DELETED SUCCESSFULLY!"});
                // reply(c,r);
                c.sendMessage(r);
                c.disconnect();
            }
        };
    }

    //queryTickets: used when customers search their tickets
    private void queryTickets(HTTP http, ServerConnection c, String uri, String where) {
        new ServerQuery(this,c,QUERYTYPE_VIEW_TICKET, "select id,title,customerName,status,dueDate from tickets where "+where) {
            public void done() {
                if (this.responseSize()!=1) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                T.addHrefToColumn("id","tickets");
                if (c.getCookie("usr").equals("rzadmin")) {
                  //  T.appendColumnToEnd("modify", "edit ticket");
                    T.appendColumnToEnd("delete", "REMOVE TICKET");
                    T.addHrefToColumn("delete","tickets","id");
                }
                //T.appendStyle(" class='center'");
                //reply();
               // String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),T.toString()});
                // reply(c,r);
               // c.sendMessage(r);
               // c.disconnect();
                System.out.println("sending ticket response...");
                reply(c,RESPONSE_SHOW_TICKET+";;;"+T.toString());

            }
        };
    }

    //get info for a single ticket
    //GET tickets?id=ticketid
    private void queryTicketByID(HTTP http, ServerConnection c, String uri, String ticketid) {
        new ServerQuery(this,c,QUERYTYPE_VIEW_TICKET, "select * from tickets where id='"+ticketid+"'") {
            public void done() {
                if (this.responseSize()!=1) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                if (c.getCookie("usr").equals("rzadmin")) {
                    T.appendColumnToEnd("modify", "edit ticket");
                    T.appendColumnToEnd("delete", "delete ticket");
                    T.addHrefToColumn("delete","tickets","id");
                    T.addHrefToColumn("modify","tickets","id");
                }
                //T.appendStyle(" class='center'");
                //reply();
                String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),T.toString()});
                // reply(c,r);
                c.sendMessage(r);
                c.disconnect();
            }
        };
    }

    private void enterNewTicket(ServerConnection c, String[] fields, String[] values) {
        System.out.println("[TicketProcessing] Create new ticket");
        int[] ticketid = {0};
        generateNewTicketID(ticketid);
        while (ticketid[0] == 0) {

        }
        System.out.println("passed the ticket id while loop");
        String title, name, email, phone, info, due, status;
        title = name = email = phone = info = due = status = "";
        for (int i = 0; i < fields.length; i++) {
            System.out.println("fields[i]=" + fields[i] + ", values[i]=" + values[i]);
            if (fields[i].equals("customerName"))
                name = cleanseInput(values[i]);
            if (fields[i].equals("customerPhone"))
                phone = values[i];
            if (fields[i].equals("customerEmail"))
                email = cleanseInput(values[i]);
            if (fields[i].equals("title"))
                title = cleanseInput(values[i]);
            if (fields[i].equals("info"))
                info = cleanseInput(values[i]);
            if (fields[i].equals("due"))
                due = cleanseInput(values[i]);
        }
        Ticket T = new Ticket(ticketid[0], title, name, email, phone, info, due);
        storeTicket(T,c,QUERYTYPE_NEWTICKET);
    }


    private void queryAllTicketsToTable(ServerConnection c) {

        new ServerQuery(this,c,QUERYTYPE_SEARCH_TICKETS,"select id,title,customerName,status,dueDate from tickets;") {
            String buildHTML = "";
            public void done() {
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                //T.printColumnData();
                T.addBasicBorders();
                T.addHrefToColumn("id","tickets");
                //T.printColumnData();


                //T.appendColumnToEnd("modify","<a href=/tickets?update>edit ticket</a>");

                T.appendColumnToEnd("delete","remove ticket");
                T.addHrefToColumn("delete","tickets","id");
                buildHTML = T.toString();//HTMLGenerator.generateTable(this.response_getFields(),this.response_getValues());
                reply(c,RESPONSE_SHOW_TICKETS+";;;"+buildHTML);
            }
        };
    }



    // old method
    private void queryAllTickets(ServerConnection c) {
        new ServerQuery(this,c,QUERYTYPE_SEARCH_TICKETS,"select id,title,customerName,status,dueDate from tickets;") {
            String buildHTML = "<pre>[Ticket ID]\t\t\t[Title]\t\t\t[Customer Name]\t\t\t[Status]\t\t\t[Due Date]</pre><br>";

            String[] tableColNames = {};

            public void done() {
              //  buildHTML += "<pre>69420\t\tbroken phone\t\tbob\t\tawaiting diagnosis\t\t4/20/2022</pre><br>";
                for (int i=0; i<this.getResponses().size(); i++) {
                    String r = this.getResponses().get(i);
                    String[][] fv = this.responseParams(i);
                    buildHTML += "<pre><a href=\"tickets.html?id="+this.responseParamValue(i,"id")+"\">"+this.responseParamValue(i,"id")+"</a>\t\t\t";
                    buildHTML += ""+this.responseParamValue(i,"title")+"\t\t\t";
                    buildHTML += ""+this.responseParamValue(i,"customerName")+"\t\t\t";
                    buildHTML += ""+this.responseParamValue(i,"status")+"\t\t\t";
                    buildHTML += ""+this.responseParamValue(i,"due")+"</pre><br>";
                  //  buildHTML+="Data for Ticket #"+this.responseParamValue(i,"ticket_id")+"<br>- - - - - - - - - - - - - - -<br>";
                 /*   for (int j=0; j<fv[0].length; j++) {
                        String field = fv[0][j];
                        String value = fv[1][j];

                        buildHTML+= field+": "+value+"<br>";

                    }*/
                //    buildHTML+="~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~<br>";
                }
               /* for (String x: this.getResponses()) {
                    buildHTML+= "{"+x+"}<br>";
                }
                */

                reply(c,RESPONSE_SHOW_TICKETS+";;;"+buildHTML);
            }
        };
    }

    private void generateNewTicketID(int[] buf) {
        int x = 1+(int)(Math.random()*100000000);
        boolean found = false;
        boolean found2 = false;
        new ServerQuery(this,"select id from tickets where id='"+x+"'") {
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
        new ServerQuery(this,c,type,"INSERT INTO tickets(id,title,customerName,customerPhone,customerEmail,info,status,dueDate) VALUES('"+T.getId()+"','"+T.getTitle()+"','"+T.getCustName()+"','"+T.getCustPhone()+"','"+T.getCustEmail()+"','"+T.getInfo()+"','"+T.getStatus()+"','"+T.getDue()+"')") {
            public void done() {
                System.out.println("inserted ticket "+T.getId()+"!");
                reply(c,RESPONSE_SUCCESS+":"+T.getId());
               // c.sendMessage(HTTP.HTTP_OK+"\r\n"+RESPONSE_SUCCESS);
               // c.disconnect();
            }
        };
    }

    public void initTable() {
        new ServerQuery(this,"SHOW TABLES LIKE \""+getTable()+"\"") {
            public void done() {
                if (this.responseSize()==0) {
                    new ServerQuery(this.util(),"CREATE TABLE "+getTable()+"(id TEXT, title TEXT, customerName TEXT, customerPhone TEXT, customerEmail TEXT, info TEXT, status TEXT, dueDate TEXT)") {
                        public void done() {
                            System.out.println("Successfully initialized database table: tickets");
                        }
                    };
                }
            }
        };
    }

    private String cleanseInput(String input) {
        return input.replace("+"," ").replace("%40","@").replace("%3B",";");
    }

}