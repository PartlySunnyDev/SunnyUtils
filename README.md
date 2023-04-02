## Spigot Base Template for creating plugins!

Currently supports **1.16.5+** (latetst versions)

### Features

- [x] Multi-version support
- [x] NMS included with mojang remappings (You may need to run BuildTools on your system to use this)
- [x] Basic GUI framework
- [x] Simple predicate system
- [x] Command helper
- [x] Builders for common objects, like ItemStack, Potion, Gear etc.

### Usage

1. Go to the repo and click on the `Use this template` button
2. This should give you some prompts to create a new repository with this repository as its template
3. Now on your local machine run `git clone https://github.com/YOUR_NAME/YOUR_REPO.git`
4. This should create a folder `YOUR_REPO`, which you can now open up in your IDE

**!! THIS PROJECT REQUIRES BUILDTOOLS TO BE RUN SEE BELOW !!**

### BuildTools

BuildTools is a jar provide by Spigot to compile CraftBukkit and NMS

1. Download the BuildTools jar from the spigot website
2. Make sure you have a java version lower than java 19, as 1.17.1 and 1.18.2 cannot be built with java 19
3. Create a directory `BuildToolsVERSION`
4. With `BuildTools.jar` inside that directory, run `java -jar BuildTools.jar --rev VERSION --remapped`
5. Wait for te process to finish
6. Now, you can start from Step 3 to build the rest of the versions. You should build 1.16.5, 1.17.1, 1.18.2, and 1.19.4