#!/usr/bin/make -f

CXX = g++
CXXFLAGS = -O2 -Wall
LDFLAGS = -lm

main-program: Main.class
	echo "#!/bin/sh\njava Main" > main-program
	chmod a+x main-program

Main.class: Main.java
	javac Main.java

Main.java: Code.txt
	cp Code.txt Main.java

