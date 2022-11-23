package io.github.jmusacchio.kogito.generator.gradle.plugin.extensions;

import org.gradle.api.Project;
import org.kie.kogito.codegen.core.utils.GeneratedFileWriter;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

public class KogitoExtension implements Serializable {

  private File projectDir;

  private Map<String, String> properties;

  private Project project;

  private File outputDirectory;

  private File generatedSources;

  private File generatedResources;

  private boolean persistence;

  private boolean generateRules;

  private boolean generateProcesses;

  private boolean generateDecisions;

  private boolean generatePredictions;

  private boolean autoBuild;

  public KogitoExtension(Project project) {
    this.projectDir = project.getProjectDir();
    this.project = project;
    this.outputDirectory = new File(project.getBuildDir() + "/classes");
    this.generatedSources = new File(project.getBuildDir() + "/" + GeneratedFileWriter.DEFAULT_SOURCES_DIR);
    this.generatedResources = new File(project.getBuildDir() + "/" + GeneratedFileWriter.DEFAULT_RESOURCE_PATH);
    this.persistence = true;
    this.generateRules = true;
    this.generateProcesses = true;
    this.generateDecisions = true;
    this.generatePredictions = true;
    this.autoBuild = true;
  }

  public File getProjectDir() {
    return projectDir;
  }

  public void setProjectDir(File projectDir) {
    this.projectDir = projectDir;
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

  public File getGeneratedSources() {
    return generatedSources;
  }

  public void setGeneratedSources(File generatedSources) {
    this.generatedSources = generatedSources;
  }

  public File getGeneratedResources() {
    return generatedResources;
  }

  public void setGeneratedResources(File generatedResources) {
    this.generatedResources = generatedResources;
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
