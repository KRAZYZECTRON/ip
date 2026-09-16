# Verity project template

This is a project template for a greenfield Java project. Given below are instructions on how to use it.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/verity/Verity.java` file, right-click it, and choose `Run Verity.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:
   ```
   ██╗   ██╗███████╗██████╗ ██╗████████╗██╗   ██╗
   ██║   ██║██╔════╝██╔══██╗██║╚══██╔══╝╚██╗ ██╔╝
   ██║   ██║█████╗  ██████╔╝██║   ██║    ╚████╔╝
   ╚██╗ ██╔╝██╔══╝  ██╔══██╗██║   ██║     ╚██╔╝
    ╚████╔╝ ███████╗██║  ██║██║   ██║      ██║
     ╚═══╝  ╚══════╝╚═╝  ╚═╝╚═╝   ╚═╝      ╚═╝
   ```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Building and running a JAR file

The project uses Gradle, through the Gradle wrapper that comes with it, so Gradle need not be installed separately.

1. From the project root, build the JAR file:
   * Windows: `gradlew.bat shadowJar`
   * macOS/Linux: `./gradlew shadowJar`
1. The JAR file is created as `build/libs/verity.jar`. It holds everything the app needs, so it can be copied anywhere.
1. Copy it into an empty folder, open a terminal in that folder, and run `java -jar "verity.jar"` (Java 25 is needed).

Verity saves its tasks to `data/verity.txt` inside the folder it is started from, creating the `data` folder if needed.
