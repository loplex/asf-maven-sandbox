#!/bin/sh

export JAVA_HOME=/opt/java/sdk/current                                                                                                                          
export JAVA_CMD=$JAVA_HOME/bin/java                                                                                                                             
export JAVA_OPTS="-ms32m -mx256m"                                                                                                                               
export JAVA="$JAVA_CMD $JAVA_OPTS"           

(

$JAVA -jar swizzle-jirareport-1.2.1-SNAPSHOT-dep.jar maven-plugins.vm

) 2>&1 | tee $HOME/repository-staging/to-ibiblio/reports/plugins/plugin-issues.txt
