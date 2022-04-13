
// toggles menu item
function toggleDevWidth() {
    x = document.getElementById("devcol").style.width;
    console.log("initial data = " + x);
    if (x == "50px") {
        document.getElementById("devcol").style.width = "200px";
        console.log("expanded devcol width")
    } else {
        document.getElementById("devcol").style.width = "50px";
        console.log("reverted devcol width")
    }
}