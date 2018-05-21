#!/usr/bin/env bash
dir=$(dirname "$0")
host="10.12.10.10"
user=$(whoami)
webapps=/opt/eversight_ai/webapps
root_war="root.war"

echo "build war..."
$dir/gradlew war

echo "upload war..."
rsync -vz $dir/build/libs/apes-vitamin-1.0-SNAPSHOT.war ${user}@${host}:$webapps/${root_war}.tmp
ssh -T ${user}@${host} << EOF
    rm -f $webapps/${root_war}.old
    mv $webapps/${root_war} $webapps/${root_war}.old
    cp $webapps/${root_war}.tmp $webapps/${root_war}
EOF

echo "restart jetty..."
ssh ${user}@${host} sudo /opt/eversight_ai/bin/eversight restart

echo "done"
