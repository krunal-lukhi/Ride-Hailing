#!/bin/bash
# Auto-download New Relic Agent if missing
if [ ! -f "newrelic/newrelic.jar" ]; then
    echo "Downloading New Relic Java Agent..."
    curl -o newrelic/newrelic.jar https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar
fi

export $(grep -v '^#' .env | xargs)
nohup ./gradlew bootRun < /dev/null > backend_run_secure.log 2>&1 &
echo "Backend started with PID $!"
