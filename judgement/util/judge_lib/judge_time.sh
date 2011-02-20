#!/bin/sh

ulimit -t `expr $1 / 1000 + 1`
ulimit -m `expr $2 \* 2`
ulimit -v `expr $2 \* 2`
ulimit -s unlimited

/usr/bin/time -f "TimeResult #$3: %U %S %M" -o time.txt Judge/judge.sh "$3"

cat time.txt
:

