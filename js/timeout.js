
let counter = 0;
function hello() {
	counter++;
	setTimeout(hello, 2000);

	if(counter > 2) throw('fuck off');
	console.log("Hello");
	//setTimeout(hello, 2000);
}

setTimeout(hello, 2000);
