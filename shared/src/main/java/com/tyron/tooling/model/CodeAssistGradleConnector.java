package com.tyron.tooling.model;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;

public interface CodeAssistGradleConnector {

    CodeAssistGradleConnector forProjectDirectory(File projectDir);
    ProjectConnection connect();
}
