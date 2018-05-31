#!/bin/bash
#
echo "$(pwd)"

export CLASSPATH=$CLASSPATH:../lib/jgroups-3.6.4.Final.jar:./
CONFIG="-Djava.net.preferIPv4Stack=true -cp ./:../lib/jgroups-3.6.4.Final.jar"

javac -cp ../lib/jgroups-3.6.4.Final.jar *.java

TERMINAL="/usr/bin/xterm"
#TERMINAL="/usr/bin/x-terminal-emulator"

$TERMINAL -e "java $CONFIG SistemaInterface 2>/dev/null" &
$TERMINAL -e "java $CONFIG SistemaInterface 2>/dev/null" &
$TERMINAL -e "java $CONFIG SistemaInterface 2>/dev/null" &
$TERMINAL -e "java $CONFIG SistemaInterface 2>/dev/null" &


## ou manualmente com 
java $CONFIG SistemaInterface
