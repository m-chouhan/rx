
const Rx = require("rxjs/Rx");
const { range, fromEvent, interval } = require('rxjs');
const { map, filter, toArray, throttle } = require('rxjs/operators');
const { webSocket } = require("rxjs/webSocket");

const GAME_SPEED = 2;
const FPS = 20;
const STAR_COUNT = 200;
const URL = 'ws://localhost:8080';

//**** canvas setup */
var canvas = document.createElement('canvas');
var ctx = canvas.getContext("2d");
document.body.appendChild(canvas);
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

////////////main logic starts here !!! :P///////
const player$ = Rx.Observable
	.fromEvent(canvas, "mousemove")
	.map((event) => ({ x: event.clientX, y: 100 }));

const starsArray = Rx.Observable.range(1, STAR_COUNT)
	.map(() => ({
		x: parseInt(Math.random() * canvas.width),
		y: parseInt(Math.random() * canvas.height),
		size: parseInt(Math.random() * 3 + 1)
	}))
	.toArray();

const stars$ = starsArray
	.flatMap(starArray =>
		Rx.Observable.interval(GAME_SPEED)
			.map(() => {
				starArray.forEach(star => {
					if (star.y == 0)
						star.y = canvas.height;
					else star.y--;
				});
				return starArray;
			})
	);

//**** setup ws ***//
// const ws$ = webSocket(URL);
// const game$ = ws$
// 	.filter(data => data.type == 'data')
// 	.map(data => data.playerStates);

// ws$.subscribe(data => {
// 	if(data.type == 'info')
// 		console.log(data);
//});
//player$.subscribe(player => ws$.next(player));
const game$ = player$.map(item => [item]);

function render({ playerStates, stars }) {
	ctx.fillStyle = '#000000';
	ctx.fillRect(0, 0, canvas.width, canvas.height);
	ctx.fillStyle = '#ffffff';
	stars.forEach(star => ctx.fillRect(star.x, star.y, star.size, star.size));
	playerStates.forEach(player => renderPlayer(player));
}

function renderPlayer(player) {
	ctx.beginPath();
	// ctx.moveTo(75, 50);
	// ctx.lineTo(100, 75);
	// ctx.lineTo(100, 25);
	ctx.arc(player.x, player.y, 50, 0, Math.PI * 2, true);
	ctx.fill();
}

const localGame$ = Rx.Observable.combineLatest(game$, stars$, (playerStates, stars) => ({ playerStates, stars }));
localGame$
	.throttle(item => interval(FPS))
	.subscribe(state => render(state));
