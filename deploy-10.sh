#!/usr/bin/env bash
dir=$(dirname "$0")
host="10.15.255.10"
user=$(whoami)
webapps=/opt/eversight_ai/webapps
root_war="root.war"
tmp_file=/tmp/${root_war}.eversight

echo "build war..."
$dir/gradlew war

echo "upload war..."
scp  $dir/build/libs/apes-vitamin-1.0-SNAPSHOT.war ${user}@${host}:${tmp_file}
#下面命令执行报错
#rsync -vz $dir/build/libs/apes-vitamin-1.0-SNAPSHOT.war ${user}@${host}:$webapps/${root_war}.tmp
ssh -T ${user}@${host} << EOF
sudo rm -f ${webapps}/${root_war}.old
sudo mv ${webapps}/${root_war} ${webapps}/${root_war}.old
sudo cp ${tmp_file} ${webapps}/${root_war}
EOF

echo "restart jetty..."
ssh ${user}@${host} sudo /opt/eversight_ai/bin/eversight restart

echo "done"
