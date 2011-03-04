#!/bin/sh

case $lang in
  cpp)
    g++ -o Bin/makoto Src/Makoto.cpp $probdir/library/cpp/makoto_lib.cpp -I$probdir/library/cpp/
    g++ -o Bin/motoki Src/Motoki.cpp $probdir/library/cpp/motoki_lib.cpp -I$probdir/library/cpp/
    ;;
  *) echo "No such language" >&2 ; exit 1 ;;
esac

