# Quick and dirty script to generate test files to be used with nc 
# example file usage: nc localhost 4000 < test_numbers.txt
from random import randrange

with open('test_numbers.txt', 'w') as f:
    for i in range(1000000):
        num = randrange(1000000000)
        str_num = str(num)
        zeroes = '0' * (9 - len(str_num))
        f.write(zeroes + str_num + "\n")
