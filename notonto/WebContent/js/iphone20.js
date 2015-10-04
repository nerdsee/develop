/*function answer() {
	var question = document.getElementById("question");
	var answer = document.getElementById("answer");
	
	answer.style.visibility="visible";
	question.style.visibility="hidden";
}

function question() {
	var question = document.getElementById("question");
	var answer = document.getElementById("answer");
	
	answer.style.visibility="hidden";
	question.style.visibility="visible";
}
*/

var timer;
var intervall=0;
var offset;
var step=50;
var dout;
var din;
var direction;

setTimeout(scrollTo, 100, 0, 1);

function nextQuestion(ulid, uiid, a) {
	var	url = "/iphone/itemel.jsp?load&ulid="+ulid+"&uiid="+uiid+"&action="+a;
	//alert(url);
	//$("card").className="card leftout";
	showPageByHref(url);
}

function showPageByHref(href, args, method, replace, cb)
    {
        var req = new XMLHttpRequest();
        req.onerror = function()
        {
            if (cb)
                cb(false);
        };
        
        req.onreadystatechange = function()
        {
            if (req.readyState == 4)
            {
                if (replace)
                    replaceElementWithSource(replace, req.responseText);
                else
                {
                    var frag = document.createElement("div");
                    frag.innerHTML = req.responseText;
                    insertPages(frag.childNodes);
                }
                if (cb)
                    setTimeout(cb, 1000, true);
            }
        };

        if (args)
        {
            req.open(method || "GET", href, true);
            req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            req.setRequestHeader("Content-Length", args.length);
            req.send(args.join("&"));
        }
        else
        {
            req.open(method || "GET", href, true);
            req.send(null);
        }
    }

function insertPages(nodes)
    {
        var targetPage;
        for (var i = 0; i < nodes.length; i++)
        {
        	//alert("i: " + i);
            var child = nodes[i];
            if (child.nodeType == 1)
            {
                var clone = $(child.id);
                if (clone) {
                	//alert("cid " + child.id);
                	if (child.id == "question") {
                    	ntreplace(child, clone);
                    	question();
                    } else
                    	setTimeout(ntreplace, 1500, child, clone);
                } else {
                    document.body.appendChild(child);
                }
                //--i;
            }
        }
    }

function ntreplace(child, clone) {
	clone.parentNode.replaceChild(child, clone);
}

function question() {
        flipPages($("answer"), $("question"), 0);
}

function answer() {
        flipPages($("question"), $("answer"), 1);
}


function flipPages(fromPage, toPage, backwards)
{
	var element = $("card");
//alert("EL: " + element);
	/* Toggle the setting of the classname attribute */
	element.className = (element.className == 'card') ? 'card flipped' : 'card';
}

function $(id) { return document.getElementById(id); }
	
