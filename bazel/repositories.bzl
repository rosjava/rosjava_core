"""External dependencies for rosjava_core, excluding Maven dependencies.

Maven dependencies must be added to the workspace with bazel-deps.
"""

def rosjava_repositories():
  # DO NOT SUBMIT: update this after the pull request has been merged:
  # https://github.com/rosjava/rosjava_bootstrap/pull/66
  _maybe(native.http_archive,
      name = "com_github_rosjava_rosjava_bootstrap",
      sha256 = "56e52765c47b8f210f423cca49238beef50ff25310439c809717cb607b5a2271",
      strip_prefix = "rosjava_bootstrap-b4dd3fc4adbe8e3613b0ecddcb8b2ea3fda6f5e2",
      urls = [
          "https://github.com/rosjava/rosjava_bootstrap/archive/b4dd3fc4adbe8e3613b0ecddcb8b2ea3fda6f5e2.tar.gz",
      ],
  )


def _maybe(repo_rule, name, **kwargs):
  if name not in native.existing_rules():
    repo_rule(name=name, **kwargs)
