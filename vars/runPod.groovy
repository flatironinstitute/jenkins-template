def call(Map args, Closure body) {
  def cpus       = args.cpus ?: 4;
  def memory     = args.memory ?: "16Gi";
  def gpus       = args.gpus ?: 0;
  def gpuType    = args.gpuType;
  def devShm     = args.devShm ?: false;
  def image      = args.image ?: imageName(args.tag ?: "latest");

  String podExtra = "";
  String containerExtra = "";

  if (gpus) {
    podExtra += """
      runtimeClassName: nvidia
    """;
    if (gpuType) {
      podExtra += """
      nodeSelector:
        nvidia: $gpuType
      """;
    }
  }
  if (devShm) {
    podExtra += """
      volumes:
        - name: devshm
          emptyDir:
            medium: Memory
    """;
    containerExtra += """
          volumeMounts:
            - name: devshm
              mountPath: /dev/shm
    """;
  }

  podTemplate(inheritFrom: 'jnlp', yaml: """
    spec:
      imagePullSecrets:
        - name: registry-auth
      ${podExtra}
      containers:
        - name: main
          image: $image
          imagePullPolicy: Always
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
          ${containerExtra}
    """) {
    node(POD_LABEL) {
      checkout scm
      container('main') {
        body.call()
      }
    }
  }
}

