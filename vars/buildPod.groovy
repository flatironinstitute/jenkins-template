def call(
    String dockerfile = "Dockerfile",
    String context = ".",
    String tag = "",
    int cpus = 4,
    String memory = "16Gi",
    int gpus = 0,
    Closure body) {

  String IMAGE = "$REGISTRY_PREFIX/${JOB_NAME.toLowerCase()}:$BUILD_NUMBER$tag"

  podTemplate(inheritFrom: 'podman') {
    node(POD_LABEL) {
      container('main') {
        sh "podman build -t $IMAGE -f $dockerfile $context"
        sh "podman push $IMAGE"
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
          image: $IMAGE
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
        body.call()
      }
    }
  }
}
