var BigInteger;

function canvasOnClick(event) {
	if (guiDisabled) {
		return;
	}
	var mx=Math.floor((event.pageX-canvas.offsetLeft)/(pixel+1));
	var my=Math.floor((event.pageY-canvas.offsetTop)/(pixel+1));
	if ((0>mx)
			|| (0>my)
			|| (width<=mx)
			|| (height<=my)) {
		return;
	}
	var offset=my*width+mx;
	bits[offset]=(0===bits[offset])?1:0;
	redraw();
}

function decode() {
	var value=inputStart.value;
	if (""===value) {
		value="0";
	}
	value=new BigInteger(value);
	if (0>=value.compareTo(BigInteger.ZERO)) {
		return;
	}
	disableGui();
	for (var ii=size-1; 0<=ii; --ii) {
		bits[ii]=0;
	}
	redraw();
	resetPrimes();
	window.requestAnimationFrame(function() {
		decode2(value, nextPrime2(new BigInteger(size.toString())));
	});
}

function decode2(start, prime) {
	var offset=sievesAt(start, prime);
	if (offset>=size) {
		enableGui();
		return;
	}
	bits[offset]=1;
	redraw();
	window.requestAnimationFrame(function() {
		decode2(start, nextPrime());
	});
}

function disableGui() {
	guiDisabled=true;
	inputStart.disabled=true;
	buttonDecode.disabled=true;
	buttonEncode.disabled=true;
}

function encode() {
	var start=BigInteger.ONE;
	var period=BigInteger.ONE;
	var remainingBits=0;
	var remainingBits2=[];
	for (var ii=0; bits.length>ii; ++ii) {
		remainingBits2.push(bits[ii]);
		if (0!==bits[ii]) {
			++remainingBits;
		}
	}
	disableGui();
	inputStart.value=start.toString();
	resetPrimes();
	window.requestAnimationFrame(function() {
		encode2(start, period, remainingBits, remainingBits2,
				nextPrime2(new BigInteger(size.toString())));
	});
}

function encode2(start, period, remainingBits, remaingBits2, prime) {
	//console.log("encode2 st "+start+" - pe "+period+" - pr "+prime+" - re "+remainingBits+" - "+remaingBits2);
	if (0>=remainingBits) {
		for (; ; start=start.add(period)) {
			var offset=sievesAt(start, prime);
			if (size<=offset) {
				break;
			}
		}
		inputStart.value=start.toString();
		enableGui();
		return;
	}
	for (; ; start=start.add(period)) {
		var offset=sievesAt(start, prime);
		if ((size>offset)
				&& (0!==remaingBits2[offset])) {
			remaingBits2[offset]=0;
			break;
		}
	}
	--remainingBits;
	period=period.multiply(prime);
	inputStart.value=start.toString();
	window.requestAnimationFrame(function() {
		encode2(start, period, remainingBits, remaingBits2, nextPrime());
	});
}

function enableGui() {
	guiDisabled=false;
	inputStart.disabled=false;
	buttonDecode.disabled=false;
	buttonEncode.disabled=false;
}

function nextPrime() {
	outer: for (; ; primesNext=primesNext.add(BigInteger.ONE)) {
		for (var ii=0; primes.length>ii; ++ii) {
			var prime=primes[ii];
			var cc=prime.multiply(prime).compareTo(primesNext);
			if (0===cc) {
				continue outer;
			}
			if (0<cc) {
				break;
			}
			if (0===primesNext.remainder(prime).compareTo(BigInteger.ZERO)) {
				continue outer;
			}
		}
		var result=primesNext;
		primesNext=primesNext.add(BigInteger.ONE);
		primes.push(result);
		return result;
	}
}

function nextPrime2(min) {
	while (true) {
		var prime=nextPrime();
		if (0<=prime.compareTo(min)) {
			return prime;
		}
	}
}

function resetPrimes() {
	primes=[];
	primesNext=new BigInteger("2");
}

function redraw() {
	context.fillStyle="#000000";
	context.fillRect(0, 0, (pixel+1)*width+1, (pixel+1)*height+1);
	context.fillStyle="#FFFFFF";
	for (var yy=height-1; 0<=yy; --yy) {
		for (var xx=width-1; 0<=xx; --xx) {
			if (0!==bits[yy*width+xx]) {
				context.fillRect((pixel+1)*xx+1, (pixel+1)*yy+1, pixel, pixel);
			}
		}
	}
}

function sievesAt(start, prime) {
	var remainder=start.remainder(prime);
	return (0===remainder.compareTo(BigInteger.ZERO))
			?0
			:parseInt(prime.subtract(remainder).toString());
}

var width=16;
var height=16;
var url=location.href;
var index=url.indexOf("?");
if (0<=index) {
	url.substring(index+1).split("&").forEach(function(param) {
		var index=param.indexOf("=");
		if (0<=index) {
			var key=param.substring(0, index);
			var value=param.substring(index+1);
			if ("w"===key) {
				width=parseInt(value);
			}
			else if ("h"===key) {
				height=parseInt(value);
			}
		}
	});
}

var guiDisabled=false;

var primes;
var primesNext;
resetPrimes();

var pixel=16;
var size=width*height;
var bits=[];
for (var ii=size; 0<ii; --ii) {
	bits.push(0);
}

var inputStart=document.getElementById("inputStart");
var buttonDecode=document.getElementById("buttonDecode");
var buttonEncode=document.getElementById("buttonEncode");
buttonDecode.onclick=decode;
buttonEncode.onclick=encode;

var canvas=document.createElement("canvas");
canvas.width=(pixel+1)*width+1;
canvas.height=(pixel+1)*height+1;
canvas.onclick=canvasOnClick;
var context=canvas.getContext("2d");
var divCanvas=document.getElementById("divCanvas");
divCanvas.appendChild(canvas);

redraw();
