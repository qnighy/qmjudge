#!/bin/sh

cd Main
`cat $2` < ../input.txt > ../sol.txt
cd ..

if diff sol.txt output.txt >/dev/null
then
  echo "Result #$1: 100"
else
  echo "Result #$1: 0"
fi
:

