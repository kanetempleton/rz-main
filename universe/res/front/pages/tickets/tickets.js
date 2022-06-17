//import axios from 'axios';

function readCookie(name) {
  var nameEQ = name + "=";
  var ca = document.cookie.split(';');
  for(var i=0;i < ca.length;i++) {
    var c = ca[i];
    while (c.charAt(0)==' ') c = c.substring(1,c.length);
    if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
  }
  return null;
}


function trycustomerquery() {
console.log("trycustomerquery()");

const URL='fuzz'


	const ticketID = document.getElementById("enterTicketID").value;
	const custName = document.getElementById("enterCustomerName").value;

	var argz = "";

	if (ticketID.length < 1 && custName.length < 1) {
	    document.getElementById("statusCode").innerHTML = "Please fill out ticket ID or customer name.";
	    return;
	}
	if (ticketID.length > 0 && custName.length>0) {
	    argz += "id="+ticketID+"&customerName="+custName;
	}
	else if (ticketID.length > 0 && custName.length<1) {
        argz += "id="+ticketID;
    }
    else if (ticketID.length<1 && custName.length>0) {
        argz += "customerName="+custName;
    }
    console.log("href to /tickets?"+argz);
	window.location.href = '/tickets?'+argz;


/*
	const sendme={
	    packet:500,
		ticket: ticketID,
		customer:custName,
		end:0
	}
	document.getElementById("statusCode").innerHTML = "Retrieving information...";
$.ajax({
        url: URL,
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: sendme,
        success: function(result) {
            // Do something with the result
            console.log(result)

            if (result == 'retry') {
                trycustomerquery();
            }
            else if (result.startsWith("ticketfound")) {
                document.getElementById("statusCode").innerHTML = "Ticket found! Info for Customer Ticket #"+ticketID+":";
                document.getElementById("output").innerHTML = result.split(";;;")[1];
            }
            else if (result == 'ticketfound') { // TODO
                document.getElementById("statusCode").innerHTML = "Ticket found! Info for Customer Ticket #"+ticketID+":";
                document.getElementById("output").innerHTML = "ticket.id<br>ticket.title<br>ticket.customer<br>ticket.status<br>ticket.info";
            }
            else if (result == 'customerfound') { //TODO
                document.getElementById("statusCode").innerHTML = "Customer found! Displaying ticket history for "+custName+":";
                document.getElementById("output").innerHTML = "ticket_i.id, ticket_i.title, ticket_i.status";

            }
            else if (result == 'ticketnotfound') { //TODO
                document.getElementById("statusCode").innerHTML = "No tickets with that ID were found. Try searching your customer name for open tickets. ";
            }
            else if (result == 'customernotfound') { //TODO
                document.getElementById("statusCode").innerHTML = "No tickets were found for "+custName+". Please contact us.";
            }
            else {
                document.getElementById("statusCode").innerHTML = "Unrecognized server response.";
            }
        }
    });*/
}

function tryajaxquery() {
const URL = 'tickets'
const sendme={
    packet:502,
    end:0
}
$.ajax({
 url:URL,
 type:'POST',
 contentType:'application/json',
 data:sendme,
 success: function(result) {
 console.log(result)
 }
});
}

function tryshowquery() {
console.log("tryshowquery()");

const URL='fuzz'

    const showCompleted = 0;
    if (document.getElementById("showcomplete").checked)
        showCompleted = 1;
    const orderByDate = 0;
    if (document.getElementById("orderbydate").checked)
        orderByDate = 1;
	const sendme={
	    packet:502,
	    showComp: showCompleted,
	    orderDate: orderByDate,
		end:0
	}
	document.getElementById("employeeStatusCode").innerHTML = "Retrieving information...";
$.ajax({
        url: URL,
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: sendme,
        success: function(result) {
            // Do something with the result
            console.log(result)

            if (result == 'retry') {
                tryshowquery();
            }
            else if (result.startsWith('alltickets')) { // TODO
                document.getElementById("employeeStatusCode").innerHTML = "Tickets have been retrieved! Check below for data.";
                document.getElementById("output").innerHTML = result.split(";;;")[1];
            }
            else {
                document.getElementById("employeeStatusCode").innerHTML = "Unrecognized server response.";
            }
        }
    });
}


