



## multi-player networking: lockstep client-server


### time syncing

assuming client/server has a local time clock,
that is, they have a time, not necessary the world time,
but some time, and assume this time goes uniformly in c/s.

client ask server, what's current time.
server reply time.
client get time.
so client knows the difference of client/server time


### lockstep

we assume that we have a way to setup c/s to start the game at same time.
then clients time is divided into steps,
for example, client simulation is run at 10 step per second,
each step client do:

* collect user inputs
* send to server with current step
* update game state with 
* render current game state


### use "raw input" or?


see the game loop, in `tickGameAndTickMaybePauseNetwork` we are processing commands in game simulation, but the commands are actually commands gathered 100ms before, and they are sent over the network and confirmed and received now. The problem is, we are gathering user commands first.

their are two ways of doing this:

1. the raw input, like user clicked on game world position (x, y), or dragged a circle on world position (x, y)
2. the game orders: like  "mine this tile" first, then let the command to be sent over the network, then after it is confirmed, it is executed in the simulation

the problem is the second one can potentially send a a lot of data, for example, if the player selected the half the map for killing, then we need to send all the id of the animals in selection

why not use 1 then? the problem is, what user actually want might not be what actually get executed. for example if two animal is walking real fast, then if the user click to select one, when the command is sent over the network and back, the game might already advanced the simulation so that it is a different animal standing there, so the wrong animal is selected.


maybe a difference of 100ms is not a problem, but this might be a problem is the game can be in fast-speed mode (like 3x in RimWorld)

what's your opinion?