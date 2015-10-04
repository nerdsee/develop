var prevblow = null;

function userandpass() {
	var ret=true;
	if ( $("#loginpass").val() == '' ) {
		mark($("#loginpass"))
		ret=false;
	} else {
		unmark($("#loginpass"))
	}
	if ( $("#loginname").val() == '' ) {
		mark($("#loginname"))
		ret=false;
	} else {
		unmark($("#loginname"))
	}
	return ret;
}

function mark(inp) {
	inp.addClass('noinp');
}
function unmark(inp) {
	inp.removeClass('noinp');
}

function show_answer() {
	document.getElementById("area_question").className = "hidden";
	document.getElementById("area_answer").className = "shown";
}

function show_question() {
	document.getElementById("area_question").className = "shown";
	document.getElementById("area_answer").className = "hidden";
}

function shure(name) {
	check = confirm("Wollen Sie die Lektion '" + name + "' wirklich löschen?");
	return check;
}

function overlay() {
	el = document.getElementById("overlay");
	el.style.visibility = (el.style.visibility == "visible") ? "hidden"
			: "visible";
}

function overlay_on() {
	el = document.getElementById("overlay");
	el.style.visibility = "visible";
}

function blowme(element) {

	var elp = element;

	// alert("hallo"+elp);
	// element.parentNode.height=200;
	if (prevblow) {
		prevblow.className = "inflate";
	}
	elp.className = "blowup";
	prevblow = elp;
}

var qatab=0;

function bodykeydown(evt) {
	//alert("#"+evt.keyCode);
	if ((evt.keyCode == 39) && (qatab==0)) {
		show_answer();
		qatab=1;
	} else if ((evt.keyCode == 37)&&(qatab==1)) {
		show_question();
		qatab=0;
	} else if ((evt.keyCode == 38)&&(qatab==1)) { //si
		answer_yes();
	} else if ((evt.keyCode == 40)&&(qatab==1)) { //no
		answer_no();
	}
}

function answer_yes() {
	var cb = document.getElementById("ynform:btnyes");
	if(cb) {
		clickit(cb);
	}
}

function answer_no() {
	var cb = document.getElementById("ynform:btnno");
	if(cb) {
		clickit(cb);
	}
}

function clickit(cb) {
	if ( isIE() )
		clickitIE(cb);
	else
		clickitNS(cb);
}

function clickitNS(cb) {
  var evt = document.createEvent("MouseEvent");
  evt.initMouseEvent("click", true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
  cb.dispatchEvent(evt);
}

function clickitIE(cb) {
  cb.click();
}

function checkSpecialChar(evt, parent) {
	if (!evt)
		evt = window.event;
	if (evt.which) {
		code = evt.which;
	} else if (evt.keyCode) {
		code = evt.keyCode;
	}


	kl = document.getElementById("answerform:keyboardLayout").value;

	// alert("Code: " + code);

	if (csc(evt, parent, code)) {
		evt.cancelBubble = true;
		return false;
	}

	if (code == 13) {
		updateData();
		li = document.getElementById("answerform:submitlink");
		li.click();
	}
	return true;
}

function firelogin(evt,a) {
	if (!evt)
		evt = window.event;
	if (evt.which) {
		code = evt.which;
	} else if (evt.keyCode) {
		code = evt.keyCode;
	}

	if (code == 13) {
		li = document.getElementById("firelogin");
		if (li) {
		clickit(li);
		}
	}
	return true;
}

function fb_login() {
li = document.getElementById("fb_login:fblili");
li.click();
}

function updateData() {
	now = document.getElementById("now").value;
	dummy = document.getElementById("answerinput" + now);
	real = document.getElementById("answerform:answerin");

	kl = document.getElementById("answerform:keyboardLayout").value;

	if (kl == "PY") {
		real.value = addtones(dummy.value);
	} else {
		real.value = dummy.value;
	}

	return true;
}

function isIE() {
	if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){ //test for MSIE x.x;
		return true;
	}
	return false;
}