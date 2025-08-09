package io.github.jmusacchio.kogito.generator.gradle.plugin.extensions;

import org.gradle.api.Project;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

public class KogitoExtension implements Serializable {

  private File projectBaseDir;

  private Map<String, String> properties;

  private Project project;

  private File outputDirectory;

  private File baseDir;

  private String projectSourceEncoding;

  private boolean persistence;

  private boolean generateRules;

  private boolean generateProcesses;

  private boolean generateDecisions;

  private boolean generatePredictions;

  private boolean autoBuild;

  public KogitoExtension(Project project) {
    this.projectBaseDir = project.getProjectDir();
    this.project = project;
    this.outputDirectory = new File(project.getBuildDir() + "/classes");
    this.baseDir = project.getProjectDir();
    this.projectSourceEncoding = System.getProperty("file.encoding");
    this.persistence = true;
    this.generateRules = true;
    this.generateProcesses = true;
    this.generateDecisions = true;
    this.generatePredictions = true;
    this.autoBuild = true;
  }

  public File getProjectBaseDir() {
    return projectBaseDir;
  }

  public void setProjectBaseDir(File projectBaseDir) {
    this.projectBaseDir = projectBaseDir;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public File getBaseDir() {
    return baseDir;
  }

  public void setBaseDir(File baseDir) {
    this.baseDir = baseDir;
  }

  public String getProjectSourceEncoding() {
    return projectSourceEncoding;
  }

  public void setProjectSourceEncoding(String projectSourceEncoding) {
    this.projectSourceEncoding = projectSourceEncoding;
  }

  public boolean isPersistence() {
    return persistence;
  }

  public void setPersistence(boolean persistence) {
    this.persistence = persistence;
  }

  public boolean isGenerateRules() {
    return generateRules;
  }

  public void setGenerateRules(boolean generateRules) {
    this.generateRules = generateRules;
  }

  public boolean isGenerateProcesses() {
    return generateProcesses;
  }

  public void setGenerateProcesses(boolean generateProcesses) {
    this.generateProcesses = generateProcesses;
  }

  public boolean isGenerateDecisions() {
    return generateDecisions;
  }

  public void setGenerateDecisions(boolean generateDecisions) {
    this.generateDecisions = generateDecisions;
  }

  public boolean isGeneratePredictions() {
    return generatePredictions;
  }

  public void setGeneratePredictions(boolean generatePredictions) {
    this.generatePredictions = generatePredictions;
  }

  public boolean isAutoBuild() {
    return autoBuild;
  }

  public void setAutoBuild(boolean autoBuild) {
    this.autoBuild = autoBuild;
  }
}
