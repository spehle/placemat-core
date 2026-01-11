CC=gcc
CFLAGS=-O2 -Wall -Wextra -pedantic
LDLIBS=$(shell pkg-config --libs ncurses libcurl jansson)
CPPFLAGS=$(shell pkg-config --cflags ncurses libcurl jansson)

all: placemat-tui

placemat-tui: main.o
	$(CC) $(CFLAGS) -o $@ $^ $(LDLIBS)

main.o: main.c
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $<

clean:
	rm -f placemat-tui *.o
