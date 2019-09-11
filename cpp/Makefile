# Makefile

CXXFLAGS = -g -I/usr/include/jsoncpp/ -Wall -Wextra -Werror -Wno-error=deprecated-declarations -Wno-error=unused-variable -Wno-error=unused-parameter -std=c++11
LDFLAGS = -lcurl -ljsoncpp -lcrypto `pkg-config --libs opencv`

main: mask.o main.o
	g++  mask.o main.o  $(LDFLAGS) -o main

main.o: main.cpp
	g++  $(CXXFLAGS) -c main.cpp -o main.o

mask.o: mask_utils/maskApi.c
	gcc -g mask_utils/maskApi.c -c -o mask.o

.PHONY: all
all: main

.PHONY: run
run: main
	./main

.PHONY: clean
clean:
	rm -f main.o
	rm -f mask.o
	rm -f main

.PHONY: remake
remake: clean main
