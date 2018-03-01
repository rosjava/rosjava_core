# Do not edit. bazel-deps autogenerates this file from dependencies.yaml.
java_library(
    name = "gradle_plugins",
    exports = [
        "//external:jar/org/ros/rosjava_bootstrap/gradle_plugins"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "message_generation",
    exports = [
        "//external:jar/org/ros/rosjava_bootstrap/message_generation"
    ],
    runtime_deps = [
        "//3rdparty/jvm/com/google/guava:guava",
        "//3rdparty/jvm/commons_pool:commons_pool",
        "//3rdparty/jvm/io/netty:netty",
        "//3rdparty/jvm/org/apache/commons:com_springsource_org_apache_commons_codec",
        "//3rdparty/jvm/org/apache/commons:com_springsource_org_apache_commons_io",
        "//3rdparty/jvm/org/apache/commons:com_springsource_org_apache_commons_lang",
        ":gradle_plugins"
    ],
    visibility = [
        "//visibility:public"
    ]
)


