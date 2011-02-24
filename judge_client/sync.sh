#!/bin/sh
id=ssh/id_dsa
svr=qmjudge@192.168.56.101

rsync --delete -ave "ssh -i $id" ../judgement $svr:
#ssh -i $id $svr judgement/util/judge.sh 0000 gpp

