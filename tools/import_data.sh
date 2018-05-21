#! /bin/bash
#
# export_dashboard.sh
# Copyright (C) 2017 zhyzhu <zhyzhu@zhyzhu.abcft.lin>
#
# Distributed under terms of the MIT license.
#


IMPORT_HOST=10.12.0.30
IMPORT_PORT=27017
IMPORT_DB=vitamin

COLS=( dashboard_tmpl charts )

trans_charts_ip() {
    chart='charts.dat'
    [ -f ${chart} ] || return
    P="http://10.12.[0-9]+.[0-9]+"
    API_IP="http://10.12.0.30"
    sed -i -r s,${P},${API_IP},g ${chart}
    echo "trans ${chart} ip done"
}


import_cols() {
    for COL in ${COLS[@]} ; do
        echo "import ${COL}..."
        mongoimport -h ${IMPORT_HOST}:${IMPORT_PORT} -d ${IMPORT_DB} -c ${COL} --upsert ${COL}.dat > /dev/null
    done
}

#trans_charts_ip
import_cols
