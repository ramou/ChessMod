= My First Crappy Mod =


The biggest fix I need to do often is run this:
```
.\gradlew genEclipseRuns
```
in the workspace directory.

Here are some of the resources I've been using:
 * https://github.com/ramou/FirstMod/tree/master/forge-1.14.4-28.2.0-mdk
 * http://greyminecraftcoder.blogspot.com/p/list-of-topics-1144.html
 * https://minecraft.gamepedia.com/Tutorials/Creating_a_resource_pack
 * https://mcforge.readthedocs.io/en/1.14.x/
 * https://cadiboo.github.io/tutorials/1.14.4/forge/
 * http://greyminecraftcoder.blogspot.com/2014/12/block-models-texturing-quads-faces.html
 * https://bedrockminer.jimdofree.com/modding-tutorials/advanced-modding/tile-entity-special-renderer/
 
Here are resources I've considered looking at eventually:
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/resource-packs/1226039-must-read-title-formatting-tutorials-and-faq
 * http://www.songho.ca/opengl/gl_sphere.html
 
 
== Architecture ==
Everything revolves around the MODID. Everything has to refer back to it, directory structures for the assets rely on it. I should write more about this here at some point.


=== Blocks ==
Blocks are either generated, built or are generated. I don't have good words for the last two, but one is when a player produces something from a recipe, the other is when a block generates them. That difference is just that recipes seem to be encoded to do that in a fancy way. Right now, I'll focus on the recipe system, though.

