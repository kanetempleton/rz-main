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

	const argz = "";

	if (ticketID.length < 1 || custName.length < 1) {
	    document.getElementById("statusCode").innerHTML = "Please fill out both ticket ID and customer name.";
	    return;
	}


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
    });
}


function tryemployeequery() {
console.log("tryemployeequery()");

const URL='fuzz'


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
                document.getElementById("employeeStatusCode").innerHTML = "Ticket #"+result.split(":")[1]+" successfully created!";
//                document.getElementById("output").innerHTML = "ticket.id<br>ticket.title<br>ticket.customer<br>ticket.status<br>ticket.info";
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


function drawTicketPageAsEmployee() {
    document.getElementById("employeeAccess").removeAttribute("hidden");
}

function render() {
    var usr = readCookie("usr");
    if (usr!=null && usr!="none") {
        drawTicketPageAsEmployee();
    } else {

    }
}

render()