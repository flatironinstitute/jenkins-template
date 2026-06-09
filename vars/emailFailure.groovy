def call() {
  if (currentBuild.currentResult != 'SUCCESS') {
    emailext(subject: '$DEFAULT_SUBJECT',
      body: '$DEFAULT_CONTENT',
      to: env.CHANGE_AUTHOR_EMAIL,
      recipientProviders: [developers()])
  }
}
