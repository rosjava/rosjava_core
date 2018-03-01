"""External dependencies for rosjava_core, excluding Maven dependencies.

Maven dependencies must be added to the workspace with bazel-deps.
"""

def rosjava_repositories():
  _maybe(native.http_archive,
      name = "com_github_rosjava_rosjava_bootstrap",
      sha256 = "3c59776a8c6e22232d07f29a686c0e5f401812ec27f59405711657d54a792c08",
      strip_prefix = "rosjava_bootstrap-62f865dbe8a7830b21e054dc2a5ac7d2edc6eafe",
      urls = [
          "https://github.com/rosjava/rosjava_bootstrap/archive/62f865dbe8a7830b21e054dc2a5ac7d2edc6eafe.tar.gz",
      ],
  )


def _maybe(repo_rule, name, **kwargs):
  if name not in native.existing_rules():
    repo_rule(name=name, **kwargs)
