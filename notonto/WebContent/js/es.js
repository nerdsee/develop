
function csc(evt, parent, code) {
		if (code==192) {
			if (evt.shiftKey) {
				parent.value+=String.fromCharCode(209); //~N
			} else {
				parent.value+=String.fromCharCode(241); //~n
			}
			// alert("Code2: " + parent);
			return true;
		}
		return false;
}

function parse(parent) {
	return true;
}