#!/bin/bash

MARKER_FILE=/.datasource

if [ ! -f ${MARKER_FILE} ]; then

    apt-get update && apt-get install -y curl

    SERVER_START_TIMEOUT_SEC=30

    ./run.sh "${@}" &
    timeout ${SERVER_START_TIMEOUT_SEC} bash -c "until </dev/tcp/localhost/3000; do sleep 1; done"

    curl -s -H "Content-Type: application/json" \
        -XPOST http://admin:${GF_SECURITY_ADMIN_PASSWORD}@localhost:3000/api/datasources \
        -d @- <<EOF
{
    "name": "prometheus",
    "type": "prometheus",
    "access": "proxy",
    "url": "${PROMETHEUS_URL}",
    "isDefault": true
}
EOF

    pkill grafana-server
    timeout ${SERVER_START_TIMEOUT_SEC} bash -c "while </dev/tcp/localhost/3000; do sleep 1; done"

    touch ${MARKER_FILE}

    echo "Datasource successfully added!"
fi

exec ./run.sh "${@}"