package com.ericsson.de.taf.jenkins.dsl.utils

import com.ericsson.de.taf.jenkins.dsl.Constants
import com.ericsson.de.taf.jenkins.dsl.builders.GitBuilder
import javaposse.jobdsl.dsl.helpers.ScmContext

final class Git {

    private Git() {
    }

    static def simple(ScmContext scm) {
        new GitBuilder().build(scm)
    }

    static def gerrit(ScmContext scm) {
        new GitBuilder(
                remoteRefspec: Constants.GERRIT_REFSPEC,
                gitBranch: Constants.GERRIT_BRANCH,
                addGerritTrigger: true
        ).build(scm)
    }
}
