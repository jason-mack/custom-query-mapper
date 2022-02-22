import java.io.File

import sbt._

object Bootstrap extends AutoPlugin {

  override val trigger = AllRequirements

  def credentialsFrom(file: File): Option[Credentials]   = if (file.exists()) Some(Credentials(file)) else None
  def credentials(hostName: String): Option[Credentials] = {
    val filePath = Path.userHome / ".ivy2" / s".credentials-$hostName"
    credentialsFrom(filePath)
  }

  def credentials(envPrefix: String, realm: String, hostName: String): Option[Credentials] = {
    val credentialsEnvironmentUsernameVar = envPrefix + "_USERNAME"
    val credentialsEnvironmentPasswordVar = envPrefix + "_PASSWORD"
    (sys.env.get(credentialsEnvironmentUsernameVar), sys.env.get(credentialsEnvironmentPasswordVar)) match {
      case (Some(username), Some(password)) => Some(Credentials(realm, hostName, username, password))
      case _                                => credentials(hostName)
    }
  }

}
