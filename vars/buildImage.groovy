def call(Map args) {
  def context    = args.context ?: ".";
  def dockerfile = args.dockerfile ?: "Dockerfile";
  def buildArgs  = args.buildArgs ?: "";
  def prep       = args.prep;
  String image = imageName(args.tag ?: "")

  podTemplate(inheritFrom: 'podman', showRawYaml: false) {
    node(POD_LABEL) {
      checkout scm
      container('main') {
        if (prep)
           prep.call()
        sh "podman build -t $image -f $context/$dockerfile $buildArgs $context"
        sh "podman push $image"
      }
    }
  }
}
