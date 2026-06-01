#!/bin/sh
set -e

if [ -n "${DATABASE_URL}" ]; then
  uri="${DATABASE_URL#*://}"

  PGHOST="${uri#*@}"
  PGHOST="${PGHOST%%:*}"

  hp="${uri#*@}"
  PGPORT="${hp#*:}"
  PGPORT="${PGPORT%%/*}"

  PGDATABASE="${hp#*/}"
  PGDATABASE="${PGDATABASE%%\?*}"

  userpass="${uri%%@*}"
  PGUSER="${userpass%%:*}"
  PGPASSWORD="${userpass#*:}"

  export PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD
fi

exec java -jar app.jar
