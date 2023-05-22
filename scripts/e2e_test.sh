#! /bin/bash
nc localhost 4000 -w 10 < ./scripts/test_numbers.txt
echo "terminate" | nc localhost 4000
sleep 1
awk 'a[$0]++{print "found duplicates"; exit(1)}' numbers.log && echo "no duplicates found"
