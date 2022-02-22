import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import hpe.harmony.sbtPlugin.lib.{HPEArtifactory, MavenRepository => HarmonyMavenRepository}
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoPlugin

object settings {

  case object InfosightLibraries
      extends HarmonyMavenRepository(
        name = "infosight-libaries",
        url = new URL(HPEArtifactory.baseURL + "/infosight-libraries")
      )

  def packaging(name: String) =
    Seq(
      Docker / packageName := name,
      Docker / maintainer := "HPE harmony team",
      dockerUpdateLatest := true,
      dockerExposedPorts := List(8080)
    )

  def buildInfo = enablePlugins(BuildInfoPlugin)

  def common =
    Seq(
      exportJars := true,
      resolvers += InfosightLibraries.resolver(false)
    )
}
