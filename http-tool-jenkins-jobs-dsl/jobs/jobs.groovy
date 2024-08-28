import com.ericsson.de.taf.jenkins.dsl.builders.*
import javaposse.jobdsl.dsl.DslFactory

/*
Full API documentation:
https://jenkinsci.github.io/job-dsl-plugin/

Job DSL playground:
http://job-dsl.herokuapp.com/
*/


def mvnUnitTest = "clean install -T 4"
def mvnDeploy = "clean deploy -DskipTests"

def unitTests = 'Unit tests'
def snapshots = 'Snapshots deployment'


//Gerrit flow
def aa = new GerritJobBuilder('AA-gerrit-unit-tests', unitTests, mvnUnitTest)
def ab = new SonarQubeGerritJobBuilder('AB-gerrit-sonar-qube')

//Build flow
def ba = new SimpleJobBuilder('BA-unit-tests', unitTests, mvnUnitTest)
def bb = new SimpleJobBuilder('BB-deploy-snapshots', snapshots, mvnDeploy)
def bc = new DocsBuildJobBuilder('BC-docs-build')
def bd = new DocsPublishJobBuilder('BD-docs-publish', bc.name)


def build = new MasterBuildFlowBuilder('B-build-flow','.*-release',
        """\
           build '${ba.name}'
           build '${bb.name}'
           build '${bc.name}'
           build '${bd.name}'
        """.stripIndent())

def release = new ReleaseJobBuilder('XX-release', build.name)

[aa, ab, ba, bb, bc, bd, build, release]*.build(this as DslFactory)
