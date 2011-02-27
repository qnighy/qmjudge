#!/bin/sh

datadir=$1

if diff -q output.txt $datadir/sol1.txt >/dev/null; then
  exit 0
elif diff -q output.txt $datadir/sol2.txt >/dev/null; then
  exit 0
else
  exit 1
fi
