package com.ericsson.de.taf.jenkins.dsl

class Constants {

    static final String PROJECT_NAME = 'http-tool'

    static final String JOBS_PREFIX = "${PROJECT_NAME}"
    static final String GIT_PROJECT = 'OSS/com.ericsson.de/http-tool'

    static final String JOBS_MODULE = 'http-tool-jenkins-jobs-dsl'
    static final String JOBS_DIRECTORY = 'jobs'
    static final String JOBS_PATH = "${JOBS_MODULE}/${JOBS_DIRECTORY}"

    static final String DOCS_MODULE = 'http-tool'
    static final String DOCS_DIRECTORY = 'target/site'
    static final String DOCS_ZIP = "site.zip"

    static final String SLAVE_TAF_MAIN = 'taf_main_slave'
    static final String SLAVE_DOCKER_POD_H = 'FEM119_POD_H_docker_build_slave'

    static final String JDK_1_8 = 'JDK 1.8.0_25'
    static final String JDK_1_8_DOCKER = 'JDK 1.8 Docker Slave'
    static final String GERRIT_SERVER = 'gerrit.ericsson.se'
    static final String GERRIT_CENTRAL = '${GERRIT_CENTRAL}' // resolves to 'ssh://gerrit.ericsson.se:29418'
    static final String GERRIT_MIRROR = '${GERRIT_MIRROR}' // resolves to 'ssh://gerritmirror.lmera.ericsson.se:29418'
    static final String GERRIT_BRANCH = '${GERRIT_BRANCH}'

    static final String GERRIT_REFSPEC = '${GERRIT_REFSPEC}'
    static final String GIT_URL = "${GERRIT_CENTRAL}/${GIT_PROJECT}"
    static final String GIT_REMOTE = 'origin'
    static final String GIT_BRANCH = 'master'

}
