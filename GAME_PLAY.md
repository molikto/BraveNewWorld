





## PRINCIPLES
* DON'T TRY TO MAKE SOMETHING REALISTIC. make some thing fun


## DOCUMENTATION


### spacial specification

the world can be seen as a 2d grid (X, Y) of "world surface"

* additionally, Z-level. discrete, even more discrete than X, Y
    * there is 3 kind of Z-level
        * optional roof level, level 2+: this is how high, thick the roof is, some roof is constructable, some is not
            * roofs, overhead mountains, things bellow has another Z-level
            * protect things bellow from molars, if thick enough
        * constructed level, level 1: this is base level + the elevation of constructions on base level
            * low mountain and walls, but not roofs, always destructible
        * base level: the level of the surface, with constructed level removed
            * normal walkable Z-level
            * level 0
                * can build things on top of it, creating a level 1
                * can build roofs on top of it, creating a level 2
            * -0.1: where shallow water flows
                * can be changed to level 0 by some machine
        * deep water level, level -1, not walkable
        * so... this means the world is ALWAYS covered with water when level < 0
    * the above is represented as
        * roof height: how high is the roof above level 1
        * optionally level 1 construction, only if when base level is not -1
        * base level: where people walks, when level is -0.1 and 0
    * we don't have a complex system how gravity works, how waterfall works, etc.

### molecular level

the world has some type of elements

water: 

## IDEAS

* animal slavery to make power
    * trait to dislike this
    
    
## aspects that don't want know
* 3d
    * the good
        * gravity
        * line of sight
    * the bad
        * hell harder to implement
        * hell harder to do user input
    * fix it:
        * stand-able block that is 1 z order higher??
            * what about walls? can you make a ladder and stand on walls?
            * what about roofs?