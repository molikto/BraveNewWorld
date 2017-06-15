



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
