package com.tickets;


public class Ticket {

    private int id;
    private String title,custName,custEmail,custPhone,info,due,status;
    public Ticket(int id, String title, String customerName, String customerEmail, String customerPhone, String info, String due) {
        this.id=id;
        this.title = title;
        this.custName=customerName;
        this.custPhone=customerPhone;
        this.custEmail=customerEmail;
        this.info=info;
        this.due=due;
        this.status = "Awaiting diagnosis";
    }


    public int getId() {
        return id;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public String getCustName() {
        return custName;
    }

    public String getCustPhone() {
        return custPhone;
    }

    public String getDue() {
        return due;
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
        this.custEmail = custEmail;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public void setCustPhone(String custPhone) {
        this.custPhone = custPhone;
    }

    public void setDue(String due) {
        this.due = due;
    }

    public void setId(int id) {
        this.id = id;
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
}