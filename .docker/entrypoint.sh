#!/bin/bash
source <(cat /vault/secrets/*)
java -javaagent:/opt/javaagent.jar -jar /app.jar