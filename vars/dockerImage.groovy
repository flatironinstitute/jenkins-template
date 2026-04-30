def call(String dockerfile = "Dockerfile", Closure body) {
  def IMAGE = "$REGISTRY_PREFIX/${JOB_NAME.toLowerCase()}:$BUILD_NUMBER"
  podTemplate(inheritFrom: 'podman', defaultContainer: 'main') {
    node(POD_LABEL) {
      sh "podman build -t $IMAGE -f $dockerfile"
      sh "podman push $IMAGE"
    }
  }
}
