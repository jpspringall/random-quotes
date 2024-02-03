import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetTest

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {

    buildType(Octopus_RandomQuotes_Build)
}

object Octopus_RandomQuotes_Build : BuildType({
    id("Build")
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dotnetTest {
            name = "DotnetTest"
            id = "DotnetTest"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetPublish {
            name = "DotnetPublish"
            id = "DotnetPublish"
            configuration = "Release"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        step {
            name = "octopus_pack_package"
            id = "octopus_pack_package"
            type = "octopus.pack.package"
            param("octopus_packageoutputpath", ".")
            param("octopus_packageid", "RandomQuotes")
            param("octopus_packageversion", "1.0.%build.counter%")
            param("octopus_packageformat", "NuPkg")
            param("octopus_packagesourcepath", "RandomQuotes/bin/Release/net6.0/publish/")
        }
        step {
            name = "octopus_push_package"
            id = "octopus_push_package"
            type = "octopus.push.package"
            param("octopus_host", "http://octopus:8080")
            param("octopus_packagepaths", "RandomQuotes.1.0.%build.counter%.nupkg")
            param("octopus_forcepush", "false")
            param("secure:octopus_apikey", "credentialsJSON:d96f01b9-2413-42ef-b75d-b4746b647bb4")
        }
    }
})
