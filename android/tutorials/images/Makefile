# Wrapper Makefile to call "ant" in the correct way.

ROSJAVA_ROOT = "../../.."
ROSANDROID_LIBRARY = "../../library"

all: compile

$(ROSJAVA_ROOT)/java/dist/rosjava.jar:
	cd $(ROSJAVA_ROOT)/java && ant dist

clean:
	ant clean
	cd $(ROSANDROID_LIBRARY) && ant clean

# Install the app on a connected android device.
install: clean $(ROSJAVA_ROOT)/java/dist/rosjava.jar
	ant -f build-wrapper.xml install

# Build an .apk file of the app.
compile: clean $(ROSJAVA_ROOT)/java/dist/rosjava.jar
	ant -f build-wrapper.xml compile
