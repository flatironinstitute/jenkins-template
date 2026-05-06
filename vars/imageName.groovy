def call(String tag = "") {
  return "$REGISTRY_PREFIX/${JOB_NAME.toLowerCase()}:$BUILD_NUMBER$tag"
}
