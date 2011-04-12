# Wrapper Makefile to call "ant" in the correct way.

all: compile

../../java/dist/rosjava.jar:
	cd ../../java && ant dist

clean:
	ant clean
	cd ../../library/ && ant clean

# Install the app on a connected android device.
install: clean ../../java/dist/rosjava.jar
	ant -f build-wrapper.xml install

# Build an .apk file of the app.
compile: clean ../../java/dist/rosjava.jar
	ant -f build-wrapper.xml compile
