#!/bin/bash
source <(cat /vault/secrets/*)
java -javaagent:/opt/newrelic/newrelic.jar -jar /app.jar