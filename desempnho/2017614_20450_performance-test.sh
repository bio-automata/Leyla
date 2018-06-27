#!/bin/bash

export CLASSPATH=$CLASSPATH:./lib/jgroups-3.6.4.Final.jar

if [ -z $1 ]; then 
	java -Djava.net.preferIPv4Stack=true org.jgroups.tests.perf.MPerf -props "sequencer.xml"
else
	java -Djava.net.preferIPv4Stack=true org.jgroups.tests.perf.MPerf -props $1
fi
