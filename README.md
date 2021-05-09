# jfx-fractal
Java FX Fractal Generator

### Project JVM Flags
 - See https://github.com/controlsfx/controlsfx/wiki/Using-ControlsFX-with-JDK-9-and-above
   
In JDK 9 and later, in order to access any private field from a class, we need to open its package to the module trying to access it. In our case, package is javafx.scene.control.skin and the module trying to access it is org.controlsfx.controls. Adding the following as a JVM flag to the project should fix the exception:
 - `--add-opens=javafx.controls/javafx.scene.control.skin=org.controlsfx.controls`

Module "org.controlsfx.controls" is replaced with "unnamed module @0x6ea6fae3". JDK refers everything on classpath to ALL-UNNAMED module. Therefore, to fix the exception we need to open the package to module "ALL-UNNAMED".
 - `--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED`

### Project Genesis
The following Maven command was used to generate the initial structure of this project:
 - `mvn archetype:generate \
-DarchetypeGroupId=org.openjfx \
-DarchetypeArtifactId=javafx-archetype-simple \
-DarchetypeVersion=0.0.3 \
-DgroupId=org.openjfx \
-DartifactId=sample \
-Dversion=1.0.0 \
-Djavafx-version=15.0.1`

### Latest Project Screenshot
- 02/19/2021 ![Screenshot](Capture.png) 
Log Output for this image:
  
- `Initializing...[W: 3849][H: 2123][X: -2.267574-0.957420][Y: -0.907029-0.907029][352ms]`
- `Calculating...[Threads: 64][Escape: 123456789][MaxIterations: 4096][7632ms]`
- `Calculating Histogram...`
    - `Zero HashMap: 592ms`
    - `Populate HashMap: 639ms`
    - `Generate & Store Colors: 1ms`
    - `Assign Colors to pixels: 181ms`
    - `TOTAL TIME: 1414ms`
    - `[Entries: 1998][1414ms]`
- `Buffering...[290ms]`
- `Rendering...[16ms]`
- `Total Iterations: 6673684636`
- `Iters/Pixel: 816.7098`
- `TOTAL DURATION: [9707ms]`
- `+------------------------------------------------------------------------------------------+`