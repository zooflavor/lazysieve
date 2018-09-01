var allParams;
var funcBases;
var funcs;
var sum;

function buildFuncsTable() {
	var funcsTableHeadRecord
			=document.getElementById("table-head-record-funcs");
	while (funcsTableHeadRecord.lastChild) {
		funcsTableHeadRecord.removeChild(funcsTableHeadRecord.lastChild);
	}
	
	var th=document.createElement("th");
	funcsTableHeadRecord.appendChild(th);
	var checkbox=document.createElement("input");
	th.appendChild(checkbox);
	checkbox.id="checkall";
	checkbox.type="checkbox";
	checkbox.checked=true;
	checkbox.onclick=checkAll;
	
	var th=document.createElement("th");
	funcsTableHeadRecord.appendChild(th);
	
	var th=document.createElement("th");
	funcsTableHeadRecord.appendChild(th);
	th.innerHTML="f";
	th.style="font-family: monospace;";
	
	var th=document.createElement("th");
	funcsTableHeadRecord.appendChild(th);
	th.innerHTML="&Sigma;hiba<sup>2</sup>";
	th.style="font-family: monospace;";
	
	for (var ii=0; allParams.length>ii; ++ii) {
		var th=document.createElement("th");
		funcsTableHeadRecord.appendChild(th);
		th.innerHTML=allParams[ii];
		th.style="font-family: monospace;";
	}

	var funcsTableBody=document.getElementById("table-body-funcs");
	while (funcsTableBody.lastChild) {
		funcsTableBody.removeChild(funcsTableBody.lastChild);
	}
	for (var key in funcs) {
		var func=funcs[key];
		var tr=document.createElement("tr");
		funcsTableBody.appendChild(tr);

		var td=document.createElement("td");
		tr.appendChild(td);
		var checkbox=document.createElement("input");
		td.appendChild(checkbox);
		checkbox.id="check-"+key;
		checkbox.type="checkbox";
		checkbox.checked=true;
		checkbox.onclick=refit;

		var td=document.createElement("td");
		tr.appendChild(td);
		td.id="color-"+key;
		td.style="background: "+func.color+"; padding: 10px;";

		var td=document.createElement("td");
		tr.appendChild(td);
		var formula="";
		for (var ii=0; func.bases.length>ii; ++ii) {
			if (0!==ii) {
				formula+=" + ";
			}
			var base=func.bases[func.bases.length-ii-1];
			if (funcBases.one.name===base.name) {
				formula+=func.params[ii];
			}
			else {
				formula+=func.params[ii]+"*"+base.name;
			}
		}
		formula+="";
		td.align="right";
		td.innerHTML=formula;
		td.style="font-family: monospace;";

		var td=document.createElement("td");
		tr.appendChild(td);
		td.id="error-"+key;
		td.align="right";
		td.style="font-family: monospace;";
		
		for (var ii=0; allParams.length>ii; ++ii) {
			var td=document.createElement("td");
			tr.appendChild(td);
			td.id="param-"+key+"-"+allParams[ii];
			td.align="right";
			td.style="font-family: monospace;";
		}
	}
}

function checkAll() {
	var checked=document.getElementById("checkall").checked;
	var anyChecked=false;
	var anyUnchecked=false;
	for (var key in funcs) {
		if (document.getElementById("check-"+key).checked) {
			anyChecked=true;
		}
		else {
			anyUnchecked=true;
		}
	}
	if (!anyChecked) {
		checked=true;
	}
	else if (!anyUnchecked) {
		checked=false;
	}
	document.getElementById("checkall").checked=checked;
	for (var key in funcs) {
		document.getElementById("check-"+key).checked=checked;
	}
	refit();
}

function drawFunction(ff, color) {
	context.strokeStyle=color;
	context.beginPath();
	var px=-1.0;
	context.moveTo(
			factorX*xx+offsetX,
			factorY*ff(xx)+offsetY);
	while (true) {
		px+=0.5;
		if (width+1.0<px) {
			break;
		}
		var xx=(px-offsetX)/factorX;
		context.lineTo(
				factorX*xx+offsetX,
				factorY*ff(xx)+offsetY);
	}
	context.stroke();
}

function fit(name) {
	var func=funcs[name];
	document.getElementById("error-"+name).innerHTML="";
	for (var ii=0; allParams.length>ii; ++ii) {
		document.getElementById("param-"+name+"-"+allParams[ii]).innerHTML="";
	}
	try {
		if (!document.getElementById("check-"+name).checked) {
			return;
		}
		var result=func.fitter(sample);
		if (null===result) {
			document.getElementById("check-"+name).checked=false;
			return;
		}
		drawFunction(result.ff,
				document.getElementById("color-"+name).style.backgroundColor);
		for (var param in result.params) {
			document.getElementById("param-"+name+"-"+param).innerHTML
					=result.params[param].toExponential(3);
		}
		var error=[];
		for (var ii=sample.length-1; 0<=ii; --ii) {
			var diff=result.ff(sample[ii].xx)-sample[ii].yy;
			error.push(diff*diff);
		}
		document.getElementById("error-"+name).innerHTML
				=sum(error).toExponential(3);
	}
	catch (error) {
		document.getElementById("error-"+name).innerHTML=""+error;
	}
}

