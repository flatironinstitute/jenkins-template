def call(String to = "") {
  if (currentBuild.currentResult != 'SUCCESS') {
    emailext(subject: '$DEFAULT_SUBJECT',
      body: '$DEFAULT_CONTENT',
      to: to ?: env.CHANGE_AUTHOR_EMAIL,
      recipientProviders: [developers(), brokenBuildSuspects(), requestor(), culprits()])
  }
}
