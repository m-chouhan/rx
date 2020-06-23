import { of, fromEvent, range,interval, combineLatest } from 'rxjs'; 
import { map, toArray, mergeMap, scan, throttle, startWith, merge } from 'rxjs/operators';

const BACKGROUND_SPEED = 10;
const FPS = 20;
const STAR_COUNT = 200;

//**** canvas setup */
var canvas = document.createElement('canvas');
var ctx = canvas.getContext("2d");
document.body.appendChild(canvas);
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

const player$ = fromEvent(canvas, "mousemove").pipe(
    startWith({clientX : 160}),
    map((event) => ({ x: event.clientX, y: 600 }))
);
//.subscribe(console.log)
const missile$ =  fromEvent(canvas, "click").pipe(
  scan((missileArray,event) => {
    missileArray.push({ x : event.clientX, y : 600 });
    return missileArray;
  }, [])
)
//.subscribe(console.log);

const starsArray = range(1, STAR_COUNT).pipe(
    map(() => ({
      x: parseInt(Math.random() * canvas.width),
      y: parseInt(Math.random() * canvas.height),
      size: parseInt(Math.random() * 3 + 1)
    })),
    toArray()
);

const stars$ = starsArray.pipe(
    mergeMap(starArray => 
      interval(BACKGROUND_SPEED).pipe(
          map(() => {
            starArray.forEach(star => {
              if (star.y == 0)
                star.y = canvas.height;
              else star.y--;
            });
            return starArray;
          }))
    )
);

function renderPlayer(player) {
	ctx.beginPath();
	ctx.arc(player.x, player.y, 10, 0, Math.PI * 2, true);
	ctx.fill();
}

function renderStars(stars) {
  ctx.fillStyle = '#000000';
	ctx.fillRect(0, 0, canvas.width, canvas.height);
	ctx.fillStyle = '#ffffff';
	stars.forEach(star => ctx.fillRect(star.x, star.y, star.size, star.size));
}

function renderMissiles(missiles) {
  missiles.forEach(function(missile) {
    missile.y -= 10;
    renderPlayer(missile);
  });
}

function render({ player, stars, missiles }) {
  renderStars(stars);
  renderMissiles(missiles)
	renderPlayer(player);
}

combineLatest(player$, stars$, missile$, (player, stars, missiles) => ({ player, stars,missiles })).pipe(
	throttle(item => interval(FPS))
)
.subscribe(state => render(state));
