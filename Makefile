PACKAGE_NAME=$(shell basename $(PWD))

# currently just pass-through to Ant
all:
	ant dist

clean:
	ant clean

test: all
	ant test
