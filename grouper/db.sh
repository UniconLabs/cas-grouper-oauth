#!/bin/sh

DIR=`dirname $0`

case "$1" in
	start)
		echo "starting"
		if [ -e "${DIR}/db.pid" ]
		then
			echo "already running."
			exit 1
		fi
		java -cp ${DIR}/grouper.apiBinary-2.1.5/lib/jdbcSamples/hsqldb.jar org.hsqldb.Server -database.0 file:${HOME}/Applications/cas-grouper/grouper/grouper.apiBinary-2.1.5/grouper -dbname.0 grouper -port 9001 &
		echo $! >> "${DIR}/db.pid"
	;;

	stop)
		echo "stopping"
		if [ -e "${DIR}/db.pid" ]
		then
			kill `cat ${DIR}/db.pid`
			rm ${DIR}/db.pid
		fi
	;;

	*)
		echo "Usage: startdb.sh {start|stop}"
		exit 1
esac

exit 0
