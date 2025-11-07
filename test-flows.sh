#!/usr/bin/env bash
set -e
JAR=target/queuectl.jar

echo "Enqueue success job"
java -jar $JAR enqueue '{"id":"job-success","command":"echo hello","maxRetries":2}'

echo "Enqueue failing job"
java -jar $JAR enqueue '{"id":"job-fail","command":"/bin/false","maxRetries":2}'

# Start worker in background
java -jar $JAR worker start --count 2 &
PID=$!

echo "Waiting 15s for jobs to be processed..."
sleep 15

java -jar $JAR status

# stop workers
java -jar $JAR worker stop || true
kill $PID || true

java -jar $JAR dlq list
