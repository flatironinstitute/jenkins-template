def call() {
  if (currentBuild.currentResult != 'SUCCESS')
    emailext(subject: '$DEFAULT_SUBJECT',
      body: '$DEFAULT_CONTENT',
      recipientProviders: [developers()])
}
