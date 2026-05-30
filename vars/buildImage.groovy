def call(Map args) {
  def context    = args.context ?: ".";
  def dockerfile = args.dockerfile ?: "Dockerfile";
  def buildArgs  = args.buildArgs ?: "";
  def prep       = args.prep;
  String image = imageName(args.tag ?: "latest")

  podTemplate(inheritFrom: 'podman', showRawYaml: false) {
    node(POD_LABEL) {
      checkout scm
      container('main') {
        if (prep) {
          def extra = prep.call()
          if (extra)
            buildArgs += " $extra"
        }
        sh "podman build -t $image -f $context/$dockerfile $buildArgs $context"
        sh "podman push $image"
      }
    }
  }
}
