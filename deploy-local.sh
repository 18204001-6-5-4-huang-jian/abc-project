#!/usr/bin/env bash
dir=$(dirname "$0")
host="10.12.0.30"
user="jenkins"
webapps=/opt/jetty/webapps
root_war="root.war"

echo "build war..."
$dir/gradlew war

echo "upload war..."
rsync -vz $dir/build/libs/apes-vitamin-1.0-SNAPSHOT.war ${user}@${host}:$webapps/${root_war}.tmp
#rsync -vz $dir/build/libs/apes-1.0-SNAPSHOT.war jetty@10.12.6.6:$webapps/root.war.tmp
ssh -T ${user}@${host} << EOF
    rm -f $webapps/${root_war}.old
    mv $webapps/${root_war} $webapps/${root_war}.old
    cp $webapps/${root_war}.tmp $webapps/${root_war}
EOF

echo "restart jetty..."
ssh ${user}@${host} /opt/jetty/bin/jetty.sh restart

echo "done"
