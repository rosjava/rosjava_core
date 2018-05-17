# Do not edit. bazel-deps autogenerates this file from dependencies.yaml.

def declare_maven(hash):
    native.maven_jar(
        name = hash["name"],
        artifact = hash["artifact"],
        sha1 = hash["sha1"],
        repository = hash["repository"]
    )
    native.bind(
        name = hash["bind"],
        actual = hash["actual"]
    )

def list_dependencies():
    return [
    {"artifact": "com.google.code.findbugs:jsr305:1.3.9", "lang": "java", "sha1": "40719ea6961c0cb6afaeb6a921eaa1f6afd4cfdf", "repository": "https://jcenter.bintray.com/", "name": "com_google_code_findbugs_jsr305", "actual": "@com_google_code_findbugs_jsr305//jar", "bind": "jar/com/google/code/findbugs/jsr305"},
    {"artifact": "com.google.guava:guava:12.0", "lang": "java", "sha1": "5bc66dd95b79db1e437eb08adba124a3e4088dc0", "repository": "https://jcenter.bintray.com/", "name": "com_google_guava_guava", "actual": "@com_google_guava_guava//jar", "bind": "jar/com/google/guava/guava"},
    {"artifact": "commons-pool:commons-pool:1.6", "lang": "java", "sha1": "4572d589699f09d866a226a14b7f4323c6d8f040", "repository": "https://jcenter.bintray.com/", "name": "commons_pool_commons_pool", "actual": "@commons_pool_commons_pool//jar", "bind": "jar/commons_pool/commons_pool"},
    {"artifact": "dnsjava:dnsjava:2.1.1", "lang": "java", "sha1": "5708df81cf6dbff545695f0be6380b7bbc85accc", "repository": "https://jcenter.bintray.com/", "name": "dnsjava_dnsjava", "actual": "@dnsjava_dnsjava//jar", "bind": "jar/dnsjava/dnsjava"},
    {"artifact": "io.netty:netty:3.5.13.Final", "lang": "java", "sha1": "389edea561995909d5df3e70fb2c49954ac79a54", "repository": "https://jcenter.bintray.com/", "name": "io_netty_netty", "actual": "@io_netty_netty//jar", "bind": "jar/io/netty/netty"},
# duplicates in junit:junit fixed to 4.8.2
# - dnsjava:dnsjava:2.1.1 wanted version 3.8.2
# - org.apache.ws.commons:ws-commons-util:1.0.1 wanted version 3.8.1
    {"artifact": "junit:junit:4.8.2", "lang": "java", "sha1": "c94f54227b08100974c36170dcb53329435fe5ad", "repository": "https://jcenter.bintray.com/", "name": "junit_junit", "actual": "@junit_junit//jar", "bind": "jar/junit/junit"},
    {"artifact": "org.apache.commons:com.springsource.org.apache.commons.codec:1.3.0", "lang": "java", "sha1": "a7c156ef67bdc69ae36a9c70adf7b8ca2633132c", "repository": "https://jcenter.bintray.com/", "name": "org_apache_commons_com_springsource_org_apache_commons_codec", "actual": "@org_apache_commons_com_springsource_org_apache_commons_codec//jar", "bind": "jar/org/apache/commons/com_springsource_org_apache_commons_codec"},
    {"artifact": "org.apache.commons:com.springsource.org.apache.commons.httpclient:3.1.0", "lang": "java", "sha1": "c493a925a091ac3d8f449d9265fe12482dabb2a7", "repository": "https://jcenter.bintray.com/", "name": "org_apache_commons_com_springsource_org_apache_commons_httpclient", "actual": "@org_apache_commons_com_springsource_org_apache_commons_httpclient//jar", "bind": "jar/org/apache/commons/com_springsource_org_apache_commons_httpclient"},
    {"artifact": "org.apache.commons:com.springsource.org.apache.commons.io:1.4.0", "lang": "java", "sha1": "51097cd000ec88214ff181c9f059947de1cf4a35", "repository": "https://jcenter.bintray.com/", "name": "org_apache_commons_com_springsource_org_apache_commons_io", "actual": "@org_apache_commons_com_springsource_org_apache_commons_io//jar", "bind": "jar/org/apache/commons/com_springsource_org_apache_commons_io"},
    {"artifact": "org.apache.commons:com.springsource.org.apache.commons.lang:2.4.0", "lang": "java", "sha1": "686e3713650c8e3a5d98723b4d3e239586bf9822", "repository": "https://jcenter.bintray.com/", "name": "org_apache_commons_com_springsource_org_apache_commons_lang", "actual": "@org_apache_commons_com_springsource_org_apache_commons_lang//jar", "bind": "jar/org/apache/commons/com_springsource_org_apache_commons_lang"},
    {"artifact": "org.apache.commons:com.springsource.org.apache.commons.logging:1.1.1", "lang": "java", "sha1": "7657caf2c78e1d79c74d36f2ae128a115f7cc180", "repository": "https://jcenter.bintray.com/", "name": "org_apache_commons_com_springsource_org_apache_commons_logging", "actual": "@org_apache_commons_com_springsource_org_apache_commons_logging//jar", "bind": "jar/org/apache/commons/com_springsource_org_apache_commons_logging"},
    {"artifact": "org.apache.commons:com.springsource.org.apache.commons.net:2.0.0", "lang": "java", "sha1": "03e78766072ff5a5cf3f5431d973c430c94ec612", "repository": "https://jcenter.bintray.com/", "name": "org_apache_commons_com_springsource_org_apache_commons_net", "actual": "@org_apache_commons_com_springsource_org_apache_commons_net//jar", "bind": "jar/org/apache/commons/com_springsource_org_apache_commons_net"},
    {"artifact": "org.apache.ws.commons:ws-commons-util:1.0.1", "lang": "java", "sha1": "126e80ff798fece634bc94e61f8be8a8da00be60", "repository": "https://jcenter.bintray.com/", "name": "org_apache_ws_commons_ws_commons_util", "actual": "@org_apache_ws_commons_ws_commons_util//jar", "bind": "jar/org/apache/ws/commons/ws_commons_util"},
    {"artifact": "org.mockito:mockito-all:1.8.5", "lang": "java", "sha1": "a927d8ae3b8d22eb745a74f94e59ce3882f2b524", "repository": "https://jcenter.bintray.com/", "name": "org_mockito_mockito_all", "actual": "@org_mockito_mockito_all//jar", "bind": "jar/org/mockito/mockito_all"},
    {"artifact": "org.ros.rosjava_bootstrap:gradle_plugins:0.3.0", "lang": "java", "sha1": "9915d897f3b5049e99b268068978a1bca0ae7b5d", "repository": "https://github.com/rosjava/rosjava_mvn_repo/raw/master", "name": "org_ros_rosjava_bootstrap_gradle_plugins", "actual": "@org_ros_rosjava_bootstrap_gradle_plugins//jar", "bind": "jar/org/ros/rosjava_bootstrap/gradle_plugins"},
    {"artifact": "org.ros.rosjava_bootstrap:message_generation:0.3.0", "lang": "java", "sha1": "5141e448f0ea6a2d6f539219d4fc9b296ff07ca4", "repository": "https://github.com/rosjava/rosjava_mvn_repo/raw/master", "name": "org_ros_rosjava_bootstrap_message_generation", "actual": "@org_ros_rosjava_bootstrap_message_generation//jar", "bind": "jar/org/ros/rosjava_bootstrap/message_generation"},
    {"artifact": "org.ros.rosjava_messages:rosgraph_msgs:1.11.2", "lang": "java", "sha1": "adb731dd5181976b23c217872f61c724a3af70fe", "repository": "https://github.com/rosjava/rosjava_mvn_repo/raw/master", "name": "org_ros_rosjava_messages_rosgraph_msgs", "actual": "@org_ros_rosjava_messages_rosgraph_msgs//jar", "bind": "jar/org/ros/rosjava_messages/rosgraph_msgs"},
    {"artifact": "org.ros.rosjava_messages:std_msgs:0.5.11", "lang": "java", "sha1": "a0191f0506dfecfc471eb199a119691717801c0e", "repository": "https://github.com/rosjava/rosjava_mvn_repo/raw/master", "name": "org_ros_rosjava_messages_std_msgs", "actual": "@org_ros_rosjava_messages_std_msgs//jar", "bind": "jar/org/ros/rosjava_messages/std_msgs"},
    {"artifact": "xml-apis:xml-apis:1.0.b2", "lang": "java", "sha1": "3136ca936f64c9d68529f048c2618bd356bf85c9", "repository": "https://jcenter.bintray.com/", "name": "xml_apis_xml_apis", "actual": "@xml_apis_xml_apis//jar", "bind": "jar/xml_apis/xml_apis"},
    ]

def maven_dependencies(callback = declare_maven):
    for hash in list_dependencies():
        callback(hash)
