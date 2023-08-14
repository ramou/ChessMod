= My First Crappy Mod =


The biggest fix I need to do often is run this:
```
.\gradlew genEclipseRuns
```
in the workspace directory.

To get a final build, use
```
.\gradlew build
```

The `.jar` that isn't a .sources can be stuck in a mod folder.

Here are some of the resources I've been using:
 * https://cadiboo.github.io/tutorials/1.15.2/forge/
 * https://mcforge.readthedocs.io/en/1.15.x/
 * https://wiki.mcjty.eu/modding/index.php?title=Tut15_Ep14
 * http://greyminecraftcoder.blogspot.com/p/list-of-topics-1144.html
 * https://minecraft.gamepedia.com/Tutorials/Creating_a_resource_pack
 * http://greyminecraftcoder.blogspot.com/2014/12/block-models-texturing-quads-faces.html
 * https://bedrockminer.jimdofree.com/modding-tutorials/advanced-modding/tile-entity-special-renderer/
 * https://www.minecraftforge.net/forum/topic/68473-1122-drawing-cube-through-tileentityspecialrenderer/?do=findComment&comment=330870
 * https://forum.islandearth.net/d/10-forge-modding-tutorial-1-14-creating-custom-guis-1-3
 * https://github.com/TheGreyGhost/MinecraftByExample/tree/master/src/main/java/minecraftbyexample/mbe20_tileentity_data
 * https://github.com/TheGreyGhost/MinecraftByExample/blob/master/src/main/java/minecraftbyexample/mbe20_tileentity_data/TileEntityData.java
 * https://github.com/Vazkii/Botania
 * https://www.opengl.org/archives/resources/features/KilgardTechniques/oglpitfall/
 * http://greyminecraftcoder.blogspot.com/2013/08/the-tessellator.html
 
Here are resources around Gradle usage in this context
 * https://forgegradle.readthedocs.io/en/latest/reference/tasks/
 
Here are resources I've considered looking at eventually:
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/resource-packs/1226039-must-read-title-formatting-tutorials-and-faq
 * http://www.songho.ca/opengl/gl_sphere.html
 * https://wiki.mcjty.eu/modding/index.php?title=Render_Block_Baked_Model-1.12
 
 

== Architecture ==
Everything revolves around the MODID. Everything has to refer back to it, directory structures for the assets rely on it. I should write more about this here at some point.


=== Blocks ==
Blocks are either generated, built or are generated. I don't have good words for the last two, but one is when a player produces something from a recipe, the other is when a block generates them. That difference is just that recipes seem to be encoded to do that in a fancy way. Right now, I'll focus on the recipe system, though.

== Recipes ==
Some usefull pages:
 * https://mcforge.readthedocs.io/en/latest/utilities/recipes/
 * https://minecraft.gamepedia.com/Tag#Item_Tags

== Forge ==
Use the right version of forge! I really must change my structure so it doesn't look like I'm tied to a specific version... but I really kinda am. It should work when recompiled in later Forge until they change something.

=== building jar ===
I had to manually set my `JAVA_HOME` to a JDK since it was defaulting to the JRE. I don't know how to make it pull this from eclipse like seems sensible, so I did:
```
$Env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_251"

```

== Chess ==
And of course, what I'm basing the actual chess off:
 * https://github.com/pippinbarr/chesses
 * https://github.com/jhlywa/chess.js
 * https://github.com/dgurkaynak/chess-ai

== Licensing ==
 * I grabbed my 2D chess pieces from: https://commons.wikimedia.org/wiki/Category:PNG_chess_pieces/Standard_transparent 
 *  under the commons license!


