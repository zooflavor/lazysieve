CC=gcc
#CC=clang
FLAGS=-std=c99 -pedantic -Wall -Werror -march=native -O3

build: clean init.bin generator.bin

%.bin: %.c
	${CC} -o $@ $< ${FLAGS}

clean:
	rm -f *.bin
