# PZHook

![test](https://i.imgur.com/JsdzEL8.png)

This is a tool for both end-users and developers for creating Java-based mods. The library makes use of [Interceptify](https://github.com/Olipro/Interceptify) to achieve the runtime hooking.

The basic premise of it is that you write your code, zip it up into a JAR file and throw it in a `java` folder with the rest of your mod which can then be uploaded to the Steam Workshop (or run locally from your local `Zomboid/Workshop` folder in your user directory)

There is an example project in the `Example` folder of this repository.

I am aware that there is a pre-existing project that provides for Java modding in Project Zomboid - however, the benefit this particular implementation provides is that you don't have to learn anything about the hook itself; familiarise yourself with a few annotations and you can spend your modding time focusing on the game code, not this library. Additionally, it provides users with a pain-free path to getting support for Java mods.

## End-Users

Just download the appropriate zip file for your OS, extract it, run the executable and it will take care of installing itself.

**IMPORTANT:** by using this, any mods which you choose to enable will essentially be able to execute arbitrary code on your computer. This hook implements a trust model whereby all JAR files are hashed, so if a mod gets updated, it will not be enabled when you next run the game until you have approved it. Nonetheless, any mod you approve, once it executes, can essentially do whatever it likes. Consequently, exercise your better judgement when choosing what to allow.

### Manual installation

If you know what you're doing, all that is required is the following:

1. Copy the ZomboidJavaHook `jar` file to the game folder (For the avoidance of doubt, this means putting it wherever the `zombie` folder with the java classes is present, but **don't** put it *inside* the zombie folder)
2. Edit the script you use (or the JSON files, if you don't use a script) so that the JVM receives an additional argument of `-javaagent:ZomboidJavaHook.jar` (Please note that depending on what you have downloaded, the filename of the JAR may differ, adjust the command as appropriate).
3. Edit JVM args (again, either in your launch script or JSON files) from `zombie.gameStates.MainScreenState` to `ZomboidJavaHook.Main`
4. If you're on windows, and want to continue using the EXE files to launch the game, you'll need to also patch them. If that's not something you know how to do, manual installation is not for you.

## Servers

You can pass `--help` to the PZHook JAR file for a list of options. At a minimum you will want `--server`

## Developers

After you have created your JAR file with its code, it will need to be placed inside a folder called `java` which should be right beside the `media` folder of your workshop mod.

Your library can freely import any in-game class file without causing classloader issues as all class mutations are performed prior to them being introduced into the JVM state.

Since Java Modules aren't used, you can quite freely use reflection and `setAccessible()` to get at anything that's not `public`.

Be wary of which classes (if any) that you tell PZHook to expose as public: It may result in the game getting stuck at boot if it impacts how Kahlua exposes the class. As ever, test it and find out.

### Caveats

Your Java code will be enabled even if the Lua portion isn't - you should ensure your code handles that. Similarly, a user might not enable the Java portion but then go and enable the Lua portion.

**Just to be clear: if an end-user has not downloaded and installed this hook, your java code definitely won't run.**

If another mod wants to hook the same method or constructor as you, whichever gets loaded last will be the "winner". It is **not** a goal of this library to provide cooperation - this is currently a preview and the plan is that, should the uptake be significant enough, a child library will be introduced which can also go onto the Steam workshop, to ensure proper dependency handling.

## Implementation-specific Information

The Linux and MacOS builds have not been tested to see if the installer works properly, if you can, please test it.

The JVM arguments are modified by reading the current `ProjectZombiod*.json` and appending this hook as a Java Agent, then written to `ProjectZomboid*.site.json` - you can then just delete these files to disable the hook.

### Windows

One crucial detail about Windows is that unfortunately the "pzexe" executables that launch the game don't add the JRE's library folder to the DLL search path. Consequently, loading as a Java Agent, would normally fail - however:

To get around this, the installer drops two DLLs in the game folder, one for 64-bit and one for 32-bit and both pzexes are patched to reference the appropriate one. Unsurprisingly, these shims do as you would expect; they add the JRE's library folder to the DLL search path (and also export `MessageBoxA` in case `pzexe` needs it)

If you prefer not to do this, you can of course just run the game via the `.bat` script in the game folder.

This also leads onto...

### Troubleshooting
If the game ever updates (or you run Verify Files) and you find it simply stops loading (unlikely unless they suddenly find the need to introduce a change into pzexe), simply re-run the hook from a folder outside of the game and it'll re-patch the game executables.
