function Func(color, ...bases) {
	this.color=color;
	this.bases=bases;
	this.functions=[];
	this.params=[];
	for (var ii=0; bases.length>ii; ++ii) {
		this.functions.push(bases[ii].func);
		this.params.push(String.fromCharCode(97+ii));
	}
}

Func.prototype.fitter=function(sample) {
	var ss=regression(this.functions, sample);
	var result={params: {}};
	for (var ii=0; ss.length>ii; ++ii) {
		result.params[this.params[ii]]=ss[ss.length-1-ii][0];
	}
	var func=this;
	result.ff=function(xx) {
		var result=[];
		for (var ii=func.bases.length-1; 0<=ii; --ii) {
			result.push(ss[ii]*func.bases[ii].func(xx));
		}
		return sum(result);
	};
	return result;
};

function FuncBase(name, func) {
	this.name=name;
	this.func=func;
}

var funcBases={};
funcBases.one=new FuncBase("1",
		function(xx){return 1.0;});
funcBases.x=new FuncBase("x",
		function(xx){return xx;});
funcBases.x2=new FuncBase("x<sup>2</sup>",
		function(xx){return xx*xx;});
funcBases.x3=new FuncBase("x<sup>3</sup>",
		function(xx){return xx*xx*xx;});
funcBases.x4=new FuncBase("x<sup>4</sup>",
		function(xx){var x2=xx*xx; return x2*x2;});
funcBases.x5=new FuncBase("x<sup>5</sup>",
		function(xx){var x2=xx*xx; return x2*x2*xx;});
funcBases.x6=new FuncBase("x<sup>6</sup>",
		function(xx){var x2=xx*xx; var x3=x2*xx; return x3*x3;});
funcBases.xlnlnx=new FuncBase("x*lnlnx",
		function(xx){return xx*Math.log(Math.log(xx));});
funcBases.xlnx=new FuncBase("x*lnx",
		function(xx){return xx*Math.log(xx);});
funcBases.xlnxlnlnx=new FuncBase("x*lnx*lnlnx",
		function(xx){var lnx=Math.log(xx); return xx*lnx*Math.log(lnx);});
funcBases.xln2x=new FuncBase("x*ln<sup>2</sup>x",
		function(xx){var lnx=Math.log(xx); return xx*lnx*lnx;});
funcBases.lnx=new FuncBase("lnx",
		function(xx){return Math.log(xx);});
funcBases.lnlnx=new FuncBase("lnlnx",
		function(xx){return Math.log(Math.log(xx));});

var funcs={};
funcs.x=new Func("Red",
		funcBases.one, funcBases.x);
funcs.x2=new Func("Lime",
		funcBases.one, funcBases.x, funcBases.x2);
funcs.x3=new Func("Blue",
		funcBases.one, funcBases.x, funcBases.x2, funcBases.x3);
funcs.x4=new Func("Fuchsia",
		funcBases.one, funcBases.x, funcBases.x2, funcBases.x3, funcBases.x4);
funcs.xlnlnx=new Func("Purple",
		funcBases.one, funcBases.xlnlnx);
funcs.xlnx=new Func("Orange",
		funcBases.one, funcBases.xlnx);
funcs.xlnxlnlnx=new Func("Yellow",
		funcBases.one, funcBases.xlnxlnlnx);
funcs.xln2x=new Func("Aqua",
		funcBases.one, funcBases.xln2x);

//funcs.x5=new Func("Fuchsia",
//		funcBases.one, funcBases.x, funcBases.x2, funcBases.x3, funcBases.x4, funcBases.x5);
//funcs.x6=new Func("Fuchsia",
//		funcBases.one, funcBases.x, funcBases.x2, funcBases.x3, funcBases.x4, funcBases.x5, funcBases.x6);
//funcs.allBases=new Func("Fuchsia",
//		funcBases.one,
//		funcBases.x,
//		funcBases.x2,
//		funcBases.x3,
//		funcBases.x4,
//		funcBases.x5,
//		funcBases.x6,
//		funcBases.lnx,
//		funcBases.lnlnx,
//		funcBases.xlnx,
//		funcBases.xlnxlnlnx,
//		funcBases.xlnlnx,
//		funcBases.xln2x);

var allParams=[];
for (var key in funcs) {
	if (allParams.length<funcs[key].params.length) {
		allParams=funcs[key].params;
	}
}
