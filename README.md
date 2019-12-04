# atgdiagram

## What is this

Script to generate visual representation of an ATG (== Oracle Commerce these days) modules

Generator for diagram of the ATG modules. It generates source code for PlantUML post-processing using installed ATG

This is groovy script that I found when cleaning up old Mac - I am putting it here so that I can find it again.

# Using it

The generation requires installed dot tools

Mine is installed in `/opt/diagram/plantuml.jar`

See the `src/listATGModules.groovy` for documentation

# Demo

See the files in `./uml/test2.uml` - extracted from local disk - and the rendered image:

![](/uml/test2.png)


See also [my old blog](http://www.miroadamy.com/posts/2018-11-04-atg-repo-visualizer/)