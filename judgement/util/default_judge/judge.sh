#!/bin/sh

./runbox.sh Main < input.txt > sol.txt

if diff sol.txt output.txt >/dev/null
then
  echo "Result #$1: 100"
else
  echo "Result #$1: 0"
fi
:

