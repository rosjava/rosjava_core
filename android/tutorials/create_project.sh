# There's an NPE bug that makes it necessary to split this up in the case of an empty project.
android update project --target 11 --path .
android update project --target 11 --path . --library ../../library