function tryemployeequery() {
console.log("tryemployeequery()");

const URL='/tickets'


	const custName = document.getElementById("customerName").value;
	const custPhone = document.getElementById("customerPhone").value;
	const custEmail = document.getElementById("customerEmail").value;
	const ticketTitle = document.getElementById("ticketTitle").value;
	const ticketInfo = document.getElementById("ticketInfo").value;
	const ticketDueDate = document.getElementById("ticketDue").value;

    document.getElementById("employeeStatusCode").innerHTML = "";
	if (custName.length < 1) {
	    document.getElementById("employeeStatusCode").innerHTML += "Customer's name is required.<br>";
	}
	if (custPhone.length < 1 && custEmail.length < 1) {
    	document.getElementById("employeeStatusCode").innerHTML += "At least one of customer phone/email is required.<br>";
    }
    if (ticketTitle.length < 1) {
        document.getElementById("employeeStatusCode").innerHTML += "Please give this ticket a brief title. For example 'Galaxy S11 screen crack'<br>";
    }
    if (ticketInfo.length < 1) {
        document.getElementById("employeeStatusCode").innerHTML += "Please describe the details of this ticket in the 'ticket info' box.<br>";
    }
    if (ticketDueDate.length < 1) {
        document.getElementById("employeeStatusCode").innerHTML += "Please enter a due date for this ticket. Time until an initial diagnosis = 24 hours.<br>";
    }

    if (document.getElementById("employeeStatusCode").innerHTML.length > 0) {
        return;
    }

	const sendme={
	    packet:501,
		customerName: custName,
		customerPhone: custPhone,
		customerEmail: custEmail,
		title: ticketTitle,
		info: ticketInfo,
		due: ticketDueDate,
		end:0
	}
	document.getElementById("employeeStatusCode").innerHTML = "Retrieving information...";
$.ajax({
        url: URL,
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: sendme,
        success: function(result) {
            console.log("sent data "+sendme)
            // Do something with the result
            console.log(result)

            if (result == 'retry') {
                tryemployeequery();
            }
            else if (result.startsWith('success')) { // TODO
                document.getElementById("employeeStatusCode").innerHTML = "<a href=/tickets/view?id="+result.split(":")[1]+">Ticket #"+result.split(":")[1]+"</a> successfully created!";
//                document.getElementById("output").innerHTML = "ticket.id<br>ticket.title<br>ticket.customer<br>ticket.status<br>ticket.info";
                clearEmployeeInputForms();
            }
            else if (result == 'inprogress') { //TODO
                document.getElementById("employeeStatusCode").innerHTML = "Your input was formatted correctly, but the server's response protocol is still in progress.<br>Check back soon to test the full functionality.";
             //   document.getElementById("output").innerHTML = "ticket_i.id, ticket_i.title, ticket_i.status";

            }
            else if (result == 'failure') { //TODO
                document.getElementById("employeeStatusCode").innerHTML = "Ticket was not created. There might be an error with your input.";
            }
            else {
                document.getElementById("statusCode").innerHTML = "Unrecognized server response. Please scream at the web admin about this.";
            }
        }
    });
}

