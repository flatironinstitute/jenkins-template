String call(String tag = "latest") {
  return "$REGISTRY_PREFIX/${JOB_NAME.toLowerCase()}:$tag"
}
