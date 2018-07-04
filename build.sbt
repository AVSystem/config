// to release, bump major/minor/micro as appropriate,
// update NEWS, update version in README.md, tag, then
// publishSigned.
// Release tags should follow: http://semver.org/
import scalariform.formatter.preferences._

enablePlugins(GitVersioning)
git.baseVersion := "1.3.5"

organization in GlobalScope := "com.avsystem"

scalacOptions in GlobalScope in Compile := Seq("-unchecked", "-deprecation", "-feature")
scalacOptions in GlobalScope in Test := Seq("-unchecked", "-deprecation", "-feature")

scalaVersion in ThisBuild := "2.10.4"

isSnapshot in ThisBuild := false

publishTo in ThisBuild := {
  val avsystem = "https://repo.avsystem.com/"
  if (isSnapshot.value)
    Some("snapshots" at avsystem + "libs-snapshot")
  else
    Some("releases"  at avsystem + "libs-release-local")
}

/* The file with credentials must have following format:
 *
 * realm=Artifactory Realm
 * host=repo.avsystem.com
 * user=<LDAP user>
 * password=<LDAP password>
 *
 */
credentials in ThisBuild +=
  Credentials(Path.userHome / ".repo.avsystem.com.credentials")



lazy val commonSettings: Seq[Setting[_]] = Def.settings(
  unpublished,
  scalariformPreferences := scalariformPreferences.value
    .setPreference(IndentSpaces, 4)
    .setPreference(FirstArgumentOnNewline, Preserve)
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    aggregate in doc := false,
    doc := (doc in (configLib, Compile)).value,
    aggregate in packageDoc := false,
    packageDoc := (packageDoc in (configLib, Compile)).value,
    aggregate in checkstyle := false,
    checkstyle := (checkstyle in (configLib, Compile)).value
  )
  .aggregate(
    testLib, configLib,
    simpleLibScala, simpleAppScala, complexAppScala,
    simpleLibJava, simpleAppJava, complexAppJava
  )

lazy val configLib =  Project("config", file("config"))
  .settings(
    osgiSettings,
    OsgiKeys.exportPackage := Seq("com.typesafe.config", "com.typesafe.config.impl"),
    packageOptions in (Compile, packageBin) +=
      Package.ManifestAttributes("Automatic-Module-Name" -> "typesafe.config" ),
    scalariformPreferences := scalariformPreferences.value
      .setPreference(IndentSpaces, 4)
      .setPreference(FirstArgumentOnNewline, Preserve)
  )
  .enablePlugins(SbtOsgi)
  .dependsOn(testLib % "test->test")

def proj(id: String, base: File) = Project(id, base) settings commonSettings

lazy val testLib = proj("config-test-lib", file("test-lib"))

lazy val simpleLibScala  = proj("config-simple-lib-scala",  file("examples/scala/simple-lib"))  dependsOn configLib
lazy val simpleAppScala  = proj("config-simple-app-scala",  file("examples/scala/simple-app"))  dependsOn simpleLibScala
lazy val complexAppScala = proj("config-complex-app-scala", file("examples/scala/complex-app")) dependsOn simpleLibScala

lazy val simpleLibJava  = proj("config-simple-lib-java",  file("examples/java/simple-lib"))  dependsOn configLib
lazy val simpleAppJava  = proj("config-simple-app-java",  file("examples/java/simple-app"))  dependsOn simpleLibJava
lazy val complexAppJava = proj("config-complex-app-java", file("examples/java/complex-app")) dependsOn simpleLibJava

val unpublished = Seq(
  // no artifacts in this project
  publishArtifact := false,
  // make-pom has a more specific publishArtifact setting already
  // so needs specific override
  publishArtifact in makePom := false,
  // no docs to publish
  publishArtifact in packageDoc := false,
  // can't seem to get rid of ivy files except by no-op'ing the entire publish task
  publish := {},
  publishLocal := {}
)
