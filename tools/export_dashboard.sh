#! /bin/bash
#
# export_dashboard.sh
# Copyright (C) 2017 zhyzhu <zhyzhu@zhyzhu.abcft.lin>
#
# Distributed under terms of the MIT license.
#


EXPORT_HOST=10.12.0.30
EXPORT_PORT=27017
EXPORT_DB=apes
IMPORT_HOST=10.12.0.30
IMPORT_PORT=27017
IMPORT_DB=vitamin


export_product() {
    col=product
    echo "export ${col} ..."
    if [ $# -gt 0 ]; then
        name=$1
        mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${IMPORT_DB} -c ${col} -q "{name: /${name}/}}" -o ${col}.dat >/dev/null
    else
        mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${IMPORT_DB} -c ${col} -q "{export: {\$ne: false}}" -o ${col}.dat >/dev/null
    fi
}

export_chart() {
    col=charts
    BIDS="$1"
    UIDS="[ '5919274be30e8e43240a3339', '5a0baa40d5b21ece0b7f5022', '5a0ba9b4d5b21ece0b7f496f'  ]"
    echo "export ${col} ..."
    mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${EXPORT_DB} -c charts -q "{board_id: {\$in: ${BIDS} }}" -o charts.dat > /dev/null
    #mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${EXPORT_DB} -c ${col} -q "{vitamin: true}" -o ${col}.dat > /dev/null
    #mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${EXPORT_DB} -c charts -q "{creator_id: {\$in: ${UIDS} }, customize: { \$exists: true }}" -o charts.dat > /dev/null
}

export_dashboard() {
    col=dashboard
    OBJ_IDS="$1"
    echo "export ${col} ..."
    mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${EXPORT_DB} -c ${col} -q "{_id: {\$in: ${OBJ_IDS}}}" -o ${col}_tmpl.dat > /dev/null
}

export_dashboard_charts() {
    if [ ! -f "product.dat" ]; then
        export_product
    fi
    cmd=""
    if [ $# -gt 0 ] ; then
        cmd=$1
    fi
    obj_ids=""
    bids=""
    for bid in `jq '.board_ids' product.dat | egrep '"\w+"'`; do
        #if [ "${bid}" != "null" -a "${bid}" != "[" -a "${bid}" != "]" ]; then
            bid=$(echo ${bid} | tr -d ",")
            #echo "board: "${bid}
            obj_id="ObjectId(${bid})"
            if [ "${obj_ids}" == ""  ]; then
                bids="${bid}"
                obj_ids="${obj_id}"
            else
                bids="${bid},"${bids}
                obj_ids="${obj_id}, "${obj_ids}
            fi
        #fi
    done
    BIDS="[ "${bids}" ]"
    OBJ_IDS="[ "${obj_ids}" ]"
    if [ "$cmd" == "-c" ] ; then
        export_chart "${BIDS}"
    elif [ "$cmd" == "-b" ]; then
        export_dashboard "${OBJ_IDS}"
    else
        export_chart "${BIDS}"
        export_dashboard "${OBJ_IDS}"
    fi
}

export_chart_data() {
    origin_charts=""
    for chart_id in `cat charts.dat | jq '.origin_chart' | awk '/numberLong/{print $2}' | tr -d \"` ; do
        if [ "${origin_charts}" == ""  ] ; then
            origin_charts=${chart_id}
        else
            origin_charts=${origin_charts}","${chart_id}
        fi
    done
    origin_charts='['${origin_charts}']'
    echo ${origin_charts}
    mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${EXPORT_DB} -c charts_data -q "{chart_id: {\$in: ${origin_charts} }}" -o charts_data.dat

}

export_cols() {
    cols=( plan famous_person prediction report_tmpl free_expiry free_report config)
    for col in ${cols[@]} ; do
        echo "export ${col} ..."
        mongoexport -h ${EXPORT_HOST}:${EXPORT_PORT} -d ${IMPORT_DB} -c ${col} -o ${col}.dat > /dev/null
    done

    export_product
    export_dashboard_charts

}

if [[ $# -gt 0 ]]; then
    case $1 in
        -a|--all)
            shift 1
            export_cols ;;
        -p|--product)
            pname=$2
            shift 2
            export_product ${pname}
            ;;
        -b|--board)
            export_dashboard_charts -b ;;
        -c|--chart)
            export_dashboard_charts -c ;;
        *)
            export_cols
            ;;
    esac
else
    export_cols
fi

echo "  "
echo "transfer dat..."
tar czf /tmp/export_dat.tgz *.dat 
#mv *.dat /tmp/
scp /tmp/export_dat.tgz jumper:/mnt/share/vitamin/ && rm -f /tmp/export_dat.gtz
echo "transfer dat done"

