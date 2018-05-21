#!/usr/bin/env bash
dir=$(dirname "$0")
host="47.52.61.139"       #abc-data-service
user="jenkins"
webapps=/niub/bj/app/jetty/webapps
webroot=/niub/bj/app
webname=webapp
root_war="root.war"

deploy_rest_app() {
echo "build war..."
$dir/gradlew onlineDevWar

echo "upload war..."
rsync -vz $dir/build/libs/apes-vitamin-online-dev-1.0-SNAPSHOT.war ${user}@${host}:$webapps/${root_war}.tmp
#rsync -vz $dir/build/libs/apes-1.0-SNAPSHOT.war jetty@10.12.6.6:$webapps/root.war.tmp
ssh -T ${user}@${host} << EOF
    rm -f $webapps/${root_war}.old
    mv $webapps/${root_war} $webapps/${root_war}.old
    cp $webapps/${root_war}.tmp $webapps/${root_war}
EOF

echo "restart jetty..."
ssh ${user}@${host} sudo service jetty restart
echo "done"
}

deploy_web() {
echo "upload web..."
# cd ${dir}; rsync -rz src/main/${webname} ${user}@${host}:${webroot}/${webname}
cd ${dir}; rm -rf ${webname}.tar; tar cf ${webname}.tar -C src/main ${webname}; rsync -vz ${webname}.tar ${user}@${host}:${webroot}/${webname}.tar
ssh -T ${user}@${host} << EOF
    rm -rf ${webroot}/${webname}.old
    mv ${webroot}/${webname} ${webroot}/${webname}.old
    cd ${webroot} ; tar xf ${webname}.tar
EOF

}

if [ $# -gt 0 ] ; then
    case $1 in
        -r|--rest)
            deploy_rest_app
            ;;
        -w|--web)
            deploy_web
            ;;
        *)
            ;;
    esac
else
    deploy_web
    deploy_rest_app
fi
