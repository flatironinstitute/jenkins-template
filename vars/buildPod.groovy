def call(
    String dockerfile = "Dockerfile",
    String context = ".",
    String tag = "",
    Closure body) {
  def IMAGE = "$REGISTRY_PREFIX/${JOB_NAME.toLowerCase()}:$BUILD_NUMBER$tag"
  podTemplate(inheritFrom: 'podman') {
    node(POD_LABEL) {
      container('main') {
        sh "podman build -t $IMAGE -f $dockerfile"
        sh "podman push $IMAGE"
      }
    }
  }
  podTemplate(inheritFrom: 'default', containers: [containerTemplate(name: 'main', image: IMAGE)]) {
    node(POD_LABEL) {
      container('main') {
        body.call()
      }
    }
  }
}
