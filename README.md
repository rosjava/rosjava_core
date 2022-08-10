rosjava is the first pure Java implementation of ROS.

From [ROS.org](http://www.ros.org/wiki/): ROS is an open-source, meta-operating
system for your robot. It provides the services you would expect from an
operating system, including hardware abstraction, low-level device control,
implementation of commonly-used functionality, message-passing between
processes, and package management.

Developed at Google in cooperation with Willow Garage, rosjava enables
integration of Android and ROS compatible robots. This project is under active
development and currently alpha quality software. Please report bugs and feature
requests on the [issues
list](https://github.com/rosjava/rosjava/issues?state=open).

To get started, visit the
[rosjava_core](https://rosjava.github.io/rosjava_core/latest/) and
[android_core](https://rosjava.github.io/android_core/latest) pages for
documentation concerning the core libraries and examples. Also visit the roswiki
[rosjava](http://wiki.ros.org/rosjava) and
[android](http://wiki.ros.org/android) pages for more general rosjava-android
information.

Still have questions? Check out the ros-users [discussion
list](https://discourse.ros.org/c/rosjava), post questions to [ROS
Answers](http://answers.ros.org/questions/) with the tag "rosjava," or join #ROS
on irc.oftc.net.

rosjava was announced publicly during the [Cloud Robotics tech talk at Google
I/O 2011](http://www.youtube.com/watch?feature=player_embedded&v=FxXBUp-4800).

Looking for a robot platform to experiment with ROS, Android, and cloud
robotics? The [OSRF](http://www.osrfoundation.org/)
[TurtleBot](http://wiki.ros.org/Robots/TurtleBot) is a great mobile perception
platform for [getting started with robotics
development](http://www.youtube.com/watch?feature=player_embedded&v=MOEjL8JDvd0).

### Branches ###

The master branch reflects the latest version of rosjava. All development
happens on the master branch in the form of pull requests from developers.
Unless you are developing rosjava itself, you should _not_ use the master
branch.

Named branches are created whenever a new version of ROS is released. These
branches are considered stable. No new features will be added to these branches,
however, bug fixes may be cherry picked from master.

### Pull Requests ###

You must sign a Contributor License Agreement (CLA) before we can accept any
code. The CLA protects you and us.

* If you are an individual writing original source code and you're sure you own
  the intellectual property, then you'll need to sign an [individual
  CLA](https://developers.google.com/open-source/cla/individual).
* If you work for a company that wants to allow you to contribute your work to
  SL4A, then you'll need to sign a [corporate
  CLA](https://developers.google.com/open-source/cla/corporate).

Follow either of the two links above to access the appropriate CLA and
instructions for how to sign and return it. Damon will respond on either github
or email to confirm.

### Building with Bazel ###

To build this project with Bazel, simply run:

```
bazel build //...
```

To depend on `rosjava_core` from another project, you'll need to use
[bazel-deps](https://github.com/johnynek/bazel-deps).

1. Start by copying (or merging) [dependencies.yaml][dependencies.yaml] in to
   your project.
1. Follow the instructions in that file to generate the BUILD files inside your
   project.
1. Add the following lines to your WORKSPACE file:

```
load("//3rdparty:workspace.bzl", "maven_dependencies")

maven_dependencies()

git_repository(
    name = "com_github_rosjava_rosjava_core",
    commit = "{insert commit SHA for HEAD}",
    remote = "https://github.com/rosjava/rosjava_core.git",
)

load("@com_github_rosjava_rosjava_core//bazel:repositories.bzl", "rosjava_repositories")

rosjava_repositories()
```

*You may want to use `http_archive` instead of `git_repository` for the reasons
described in the [Bazel docs][git-repository-docs].*

[git-repository-docs]: https://docs.bazel.build/versions/master/be/workspace.html#git_repository

You can now depend on rosjava targets (eg
`@com_github_rosjava_rosjava_core//rosjava`) as required by your application.
