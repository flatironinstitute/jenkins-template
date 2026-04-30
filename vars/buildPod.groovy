def call(Map args, Closure body) {
  def context    = args.context ?: ".";
  def dockerfile = args.dockerfile ?: "$context/Dockerfile";
  def tag        = args.tag ?: "";
  def cpus       = args.cpus ?: 4;
  def memory     = args.memory ?: "16Gi";
  def gpus       = args.gpus ?: 0;
  def gpuType    = args.gpuType;
  String image = "$REGISTRY_PREFIX/${JOB_NAME.toLowerCase()}:$BUILD_NUMBER$tag"

  podTemplate(inheritFrom: 'podman', showRawYaml: false) {
    node(POD_LABEL) {
      container('main') {
        checkout scm
        sh "podman build -t $image -f $dockerfile $context"
        sh "podman push $image"
      }
    }
  }

  podTemplate(inheritFrom: 'default', yaml: """
    spec:
      runtimeClassName: ${gpus > 0 ? "nvidia" : ""}
      imagePullSecrets:
        - name: registry-auth
      nodeSelector:
        nvidia: ${gpuType}
      containers:
        - name: main
          image: $image
          command: [sleep]
          args: [99999]
          securityContext:
            runAsUser: 1000
            runAsGroup: 1000
          resources:
            limits:
              cpu: ${cpus}
              memory: ${memory}
              nvidia.com/gpu: ${gpus}
    """) {
    node(POD_LABEL) {
      container('main') {
        checkout scm
        body.call()
      }
    }
  }
}
