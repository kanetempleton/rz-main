package com.tickets;

import com.server.entity.*;
import com.server.protocol.*;
import com.db.*;
import com.util.html.*;
import com.server.web.Cookie;


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
    public static final int QUERYTYPE_MODIFY_TICKET = 1005;

    public static final int PACKETTYPE_CUSTOMER_VIEW_TICKET = 500;
    public static final int PACKETTYPE_EMPLOYEE_NEW_TICKET = 501;
    public static final int PACKETTYPE_EMPLOYEE_VIEW_ALL_TICKETS = 502;
    public static final int PACKETTYPE_MODIFY_TICKET = 505;


    // CUSTOMER RESPONSE CODES
    private static final String RESPONSE_NO_TICKET = "ticketnotfound";
    // EMPLOYEE RESPONSE CODES
    private static final String RESPONSE_IN_PROGRESS = "inprogress";
    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_FAILURE = "failure";
    private static final String RESPONSE_SHOW_TICKETS = "alltickets";
    private static final String RESPONSE_SHOW_TICKET = "ticketfound";
    private static final String RESPONSE_NONSENSE = "nonsense";
    private static final String RESPONSE_PERMISSION_DENIED = "notallowed";
    private static final String RESPONSE_MODIFY_SUCCESS = "modifysuccess";




    //process data from POST requests
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
            case PACKETTYPE_MODIFY_TICKET:
                editTicket(http,c,uri,fields,values);
                break;
            default:
                reply(c,RESPONSE_NONSENSE);
                break;
        }

    }

    // process custom GET requests.. mainly useful for dynamic page generation using parameters
    public byte[] processGET(HTTP http, ServerConnection c, String uri, String[] fields, String[] values) {
        boolean properFormat = false;
        String function = "get";
        String ticketid = "";
        for (int i=0; i<fields.length; i++) {
            if (fields[i].equals("delete") || fields[i].equals("modify")) {
                properFormat=true;
                ticketid = values[i];
                function = fields[i];
            }
        }
        //if (!properFormat)
        //    return null;

        if (function!="modify" && function!="delete")
            function="query";

        c.setNeedsReply(true);
        if (function.equals("query")) {
            queryTicketsByParams(http,c,uri,fields,values);
        }

        if (function.equals("id"))
            queryTicketByID(http,c,uri,ticketid);
        if (function.equals("delete"))
            deleteTicket(http,c,uri,ticketid);
        if (function.equals("modify"))
            showEditTicketFields(http,c,uri,ticketid);

        return http.WAIT_FOR_RESPONSE.getBytes();
    }

    //NEW METHODS

    //queryTicketsByParams
    //GET tickets?ticketparams
    //currently has no permission checks or restrictions on what fields can be queried
    //needs handling for bs inputs or empty queries
    private void queryTicketsByParams(HTTP http, ServerConnection c, String uri, String[] fields, String[] values) {
        //return this.select_html_table(fields,fields,values);
        String queryString = "SELECT id,title,customerName,status,dueDate FROM "+this.getTable()+"";
        if (fields.length>0 && values.length>0 && fields.length==values.length) {
            queryString+= " WHERE ";
            for (int i=0; i<fields.length; i++) {
                queryString+=fields[i]+"='"+values[i]+"'";
                if (i!=fields.length-1)
                    queryString+=",";
            }
        }
        queryString+=";";
        new ServerQuery(this,c,QUERYTYPE_SEARCH_TICKETS,queryString) {
            public void done() {
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                System.out.println("responding with modify success");
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
                //reply(c,T.toString());
            }
        };
    }


    // OLD METHODS

    private void editTicket(HTTP http, ServerConnection c, String uri, String[] fields, String[] values) {
        if (c.getCookie("usr").equalsIgnoreCase("rzadmin")) {
            String updateQuery = "UPDATE tickets SET ";
            for (int i=0; i<fields.length; i++) {
                if (fields[i].equals("end"))
                    continue;
                updateQuery += fields[i]+"='"+cleanseInput(values[i])+"'";
                if (i<fields.length-1)
                    updateQuery+=",";
            }

            if (updateQuery.endsWith(","))
                updateQuery = updateQuery.substring(0,updateQuery.length()-1);



            String ticketid = getValue("id",fields,values);
            updateQuery+=" WHERE id='"+ticketid+"';";
            new ServerQuery(this,c,QUERYTYPE_MODIFY_TICKET, updateQuery) {
                public void done() {
                    System.out.println("ticket modified successfully!");
                    new ServerQuery(this.getUtil(), c, QUERYTYPE_VIEW_TICKET, "select * from tickets where id='"+ticketid+"'") {
                        public void done() {
                            HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                            T.addBasicBorders();
                           // T.addFormToRow("id",ticketid);
                            // T.addHrefToColumn("id","tickets");

                            if (c.getCookie("usr").equals("rzadmin")) {
                                //  T.appendColumnToEnd("modify", "edit ticket");
                                T.appendColumnToEnd("modify", "<button id=\"submitChangesButton\" onclick=\"tryEditQuery()\">Submit Changes</button>");
                                // T.addHrefToColumn("delete","tickets","id");
                            }


                            //T.appendStyle(" class='center'");
                            //reply();
                           // String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),T.toString()});
                            //reply(c,r);
                            System.out.println("responding with modify success");
                            reply(c,RESPONSE_MODIFY_SUCCESS+";;;"+T.toString());
                           // c.sendMessage(r);
                           // c.disconnect();
                        }
                    };
                    //reply(c,RESPONSE_MODIFY_SUCCESS);
                }
            };
        } else {
            reply(c,RESPONSE_PERMISSION_DENIED);
        }
    }

    private void showEditTicketFields(HTTP http, ServerConnection c, String uri, String ticketid) {
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

    //delete a ticket from the database
    private void deleteTicket(HTTP http, ServerConnection c, String uri, String ticketid) {
        String username = c.getCookie("usr");
        for (Cookie x: c.getCookies()) {
            System.out.println("cookie for "+c+": "+x);
        }
        System.out.println("checking rights for "+username);
        boolean canDoThis=false;
        if (username.equalsIgnoreCase("rzadmin")) {
            System.out.println("rights check success!");
            canDoThis=true;
        } else {
            System.out.println("rights check failed!");
        }
        if (!canDoThis) {
            reply(c,RESPONSE_PERMISSION_DENIED);
            return;
        }
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

    //POST request for submitting ticket
    private void enterNewTicket(ServerConnection c, String[] fields, String[] values) {
        System.out.println("[TicketProcessing] Create new ticket");
        int[] ticketid = {0};
        generateNewTicketID(ticketid,100000);
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


    // "show all tickets" button
    // show summary for all tickets and put them in an HTML table
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

                reply(c,RESPONSE_SHOW_TICKETS+";;;"+buildHTML);
            }
        };
    }

    //find an unused ticket id
    private void generateNewTicketID(int[] buf, int max) {
        int x = 1+(int)(Math.random()*max);
        boolean found = false;
        boolean found2 = false;
        new ServerQuery(this,"select id from tickets where id='"+x+"'") {
            public void done() {
                if (this.getResponses().size() > 0) {
                    generateNewTicketID(buf,max);
                    System.out.println("retrying new ticket id...");
                } else {
                    buf[0] = x;
                    System.out.println("ticket id found: "+x+" buffer[0]="+buf[0]);
                }
            }
        };
        System.out.println("generating new customer ticket id...");
    }


    // store a ticket into the database
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

    // initialize the "tickets" database table
    // @TODO: auto-modify table structure
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

    //convert POST input data to text
    //might need some work so people don't hack us
    //TODO: separate input cleansing for URLs... like use %20 instead of + for spacebar
    private String cleanseInput(String input) {
        return input.replace("+"," ").replace("%40","@").replace("%3B",";").replace("%2F","/").replace("%25252C",",").replace("%2C",",");
    }

    //send a reply with HTTP 200 OK
    //mainly used for responding to POST requests
    private void reply(ServerConnection c, String code) {
        //System.out.println("sending reply: ["+code+"]");
        c.sendMessage(HTTP.HTTP_OK+"\r\n"+code);
        c.disconnect();
    }

    private String getValue(String field, String[] fields, String[] values) {
        for (int i=0; i<fields.length; i++) {
            if (fields[i].equals(field))
                return values[i];
        }
        return "NULL";
    }

}