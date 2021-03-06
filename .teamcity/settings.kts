import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.MSBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.msBuild
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_1.failureConditions.BuildFailureOnText
import jetbrains.buildServer.configs.kotlin.v2018_1.failureConditions.failOnText
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_1.ui.add
import jetbrains.buildServer.configs.kotlin.v2018_1.ui.id
import java.awt.DisplayMode
import javax.swing.text.html.HTML.Attribute.N

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

version = "2018.1"

project {
    description = "My first project"

    subProject (SBG)
}

object SBG : Project({

/*General Settings*/
    name = "ServiceBusGateway"
    uuid = "ServiceBusGatewayUuId"
    description = "WebApi for authenticating a request"
    id ("ServiceBusGatewayId")

/*Parameters*/
    params {
        param("RepoName" , "orders-servicebusgateway")
        param("env.Connection", "Test123")
        param("system.Connection", "system value")
        password("AccessKey" ,"password123")
        param("CheckIfHidden" , "HideThisValue" , "ParameterDisplay.HIDDEN")
    }
    /*SubProject*/
    subProject (BUILDSBG)
})

object BUILDSBG : Project ({
    /*General Settings*/
    name = "Build ServiceBusGateway"
    description = "Builds the service"
    id ( "ServiceBusGatewayBuild")

    /*Build Configuration*/
    buildType(Build)
})

object Build : BuildType({
    /*General Settings*/
    name = "Compile Run Test"
    id ("Compileruntest")
    artifactRules = """C:\GithubRepo\PipelineAsCode\*"""

    /*Parameters*/
    params {
        param("MSBuild.Logging.Verbosity", "detailed")
        param("MSBuild.AdditionalParameters", "/maxcpucount")
        param("Build.Configuration", "Release")
            }

    /*Triggers*/
    triggers {
        schedule {
            id = ("Trigger_1")
            var cron = ScheduleTrigger.SchedulingPolicy.Cron()
            cron.seconds = "5"
            cron.minutes= "*"
            schedulingPolicy = cron
        }
        vcs {
        id = ("Trigger_2")
             }
    }

    val myvcsRoot = GitVcsRoot {
        id = AbsoluteId("HttpsGithubComShianctStringcalculatorGit")
        name = "https://github.com/shianct/Stringcalculator.git"
        url = "https://github.com/shianct/Stringcalculator.git"
    }

    /*VCS Settings*/
    vcs {
        root(myvcsRoot)
        root(DslContext.settingsRootId)
        }

    /*Agent requirements*/
    requirements{
        contains("teamcity.agent.name" , "ip_172.17")
    }

    /*failure conditions*/
    failureConditions{
        failOnText {
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "FailFailFail"
            failureMessage = "This is a predetermined failure"
            stopBuildOnFailure = true
        }
    }

    /*Build Steps*/
    steps {
        script {
            name = "Set version number"
            scriptContent = """
                #!/bin/bash
                HASH=%build.vcs.number%
                SHORT_HASH=${"$"}{HASH:0:7}
                BUILD_COUNTER=%build.counter%
                BUILD_NUMBER="1.0.${"$"}BUILD_COUNTER.${"$"}SHORT_HASH"
                echo "##teamcity[buildNumber '${"$"}BUILD_NUMBER']"
            """.trimIndent()
        }

        msBuild {
            name = "build solution"
            path = """C:\GithubRepo\Stringcalculator\String Calculator.sln"""
            toolsVersion = MSBuildStep.MSBuildToolsVersion.V15_0
            args = "/p:Configuration=%Build.Configuration% /verbosity:%MSBuild.Logging.Verbosity% %MSBuild.AdditionalParameters%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }

    }

})
