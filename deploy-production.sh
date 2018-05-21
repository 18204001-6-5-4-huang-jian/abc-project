#!/usr/bin/env bash
dir=$(dirname "$0")
host="47.90.122.238"       #abc-data-service
user="root"
webapps=/niub/bj/app/jetty/webapps
root_war="root.war"

echo "build war..."
$dir/gradlew releaseWar

echo "upload war..."
rsync -vz $dir/build/libs/apes-vitamin-release-1.0-SNAPSHOT.war ${user}@${host}:$webapps/${root_war}.tmp
ssh -T ${user}@${host} << EOF
    rm -f $webapps/${root_war}.old
    mv $webapps/${root_war} $webapps/${root_war}.old
    cp $webapps/${root_war}.tmp $webapps/${root_war}
EOF

echo "restart jetty..."
ssh ${user}@${host} sudo service jetty restart

echo "done"
