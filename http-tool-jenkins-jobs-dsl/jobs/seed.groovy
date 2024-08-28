import com.ericsson.de.taf.jenkins.dsl.Constants

import static com.ericsson.de.taf.jenkins.dsl.Constants.*


job("${Constants.JOBS_PREFIX}-job-seed") {
    description "Job DSL seed job for auto provisioning the rest of the ${Constants.PROJECT_NAME} jobs"
    scm {
        git {
            remote {
                url Constants.GIT_URL
                name Constants.GIT_REMOTE
            }
            branch Constants.GIT_BRANCH
        }
    }
    triggers {
        scm 'H/2 * * * *'
    }
    steps {
        dsl {
            external "${JOBS_PATH}/*.groovy"
            additionalClasspath "${JOBS_MODULE}/src/main/groovy"
            removeAction 'DELETE'
        }
    }
}
