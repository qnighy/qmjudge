#!/bin/sh

ulimit -t `expr $2 / 1000 + 1`
ulimit -m `expr $3 \* 2`
ulimit -v `expr $3 \* 2`
ulimit -s unlimited

cd Main
/usr/bin/time -f "%U %S %M" sh -c `cat $4`"< ../input.txt > ../sol.txt"
cd ..
if diff sol.txt output.txt >/dev/null
then
  echo "Result #$1: 1"
else
  echo "Result #$1: 0"
fi
:

