package com.tickets;

import com.server.entity.*;
import com.server.protocol.*;
import com.db.*;
import com.util.html.*;
import com.server.web.Cookie;
import com.db.crud.*;
import com.util.Tools;


public class TicketProcessing extends CRUDHandler {

    private static final int MAX_TICKET_ID=100000; //TODO: format ticket id as xxxx-xxxx

    private boolean hasAdminAccess(ServerConnection c) {
        return true;
        //return c.getCookie("usr").equals("rzadmin");
    }

    //title,customerName,customerEmail,customerPhone,info,dueDate,status;
    public TicketProcessing() {
        super("tickets","id");
        String[] save = {"title","customerName","customerPhone","customerEmail",
                            "info","status","dueDate","lastModifiedDate","lastModifiedBy","hidden"};
        setTypeForField("info","VARCHAR(4096)");
        setSaveFields(save);
    }

    public void start() {
        System.out.println("started the ticket system!");
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
                queryAllTicketsToTable(c,fields,values);
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
    public byte[] processGET(HTTP http, ServerConnection c, String uri, String res, String[] fields, String[] values) {
       // System.out.println("PROCESSING CUSTOM GET!!!!!!!!"+uri);
        boolean properFormat = false;
        String function = "get";
        String ticketid = "";
        for (int i=0; i<fields.length; i++) {
            if (uri.contains("/tickets/view")) {
                function = "view";
            }
            else if (uri.contains("/tickets/modify")) {
                function = "modify";
            }
            else if (uri.contains("/tickets/delete")) {
                function = "delete";
            }
            else if (uri.contains("/tickets/hide")) {
                function = "hide";
            }
            if (fields[i].equals("id")) {
                properFormat=true;
                ticketid = values[i];
               // function = "view";
            }
        }
        //if (!properFormat)
        //    return null;
       /* if (uri.contains("/tickets/view")) {
            System.out.println("TICKET VIEW DETECTION SUXXESS!!!!!!!!!!");
            function = "view";
        }
*/
        c.setNeedsReply(true);

        switch (function) {
            case "view":
                queryTicketByID(http,c,uri,res,ticketid);
                break;
            case "modify":
                queryTicketByIDWithEdit(http,c,uri,res,ticketid);
                break;
            case "delete":
                deleteTicket(http,c,uri,res,ticketid);
                break;
            case "hide":
                //hideTicket(http,c,uri,res,ticketid);
                editTicket(http,c,uri,new String[]{"id","hidden"}, new String[]{""+ticketid,"1"});
                break;
            default:
                function = "query";
                queryTicketsByParams(http,c,uri,res,fields,values,ticketid);
                break;
        }

        /*c.setNeedsReply(true);
        if (function.equals("query")) {
            queryTicketsByParams(http,c,uri,fields,values);
        }

        if (function.equals("id"))
            queryTicketByID(http,c,uri,ticketid);
        if (function.equals("delete"))
            deleteTicket(http,c,uri,ticketid);
        if (function.equals("modify"))
            showEditTicketFields(http,c,uri,ticketid);*/

        return http.WAIT_FOR_RESPONSE.getBytes();
    }

    //NEW METHODS


    //Search Tickets
    //GET tickets?ticketparams
    //currently has no permission checks or restrictions on what fields can be queried
    //needs handling for bs inputs or empty queries
    private void queryTicketsByParams(HTTP http, ServerConnection c, String uri, String res, String[] fields, String[] values, String ticketid) {
        //return this.select_html_table(fields,fields,values);
        boolean all = false;
        if (ticketid.equals("all")) {
            if (!hasAdminAccess(c)) {
                reply(c,RESPONSE_PERMISSION_DENIED);
                return;
            }
            all=true;
        }

        String queryString = "SELECT id,title,customerName,status,dueDate FROM "+this.getTable()+"";

        if (ticketid.equals("all")) {
            if (!Tools.fieldValuePair(fields, values, "showComplete", "1") && !Tools.fieldValuePair(fields, values, "showHidden", "1")) {
                queryString += " WHERE status<>'Completed' AND status<>'COMPLETED' AND status<>'COMPLETE' AND status<>'Complete'" +
                        " AND status<>'Done' AND hidden<>'1'";
            } else if (!Tools.fieldValuePair(fields, values, "showComplete", "1")) {
                queryString += " WHERE status<>'Completed' AND status<>'COMPLETED' AND status<>'COMPLETE' AND status<>'Complete'" +
                        " AND status<>'Done'";
            } else if (!Tools.fieldValuePair(fields, values, "showHidden", "1")) {
                queryString += " WHERE hidden<>'1'";
            }
            if (Tools.fieldValuePair(fields, values, "orderDate", "1")) {
                queryString += " ORDER BY dueDate DESC";
            }
        }
        //queryString+=";";

        //String queryString = "SELECT id,title,customerName,status,dueDate FROM "+this.getTable()+"";
        if (!all) {
            if (fields.length > 0 && values.length > 0 && fields.length == values.length) {
                queryString += " WHERE ";
                for (int i = 0; i < fields.length; i++) {
                    queryString += fields[i] + "='" + cleanseInput(values[i]) + "'";
                    if (i != fields.length - 1)
                        queryString += " AND ";
                }
            }
        }
        queryString+=";";
        new ServerQuery(this,c,QUERYTYPE_SEARCH_TICKETS,queryString) {
            public void done() {
                if (this.responseSize()==0) {
                    String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),"<strong>No items found</strong>"});
                    // reply(c,r);
                    c.sendMessage(r);
                    c.disconnect();
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                System.out.println("responding with modify success");
                T.addHrefToColumn("id","tickets/view");
                if (hasAdminAccess(c)) {
                    T.appendColumnToEnd("modify", "edit ticket");
                    T.appendColumnToEnd("delete", "delete ticket");
                    T.appendColumnToEnd("hide", "hide ticket");
                    T.addHrefToColumn("delete","tickets/delete","id","id");
                    T.addHrefToColumn("modify","tickets/modify","id","id");
                    T.addHrefToColumn("hide","tickets/hide","id","id");
                }
                //T.appendStyle(" class='center'");
                //reply();
                String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(res),T.toString()});
                // reply(c,r);
                c.sendMessage(r);
                c.disconnect();
                //reply(c,T.toString());
            }
        };
    }




    //View Ticket
    //get info for a single ticket
    //GET tickets/view?id=ticketid
    private void queryTicketByID(HTTP http, ServerConnection c, String uri, String res, String ticketid) {
        new ServerQuery(this,c,QUERYTYPE_VIEW_TICKET, "select * from tickets where id='"+ticketid+"'") {
            public void done() {
                if (this.responseSize()!=1) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                if (hasAdminAccess(c)) {
                    T.appendColumnToEnd("modify", "edit ticket");
                    T.appendColumnToEnd("delete", "delete ticket");
                    T.addHrefToColumn("delete","delete","id","id");
                    T.addHrefToColumn("modify","modify","id","id");
                }
                //T.appendStyle(" class='center'");
                //reply();
                String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(res),T.toString()});
                // reply(c,r);
                c.sendMessage(translateOutput(r));
                c.disconnect();
            }
        };
    }


    //Edit Ticket
    //show modify fields for a single ticket
    //GET tickets/modify?id=ticketid
    private void queryTicketByIDWithEdit(HTTP http, ServerConnection c, String uri, String res, String ticketid) {
        new ServerQuery(this,c,QUERYTYPE_VIEW_TICKET, "select * from tickets where id='"+ticketid+"'") {
            public void done() {
                if (this.responseSize()!=1) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                T.addBasicBorders();
                T.addFormToRow("id",ticketid,"id","info");
                if (hasAdminAccess(c)) {
                    //T.appendColumnToEnd("modify", "edit ticket");
                    T.appendColumnToEnd("modify", "<button id=\"submitChangesButton\" onclick=\"tryEditQuery()\">Submit Changes</button>");

                    T.appendColumnToEnd("delete", "delete ticket");
                    T.addHrefToColumn("delete","delete","id","id");
                 //   T.addHrefToColumn("modify","modify","id","id");

                }
                //T.appendStyle(" class='center'");
                //reply();
                String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(res),T.toString()});
                // reply(c,r);
                c.sendMessage(r);
                c.disconnect();
            }
        };
    }


    //Delete Ticket
    //delete a ticket from the database
    //TODO: change this from GET to DELETE request, but i want to go home so fuck it
    private void deleteTicket(HTTP http, ServerConnection c, String uri, String res, String ticketid) {
        String username = c.getCookie("usr");
        for (Cookie x: c.getCookies()) {
            System.out.println("cookie for "+c+": "+x);
        }
        System.out.println("checking rights for "+username);
        boolean canDoThis=false;
        if (hasAdminAccess(c)) {
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
                String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(res),"TICKET #"+ticketid+" DELETED SUCCESSFULLY!"});
                // reply(c,r);
                c.sendMessage(r);
                c.disconnect();
            }
        };
    }

    //View All Tickets
    // "show all tickets" button
    // show summary for all tickets and put them in an HTML table
    private void queryAllTicketsToTable(ServerConnection c, String[] fields, String[] values) {

        String qry = "SELECT id,title,customerName,status,dueDate FROM tickets";

        if (Tools.fieldValuePair(fields,values,"showComp","0")) {
            qry += " WHERE status<>'Completed' AND status<>'COMPLETED' AND status<>'COMPLETE' AND status<>'Complete'";
        }
        if (Tools.fieldValuePair(fields,values,"orderDate","1")) {
            qry += " ORDER BY dueDate'";
        }
        qry+=";";

        new ServerQuery(this,c,QUERYTYPE_SEARCH_TICKETS,qry) {
            String buildHTML = "";
            public void done() {
                if (this.responseSize() == 0) {
                    reply(c,RESPONSE_NO_TICKET);
                    return;
                }
                HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                //T.printColumnData();
                T.addBasicBorders();
                T.addHrefToColumn("id","/tickets/view");
                //T.printColumnData();


                //T.appendColumnToEnd("modify","<a href=/tickets?update>edit ticket</a>");

                T.appendColumnToEnd("modify","edit ticket");
                T.addHrefToColumn("modify","/tickets/modify","id","id");
                buildHTML = T.toString();//HTMLGenerator.generateTable(this.response_getFields(),this.response_getValues());
                reply(c,RESPONSE_SHOW_TICKETS+";;;"+buildHTML);
            }
        };
    }


    private void editTicket(HTTP http, ServerConnection c, String uri, String[] fields, String[] values) {
        if (hasAdminAccess(c)) {
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
                            if (this.responseSize() == 0) {
                                reply(c,RESPONSE_NONSENSE);
                                return;
                            }
                            HTMLTable T = new HTMLTable(this.response_getFields(),this.response_getValues());
                            T.addBasicBorders();
                           // T.addFormToRow("id",ticketid);
                            // T.addHrefToColumn("id","tickets");

                            if (hasAdminAccess(c)) {
                                //  T.appendColumnToEnd("modify", "edit ticket");
                              //  T.appendColumnToEnd("modify", "<button id=\"submitChangesButton\" onclick=\"tryEditQuery()\">Submit Changes</button>");
                                // T.addHrefToColumn("delete","tickets","id");

                            }


                            //T.appendStyle(" class='center'");
                            //reply();
                           // String r = http.multiHTMLResponse_noTags(http.HTTP_OK,new String[]{http.fileHTML_noTags(uri),T.toString()});
                            //reply(c,r);
                            System.out.println("responding with modify success");
                            //reply(c,RESPONSE_MODIFY_SUCCESS+";;;"+T.toString());
                            if (Tools.fieldValuePair(fields,values,"hidden","1")) {
                                String r = http.multiHTMLResponse_noTags(http.HTTP_OK, new String[]{http.fileHTML_noTags(uri), T.toString()});
                               // reply(c,r);
                                c.sendMessage(r);
                                c.disconnect();
                            } else {
                                reply(c,RESPONSE_MODIFY_SUCCESS+";;;"+T.toString());
                            }
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

                if (hasAdminAccess(c)) {
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
                if (hasAdminAccess(c)) {
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


    //POST request for submitting ticket
    private void enterNewTicket(ServerConnection c, String[] fields, String[] values) {
        System.out.println("[TicketProcessing] Create new ticket");
        int[] ticketid = {0};
        generateNewTicketID(ticketid,MAX_TICKET_ID,fields,values,c);
       /* while (ticketid[0] == 0) {

        }*/
        System.out.println("passed the ticket id while loop");

    }





    //find an unused ticket id
    private void generateNewTicketID(int[] buf, int max,String[] fields, String[] values,ServerConnection c) {
        int x = 1+(int)(Math.random()*max);
        boolean found = false;
        boolean found2 = false;
        new ServerQuery(this,"select id from tickets where id='"+x+"'") {
            public void done() {
                if (this.getResponses().size() > 0) {
                    generateNewTicketID(buf,max,fields,values,c);
                    System.out.println("retrying new ticket id...");
                } else {
                    buf[0] = x;
                    System.out.println("ticket id found: "+x+" buffer[0]="+buf[0]);
                    String title, name, email, phone, info, due, status;
                    title = name = email = phone = info = due = status = "";
                    for (int i = 0; i < fields.length; i++) {
                        System.out.println("fields[i]=" + fields[i] + ", values[i]=" + values[i]);
                        if (fields[i].equals("customerName"))
                            name = cleanseInput(values[i]);
                        if (fields[i].equals("customerPhone"))
                            phone = cleanseInput(values[i]);
                        if (fields[i].equals("customerEmail"))
                            email = cleanseInput(values[i]);
                        if (fields[i].equals("title"))
                            title = cleanseInput(values[i]);
                        if (fields[i].equals("info"))
                            info = cleanseInput(values[i]);
                        if (fields[i].equals("due"))
                            due = cleanseInput(values[i]);
                    }
                    Ticket T = new Ticket((CRUDHandler)this.getUtil(),buf[0], title, name, email, phone, info, due);
                    storeTicket(T,c,QUERYTYPE_NEWTICKET);
                }
            }
        };
        System.out.println("generating new customer ticket id...");
    }


    // store a ticket into the database
    public void storeTicket(Ticket T, ServerConnection c, int type) {
        new ServerQuery(this,c,type,"INSERT INTO tickets(id,title,customerName,customerPhone,customerEmail,info,status,dueDate) VALUES('"+T.getID()+"','"+T.getTitle()+"','"+T.getCustName()+"','"+T.getCustPhone()+"','"+T.getCustEmail()+"','"+T.getInfo()+"','"+T.getStatus()+"','"+T.getDue()+"')") {
            public void done() {
                System.out.println("inserted ticket "+T.getID()+"!");
                reply(c,RESPONSE_SUCCESS+":"+T.getID());
               // c.sendMessage(HTTP.HTTP_OK+"\r\n"+RESPONSE_SUCCESS);
               // c.disconnect();
            }
        };
    }

    // initialize the "tickets" database table
    // @TODO: auto-modify table structure
   /* public void initTable() {
        new ServerQuery(this,"SHOW TABLES LIKE \""+getTable()+"\"") {
            public void done() {
                if (this.responseSize()==0) {
                    new ServerQuery(this.util(),"CREATE TABLE "+getTable()+"(id TEXT, title TEXT, customerName TEXT, customerPhone TEXT, customerEmail TEXT, info TEXT, status TEXT, dueDate TEXT)") {
                        public void done() {
                            System.out.println("Successfully initialized database table: tickets");
                        }
                    };
                } else {
                    System.out.println("bro wtf you shouldn't even be able to see this message");
                }
            }
        };
    }*/


    private String translateOutput(String output) {
        return output.replace("newline","<br>");
    }

    //convert POST input data to text
    //might need some work so people don't hack us
    //TODO: separate input cleansing for URLs... like use %20 instead of + for spacebar
    private String cleanseInput(String input) {
        return input.replace("+"," ") //space using +
                .replace("%40","@")
                .replace("%3B",";")
                .replace("%2F","/")
                .replace("%25252C",",") //comma...?
                .replace("%2C",",") //commas
                .replace("%20"," ") //space using %20
                .replace("'","") //remove apostrophes lol tempfix
                .replace("%3C","<")
                .replace("%3E",">")
                .replace("newline","<br>")
                .replace("%0A","<br>")
                .replace("%24","$")
                ;
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