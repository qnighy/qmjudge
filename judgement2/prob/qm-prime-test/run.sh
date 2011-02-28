#!/bin/bash

case $lang in
  cpp)
    runmakoto=Bin/makoto
    runmotoki=Bin/motoki
    ;;
  *) echo "No such language" >&2 ; exit 1 ;;
esac

jail-in $runmakoto <$datadir/input.txt 2>Result/stderr-makoto.txt | \
  $probdir/tool/filter-makoto 2>Result/stderr-filter-makoto.txt | \
  jail-in $runmotoki > Result/output.txt 2>Result/stderr-motoki.txt

stat=("${PIPESTATUS[@]}")

if [ "x${stat[0]}" = "x0" -a "x${stat[1]}" = "x0" -a "x${stat[2]}" = "x0" ]; then
  exit 0
else
  exit 1
fi

