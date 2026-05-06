def call(Map args, Closure body) {
  buildImage(args)
  runPod(args, body)
}
