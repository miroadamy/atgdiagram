#!/bin/sh
PLANTUML_HOME=/opt/diagrams

if [ ! -e $PLANTUML_HOME ]
then
    echo There is no PLANT UML installation at \$PLANTUML_HOME=$PLANTUML_HOME
    exit 1

fi

if [ "x" == "x$1" ]
then
    echo Usage: $0 UML-FILE-TO-PROCESS
    exit 2
fi

java -Xmx1024m -jar $PLANTUML_HOME/plantuml.jar $1
