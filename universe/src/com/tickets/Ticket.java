package com.tickets;

import com.server.entity.*;
import com.db.*;
import com.db.crud.*;

public class Ticket extends CRUDObject {

    private String title,customerName,customerEmail,customerPhone,info,dueDate,status;
    private String lastModifiedDate,lastModifiedBy,hidden;
    private TicketProcessing manager;
    public Ticket(CRUDHandler H, int id, String title, String customerName, String customerEmail, String customerPhone, String info, String due) {
        super(H,""+id);
        this.title = title;
        this.customerName=customerName;
        this.customerPhone=customerPhone;
        this.customerEmail=customerEmail;
        this.info=info;
        this.dueDate=due;
        this.status = "Awaiting diagnosis";
        this.lastModifiedBy = "rzadmin";
        this.lastModifiedDate = "not assigned";
        this.hidden = "0";
    }


    public String getCustEmail() {
        return customerEmail;
    }

    public String getCustName() {
        return customerName;
    }

    public String getCustPhone() {
        return customerPhone;
    }

    public String getDue() {
        return dueDate;
    }

    public String getInfo() {
        return info;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public void setCustEmail(String custEmail) {
        this.customerEmail = custEmail;
    }

    public void setCustName(String custName) {
        this.customerName = custName;
    }

    public void setCustPhone(String custPhone) {
        this.customerPhone = custPhone;
    }

    public void setDue(String due) {
        this.dueDate = due;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLastModifiedDate(String date) {
        this.lastModifiedDate = date;
    }
    public void setLastModifiedBy(String name) {this.lastModifiedBy=name;}
    public void setHidden(boolean b) {
        this.hidden=""+b;
    }
    public boolean hidden(){return Boolean.parseBoolean(this.hidden);}

}