def call(Map args, Closure body) {
  def cpus       = args.cpus ?: 4;
  def memory     = args.memory ?: "16Gi";
  def gpus       = args.gpus ?: 0;
  def gpuType    = args.gpuType;
  def image      = args.image ?: imageName(args.tag ?: "");

  podTemplate(inheritFrom: 'jnlp', yaml: """
    spec:
      runtimeClassName: ${gpus > 0 ? "nvidia" : ""}
      imagePullSecrets:
        - name: registry-auth
      nodeSelector:
        ${gpuType ? "nvidia: $gpuType" : ""}
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
          env:
          - name: PARALLEL
            value: ${cpus}
    """) {
    node(POD_LABEL) {
      checkout scm
      container('main') {
        body.call()
      }
    }
  }
}