function refit() {
	if (!guiEnabled) {
		if (0>=refitQueue.length) {
			setGuiEnabled(true);
		}
		else {
			var key=refitQueue[0];
			refitQueue.splice(0, 1);
			fit(key);
			window.requestAnimationFrame(refit);
		}
		return;
	}
	setGuiEnabled(false);
	context.fillStyle="purple";
	context.fillRect(0, 0, width, height);
	
	for (var func in funcs) {
		document.getElementById("error-"+func).innerHTML="";
		for (var pp=allParams.length-1; 0<=pp; --pp) {
			document.getElementById("param-"+func+"-"+allParams[pp]).innerHTML
					="";
		}
	}

	sample=[];
	input.value.replace(/(?:\r\n|\r)+/g, "\n").split("\n")
		.forEach(function(line) {
			line=line.trim();
			if (0>=line.length) {
				return;
			}
			var parts=line.split(",");
			if (2!==parts.length) {
				return;
			}
			for (var ii=0; 2>ii; ++ii) {
				parts[ii]=parts[ii].trim();
				if (0>=parts[ii].length) {
					return;
				}
				parts[ii]=parseFloat(parts[ii]);
				if (isNaN(parts[ii])
						|| (!isFinite(parts[ii]))) {
					return;
				}
			}
			sample.push({xx: parts[0], yy: parts[1]});
		});

	sample.sort(function(s0, s1) {
		return s0.xx-s1.xx;
	});
	for (var ii=sample.length-1; 0<ii; --ii) {
		if (sample[ii].xx===sample[ii-1].xx) {
			sample.splice(ii, 1);
		}
	}
	if (0>=sample.length) {
		setGuiEnabled(true);
		return;
	}

	maxX=sample[0].xx;
	maxY=sample[0].yy;
	minX=sample[0].xx;
	minY=sample[0].yy;
	for (var ii=sample.length-1; 0<ii; --ii) {
		maxX=Math.max(maxX, sample[ii].xx);
		maxY=Math.max(maxY, sample[ii].yy);
		minX=Math.min(minX, sample[ii].xx);
		minY=Math.min(minY, sample[ii].yy);
	}
	if (maxX<=minX) {
		++maxX;
		--minX;
	}
	if (maxY<=minY) {
		++maxY;
		--minY;
	}
	factorX=1.0*(width-8)/(maxX-minX);
	factorY=-1.0*(height-8)/(maxY-minY);
	offsetX=4.0-factorX*minX;
	offsetY=height-4.0-factorY*minY;

	context.fillStyle="Ivory";
	context.fillRect(0, 0, width, height);
	context.strokeStyle="LightGray";
	context.beginPath();
	context.moveTo(
			factorX*sample[sample.length-1].xx+offsetX,
			factorY*sample[sample.length-1].yy+offsetY);
	for (var ii=sample.length-2; 0<=ii; --ii) {
		context.lineTo(
			factorX*sample[ii].xx+offsetX,
			factorY*sample[ii].yy+offsetY);
	}
	context.stroke();
	context.fillStyle="Black";
	for (var ii=sample.length-1; 0<=ii; --ii) {
		context.fillRect(
				factorX*sample[ii].xx+offsetX-0.5,
				factorY*sample[ii].yy+offsetY-0.5, 1, 1);
	}
	refitQueue=[];
	for (var key in funcs) {
		refitQueue.push(key);
	}
	window.requestAnimationFrame(refit);
}

function setGuiEnabled(enabled) {
	document.getElementById("checkall").disabled=!enabled;
	document.getElementById("input").disabled=!enabled;
	for (var key in funcs) {
		document.getElementById("check-"+key).disabled=!enabled;
	}
	guiEnabled=enabled;
}

var width=window.innerWidth-50;
var height=window.innerHeight/2;
var canvas=document.createElement("canvas");
canvas.width=width;
canvas.height=height;
var context=canvas.getContext("2d");
document.getElementById("divCanvas").appendChild(canvas);

var input=document.getElementById("input");
document.getElementById("button").onclick=refit;

buildFuncsTable();

var guiEnabled=true;
setGuiEnabled(true);

var refitQueue=[];

var factorX=0.0;
var factorY=0.0;
var maxX=0.0;
var maxY=0.0;
var minX=0.0;
var minY=0.0;
var offsetX=0.0;
var offsetY=0.0;
var sample=[];

refit();
