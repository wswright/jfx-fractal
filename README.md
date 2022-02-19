# jfx-fractal
Java FX Fractal Generator

Uses JDK16 Records for performance, and thus requires JDK16 in order to compile and run.
## Sub-Projects:
 - ### [jfx-fractal-lib](https://github.com/wswright/jfx-fractal-lib)
   - Contains the service interface.
 - ### [jfx-fractal-equations](https://github.com/wswright/jfx-fractal-equations)
   - Contains all equations used for rendering.

## Features
 - ### Click-to-Center (left-click in order to center the viewport on the left-clicked part of the fractal)
 - ### Panning (w,a,s,d)
 - ### Zooming (scroll-wheel)
 - ### Color Shading
 - ### Resizable
 - ### Multi-Threaded - Uses both chunking and multi-threading to render quickly.
 - ### Dynamic Class Loading
   - Create your own modules providing `org.example.fractal.lib.IFractalEquation`, and JFX-Fractal will pick them up automatically. See the  [jfx-fractal-lib](https://github.com/wswright/jfx-fractal-lib) project for details.
 

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

### Contributing
Interested in contributing? Drop me a line @ cpress.scott@gmail.com, or send a pull request.

### Latest Project Screenshots
- 06/02/2021 ![Screenshot](DropDown.PNG)
- 05/22/2021 ![Screenshot](Capture2.PNG)
- 05/08/2021 ![Screenshot](Capture.PNG)

### Design Diagrams
- 02/18/2022 Class Layout ![Class Layout](img_1.png)