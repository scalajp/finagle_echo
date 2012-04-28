#!/bin/sh
java -Xms256m -Xmx1024M -jar `dirname $0`/sbt-launch.jar "$@"
