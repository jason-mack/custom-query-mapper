resolvers += "Artifactory" at "https://hpeartifacts.jfrog.io/hpeartifacts/harmony-sbt-plugins/"

credentials ++= Bootstrap.credentials("HPE_ARTIFACTORY", "Artifactory Realm", "hpeartifacts.jfrog.io").toSeq

addSbtPlugin("hpe.harmony" % "sbt-harmony" % "1.3.0")

addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.6.0")

addSbtPlugin("org.scoverage"  % "sbt-scoverage" % "1.6.1")
addSbtPlugin("com.github.mwz" % "sbt-sonar"     % "2.2.0")
