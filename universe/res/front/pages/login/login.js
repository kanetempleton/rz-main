//import axios from 'axios';

//https://stackoverflow.com/questions/5639346/what-is-the-shortest-function-for-reading-a-cookie-by-name-in-javascript
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

function trylogin() {

const URL=''

	const un = document.getElementById("enterUsername").value;
	const pw = document.getElementById("enterPassword").value;
	if (un.length == 0 || pw.length==0) {
	    document.getElementById("msgText").innerHTML = "Need nonzero length for username and password.";
	    return;
	}

    var pw2 = rsaEncrypt(5,38407,pw);
	const sendme = {
	    packet:1,
		username:un,
		password:pw,
		end:0
	}

	document.getElementById("msgText").innerHTML = "Waiting for reply...";
$.ajax({
        url: URL,
        type: 'PUT',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: sendme,
        success: function(result) {
            // Do something with the result
            console.log(result)

            if (result == 'retry') {
                trylogin();
            }
            else if (result == 'loginsuccess') {
                document.getElementById("msgText").innerHTML = "Login succeeded.";
                drawLoggedInPage();
            }
            else if (result == 'logininvalid') {
                document.getElementById("msgText").innerHTML = "Invalid password.";
            }
            else if (result == 'loginsilly') {
                document.getElementById("msgText").innerHTML = "Server encountered formatting issues with checking your login details... please contact an admin.";
            }
            else if (result == 'logindne') {
                document.getElementById("msgText").innerHTML = "No user with that username exists. Try <a href='register.html'>registering.</a>";
            }
            else if (result == 'urbannedgtfo') {
                document.getElementById("msgText").innerHTML = "This user account is banned. Contact an admin if you think this is a mistake (it's probably not).";
            }
            else if (result == 'toomanyattempts') {
                document.getElementById("msgText").innerHTML = "Too many invalid logins. Login attempts for this username will be blocked for 5 minutes.";
            }
            else {
                document.getElementById("msgText").innerHTML = "Bad response code from server. Could not log in.";
            }
        }
    });
}

function trylogout() {

const URL=''

   var usr = readCookie("usr");
   if (usr!=null && usr!="none") {
       const sendme={
            packet:3,
            username:usr,
            end:0
        }
       document.getElementById("msgText").innerHTML = "Waiting for reply...";
       $.ajax({
               url: URL,
               type: 'PUT',
               contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
               data: sendme,
               success: function(result) {
                   // Do something with the result
                   console.log(result)

                   if (result == 'retry') {
                       trylogout();
                   }
                   else if (result == 'logoutbye') {
                       document.getElementById("msgText").innerHTML = "Logout successful.";
                       drawLogInPage();
                   }
                   else {
                       document.getElementById("msgText").innerHTML = "Bad response code from server. Could not log out.";
                   }
               }
           });
   }
   else {
        document.getElementById("msgText").innerHTML = "You are not currently logged in.";
   }

}

function stringToBytesFaster ( str ) {
var ch, st, re = [], j=0;
for (var i = 0; i < str.length; i++ ) {
    ch = str.charCodeAt(i);
    if(ch < 127)
    {
        re[j++] = ch & 0xFF;
    }
    else
    {
        st = [];    // clear stack
        do {
            st.push( ch & 0xFF );  // push byte to stack
            ch = ch >> 8;          // shift value down by 1 byte
        }
        while ( ch );
        // add stack contents to result
        // done because chars have "wrong" endianness
        st = st.reverse();
        for(var k=0;k<st.length; ++k)
            re[j++] = st[k];
    }
}
// return an array of bytes
return re;
}


function rsaEncrypt(e, n, M) {
       var mm = stringToBytesFaster(M);
        var outp = "";
        for (var i=0; i<mm.length; i++) {
            var mi = mm[i];
            var encr = Math.pow(mi,e)%n;
            var ss = encr.toString(16);
            outp = outp+""+ss;
        }
        console.log("final output = "+outp);
        return outp;
 }

var loginbutton = document.getElementById("loginButton");
var forgotpwbutton = document.getElementById("forgotPassButton");
loginbutton.addEventListener ("click", function() {
    if (loginbutton.innerHTML == "Log out") {
        trylogout();
    } else {
        trylogin();
    }
});


function drawLoggedInPage() {
    document.getElementById("statusCode").innerHTML = "Currently logged in as: "+readCookie("usr");
    loginButton.innerHTML = "Log out";
    forgotpwbutton.style.display = "none";//set to "block" to show it again??
    document.getElementById("enterPassword").style.display = "none";
    document.getElementById("enterUsername").style.display = "none";
    document.getElementById("pleaseText").style.display = "none";
    document.getElementById("registerText").style.display = "none";
    document.getElementById("enterUsername").value = "";
    document.getElementById("enterPassword").value = "";
}

function drawLogInPage() {
    document.getElementById("statusCode").innerHTML = "";
    loginButton.innerHTML = "Log in";
    forgotpwbutton.style.display = "initial";
    document.getElementById("enterPassword").style.display = "initial";
    document.getElementById("enterUsername").style.display = "initial";
    document.getElementById("pleaseText").style.display = "initial";
    document.getElementById("registerText").style.display = "initial";
}

function render() {
document.getElementById("enterPassword").style.display = "none";
    var usr = readCookie("usr");
    if (usr!=null && usr!="none") {
        drawLoggedInPage();
    } else {
        drawLogInPage();
    }
}


render();
