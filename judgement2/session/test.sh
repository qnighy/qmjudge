#!/bin/sh
rm cppsample-1/Bin -r
rm cppsample-1/Result -r
rm cppsample-2/Bin -r
rm cppsample-2/Result -r
rm javasample-1/Bin -r
rm javasample-1/Result -r

../util/qmjutil cppsample-1 qm-aplusb cpp build-program
for i in 0 1 2
do
  ../util/qmjutil cppsample-1 qm-aplusb cpp judge-once $i
done

../util/qmjutil cppsample-2 qm-prime-test cpp build-program
for i in 0 1 2 3 4 5 6 7 8
do
  ../util/qmjutil cppsample-2 qm-prime-test cpp judge-once $i
done

../util/qmjutil javasample-1 qm-aplusb java build-program
for i in 0 1 2
do
  ../util/qmjutil javasample-1 qm-aplusb java judge-once $i
done

