def stagedProjects = []

hubot room: 'release', message: "starting fabric8 project release"
try {
  String fabric8PullRequest = bumpFabric8Versions{}
  if (fabric8PullRequest != null){
    waitUntilPullRequestMerged{
      name = 'fabric8'
      prId = fabric8PullRequest
    }
  }

  stagedProjects << stageProject{
    project = 'fabric8'
  }

  fabric8ReleasePR = releaseFabric8 {
    projectStagingDetails = stagedProjects
    project = 'fabric8'
  }
  stagedProjects = []
  waitUntilArtifactSyncedWithCentral {
    artifact = 'fabric8-maven-plugin'
  }

  hubot room: 'release', message: "fabric8 release was successful"
  hubot room: 'release', message: "fabric8 updating fabric8-devops, fabric8-ipaas and ipaas-quickstarts with new fabric8.version"

  parallel(quickstarts: {
    String quickstartPr = bumpiPaaSQuickstartsVersions{}
    if (quickstartPr != null){
      waitUntilPullRequestMerged{
        name = 'ipaas-quickstarts'
        prId = quickstartPr
      }
    }
  }, devops: {
    String devopsPr = bumpFabric8DevOpsVersions{}
    if (devopsPr != null){
      waitUntilPullRequestMerged{
        name = 'fabric8-devops'
        prId = devopsPr
      }
    }
  }, ipaas: {
    String ipaasPr = bumpFabric8iPaaSVersions{}
    if (ipaasPr != null){
      waitUntilPullRequestMerged{
        name = 'fabric8-ipaas'
        prId = ipaasPr
      }
    }
  })

  hubot room: 'release', message: "fabric8-devops, fabric8-ipaas and ipaas-quickstarts updated with new fabric8.version"
} catch (err){
    hubot room: 'release', message: "fabric8 release failed ${err}"
    currentBuild.result = 'FAILURE'
}