function tryEditQuery() {
console.log("tryeditquery()");

const URL='/tickets'

/*
    var obj = new Object();
       obj.packet=505;
       obj.customerName = document.getElementById("form_customerName").value;
       obj.id  = document.getElementById("form_id").value;
       obj.status = document.getElementById("form_status").value;
       obj.customerEmail=document.getElementById("form_customerEmail").value;
       obj.customerPhone = document.getElementById("form_customerPhone").value;
       obj.title = document.getElementById("form_title").value;
       	obj.info = document.getElementById("form_info").value;
       	obj.dueDate = document.getElementById("form_dueDate").value;
       	var jsonString= JSON.stringify(obj);
*/
	const custName = document.getElementById("form_customerName").value;
	const custPhone = document.getElementById("form_customerPhone").value;
	const custEmail = document.getElementById("form_customerEmail").value;
	const ticketTitle = document.getElementById("form_title").value;
	const ticketInfo = document.getElementById("form_info").value;
	const ticketDueDate = document.getElementById("form_dueDate").value;
	const ticketStatus = document.getElementById("form_status").value;
	const ticketID = document.getElementById("data_id").innerHTML;

	const sendme={
    	    packet:505,
    	    id: ticketID,
    		customerName: custName,
    		customerPhone: custPhone,
    		customerEmail: custEmail,
    		title: ticketTitle,
    		info: ticketInfo,
    		dueDate: ticketDueDate,
    		status: ticketStatus,
    		end:0
    	}
    	console.log("sent "+sendme)
    	document.getElementById("statusCode").innerHTML = "Retrieving information...";

    $.ajax({
            url: URL,
            type: 'POST',
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8', //'application/json'
            data: sendme,
            success: function(result) {
                //console.log("sent data "+sendme)
                // Do something with the result
                console.log("received reply: "+result)

                if (result == 'retry') {
                 //   tryEditQuery();
                 document.getElementById("statusCode").innerHTML = "check server."
                }
                else if (result.startsWith('modifysuccess')) { // TODO
                    document.getElementById("employeeStatusCode").innerHTML = "<a href=/tickets/view?id="+ticketID+">Ticket #"+ticketID+"</a> successfully modified!";
    //                document.getElementById("output").innerHTML = "ticket.id<br>ticket.title<br>ticket.customer<br>ticket.status<br>ticket.info";
                    document.getElementById("output").innerHTML = result.split(";;;")[1];
                  //  clearEmployeeInputForms();
                }
                else if (result == 'inprogress') { //TODO
                    document.getElementById("employeeStatusCode").innerHTML = "Your input was formatted correctly, but the server's response protocol is still in progress.<br>Check back soon to test the full functionality.";
                 //   document.getElementById("output").innerHTML = "ticket_i.id, ticket_i.title, ticket_i.status";

                }
                else if (result == 'failure') { //TODO
                    document.getElementById("employeeStatusCode").innerHTML = "Ticket was not created. There might be an error with your input.";
                }
                else {
                    document.getElementById("statusCode").innerHTML = "Unrecognized server response. Please scream at the web admin about this.";
                }
            }
        });
}

var submitButton = document.getElementById("searchButton");
submitButton.addEventListener ("click", function() {
    trycustomerquery();
});
var employeeAddButton = document.getElementById("newTicketButton");
employeeAddButton.addEventListener ("click", function() {
    tryemployeequery();
});
var showAllTicketsButton = document.getElementById("showAllTicketsButton");
showAllTicketsButton.addEventListener ("click", function() {
    //tryshowquery();
    var x = '/tickets?id=all'
        if (document.getElementById("showcomplete").checked)
            x = x+'&showComplete=1'
        if (document.getElementById("orderbydate").checked)
            x = x+'&orderDate=1'
        if (document.getElementById("showhidden").checked)
            x = x+'&showHidden=1'
    window.location.href = x
});
/*
var submitChangesButton = document.getElementById("submitChangesButton");
submitChangesButton.addEventListener ("click", function() {
    tryeditquery();
});*/




function drawTicketPageAsEmployee() {
    document.getElementById("employeeAccess").removeAttribute("hidden");
}

function clearEmployeeInputForms() {
    document.getElementById("customerName").value = "";
    document.getElementById("customerPhone").value = "";
    document.getElementById("customerEmail").value = "";
    document.getElementById("ticketTitle").value = "";
    document.getElementById("ticketInfo").value = "";
    document.getElementById("ticketDue").value = "";
}

function render() {
    var usr = readCookie("usr");
    if (usr!=null && usr!="none") {
        drawTicketPageAsEmployee();
    } else {
     drawTicketPageAsEmployee();
    }
}

render()