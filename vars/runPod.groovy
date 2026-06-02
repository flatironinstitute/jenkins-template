def call(Map args, Closure body) {
  def cpus       = args.cpus ?: 4;
  def memory     = args.memory ?: "16Gi";
  def cpuType    = args.cpuType ?: [];
  def gpus       = args.gpus ?: 0;
  def gpuType    = args.gpuType;
  def devShm     = args.devShm ?: false;
  def mounts     = args.mounts ?: [:];
  def image      = args.image ?: imageName(args.tag ?: "latest");

  def spec = [
    'imagePullSecrets': [['name': 'registry-auth']],
    'nodeSelector': [:],
    'volumes': [],
    'containers': [[
      'name': 'main',
      'image': image,
      'imagePullPolicy': 'Always',
      'command': ['sleep', '99999'],
      'securityContext': [
        'runAsUser': 1000,
        'runAsGroup': 1000],
      'resources': [
        'limits': [
          'cpu': cpus,
          'memory': memory,
          'nvidia.com/gpu': gpus]],
      'env': [[
        'name': 'PARALLEL',
        'value': cpus]],
      'volumeMounts': []]]]

  if (cpuType in String)
    cpuType = [cpuType]
  cpuType.each { cput ->
    spec['nodeSelector']["feature.node.kubernetes.io/cpu-cpuid.${cput.toUpperCase()}"] = "true"
  }

  if (gpus) {
    spec['runtimeClassName'] = 'nvidia'
    if (gpuType) {
      spec['nodeSelector']['nvidia'] = gpuType
    }
  }

  if (devShm) {
    spec['volumes'] << [
      'name': 'devshm',
      'emptyDir': [
        'medium': 'Memory']]
    spec['containers'][0]['volumeMounts'] << [
      'name': 'devshm',
      'mountPath': '/dev/shm']
  }

  mounts.each { pvc, path ->
    spec['volumes'] << [
      'name': pvc,
      'persistentVolumeClaim': [
        'claimName': pvc]]
    spec['containers'][0]['volumeMounts'] << [
      'name': pvc,
      'mountPath': path]
  }

  def yaml = writeYaml(returnText: true, data: ['spec': spec])
  podTemplate(inheritFrom: 'jnlp', yaml: yaml) {
    node(POD_LABEL) {
      checkout scm
      container('main') {
        body.call()
      }
    }
  }
}
