[![Fabric](https://img.shields.io/badge/Mod_Loader-Fabric-blue)](https://fabricmc.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16.1-green)](https://www.minecraft.net/)

# easyexcavate

This mod is heavily inspired by, and is essentially a barebones clone of, VeinMiner or OreExcavator, built for 1.16.1 using Fabric!


Hold the Grave key (or whatever key you've set in the controls menu) when mining a block (with the correct tool) and blocks of the same type will get broken alongside it!
This does take the correct amount of durability from the used tool, as well as add the correct amount of exhaustion (plus more, by default).


You, or a server owner, can customize many aspects of the mod. Customizable aspects include the maximum blocks that can be broken at once, the maximum range the excavation can go, the bonus exhaustion multiplier, a blacklist of blocks that can be excavated (that can be inverted into a whitelist), a blacklist of tools/items that can excavate (also invertable), whether a tool is necessary, and more.


The bonus exhaustion multiplier is applied as follows:

Vanilla Exhaustion Rate * (Blocks Broken * BEM)

(the vanilla exhaustion rate is 0.005 per block broken)

As the name implies, it is applied on top of the 0.005 exhaustion per block you would get in vanilla.


Server config settings override client config settings, meaning server owners can set how everyone's excavation behaves.


By default, an excavation can mine up to 128 blocks at once, with a range of 8 and a bonus exhaustion multiplier of 0.125.


![Banner](https://lh3.googleusercontent.com/Hkw1cQWVIgAMyWelatcbalSYNVgC0N6hxBJeM6612npDJ6lj41GieBbWZry94bkbipe06R6EwkTVY84zEw=w782-h440)

The End Portal excavated lol.

## Notes

this is a fork of https://github.com/Shnupbups/easyexcavate. this creator quit development and designated [Diggus Maximus](https://www.curseforge.com/minecraft/mc-mods/diggus-maximus) as the successor.
the reason for continuing to develop this project is to study fabric moding.

##
