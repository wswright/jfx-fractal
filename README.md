# jfx-fractal
Java FX Fractal Generator

Uses JDK16 Records.

## Features
 - ### Click-to-Center (left-click in order to center the viewport on the left-clicked part of the fractal)
 - ### Panning (w,a,s,d)
 - ### Zooming (scroll-wheel)
 - ### Color Shading
 - ### Resizable
 - ### Multi-Threaded

The names of the fractal classes are really terribly wrong. I aim to clean that up soon. I'm just playing around for now.

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

### Latest Project Screenshots
- 05/22/2021 ![Screenshot](Capture2.PNG)
- 05/08/2021 ![Screenshot](Capture.PNG)