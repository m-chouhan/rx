function haiku() {
    var adjs = ["autumn", "hidden", "bitter", "misty", "silent", "empty", "dry",
    "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring",
    "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered",
    "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green",
    "long", "late", "lingering", "bold", "little", "morning", "muddy", "old",
    "red", "rough", "still", "small", "sparkling", "throbbing", "shy",
    "wandering", "withered", "wild", "black", "young", "holy", "solitary",
    "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine",
    "polished", "ancient", "purple", "lively", "nameless"]
    , nouns = ["waterfall", "river", "breeze", "moon", "rain", "wind", "sea",
    "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn",
    "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird",
    "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower",
    "firefly", "feather", "grass", "haze", "mountain", "night", "pond",
    "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf",
    "thunder", "violet", "water", "wildflower", "wave", "water", "resonance",
    "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper",
    "frog", "smoke", "star"];
    return adjs[Math.floor(Math.random()*(adjs.length-1))]+"_"+nouns[Math.floor(Math.random()*(nouns.length-1))];
}


/*********** START ********/
const WebSocket = require('ws');
const Rx = require("rxjs/Rx");

const playerInfo = [];
const playerStates = [];

const game$ = new Rx.Subject();
game$.subscribe(event => {
    playerStates[event.id] = {x : event.x, y: event.y};
    const data = JSON.stringify({ type : 'data', playerStates });
    playerInfo.forEach(player => player.channel.send(data));
});

const newConnection$ = Rx.Observable.create(observer => {
    const wss = new WebSocket.Server({ port: 8080 });
    wss.on('connection', ws => {
        const pInfo = { id : playerInfo.length, name : haiku(), channel : ws };
        ws.on('message', data => {
            console.log(`Received message => ${JSON.stringify(data)}, now broadcasting for everyone!!`);
            game$.next({id : pInfo.id, ...JSON.parse(data)});
        });
        ws.send(JSON.stringify(
            {type: 'info', name : pInfo.name ,id: pInfo.id }
        ));

        const data = JSON.stringify({type: 'info', message: `${pInfo.name} joined with id: ${pInfo.id}`})
        playerInfo.forEach(player => player.channel.send(data));
        playerInfo.push(pInfo);
        playerStates.push({x : 0, y: 0});
    });
});