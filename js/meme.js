
const { Observable, of, from } = require('rxjs');
const { scan, reduce, count, filter } = require('rxjs/operators');
const { groupBy, mergeMap, toArray } = require('rxjs/operators');


/*
  FlatMap/mergeMap example
  https://www.youtube.com/watch?v=FcNSSTCaGsM
*/

of("this is", "a meme","on rxjs")
.pipe(
  mergeMap(str => str.split('\ '))
)
.subscribe(console.log)

/**
 * Querying the sequence
 * SELECT count(*) as count FROM mouse_db WHERE count > 1 AND x < 100 GROUP_BY time
 */
const mouseDB = [
  { x : 10, y : 10, time: 25 },
  { x : 100, y : 10, time: 26 },
  { x : 100, y : 20, time: 25 },
  { x : 10, y : 10, time: 26 },
];

const scanCount = scan((sum,item) => sum+1, 0);
const reduceCount = reduce((sum,item) => sum+1, 0)
of(1,2,3,1,1,2)
  .pipe(
    count()
  )
  .subscribe(item => console.log(item));

from(mouseDB)
  .pipe(
    filter(item => item.x < 50),
    groupBy(item => item.time),
    mergeMap(stream$ => stream$.pipe(count()))
  )
  .subscribe(console.log)
