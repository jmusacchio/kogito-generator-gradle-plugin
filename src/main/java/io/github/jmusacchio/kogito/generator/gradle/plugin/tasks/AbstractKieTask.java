package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util;
import org.drools.codegen.common.GeneratedFileWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.kie.kogito.codegen.manager.util.CodeGenManagerUtil;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractKieTask extends DefaultTask {

  protected static final GeneratedFileWriter.Builder GENERATED_FILE_WRITER_BUILDER = GeneratedFileWriter.builder("kogito", "kogito.codegen.resources.directory", "kogito.codegen.sources.directory");

  @org.gradle.api.tasks.Optional
  @Input
  private Map<String, String> properties;

  @Internal
  private File outputDirectory;

  @Internal
  private File baseDir;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean persistence;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generateRules;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generateProcesses;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generateDecisions;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generatePredictions;

  @Input
  protected String projectSourceEncoding;

  @Internal
  private File projectBaseDir;

  public AbstractKieTask(KogitoExtension extension) {
    this.projectBaseDir = extension.getProjectBaseDir();
    this.properties = extension.getProperties();
    this.outputDirectory = extension.getOutputDirectory();
    this.baseDir = extension.getBaseDir();
    this.persistence = extension.isPersistence();
    this.generateRules = extension.isGenerateRules();
    this.generateProcesses = extension.isGenerateProcesses();
    this.generateDecisions = extension.isGenerateDecisions();
    this.generatePredictions = extension.isGeneratePredictions();
    this.projectSourceEncoding = extension.getProjectSourceEncoding();
  }

  protected void setSystemProperties(Map<String, String> properties) {
    if (properties != null) {
      getLogger().debug("Additional system properties: " + properties);
      for (Map.Entry<String, String> property : properties.entrySet()) {
        System.setProperty(property.getKey(), property.getValue());
      }
      getLogger().debug("Configured system properties were successfully set.");
    }
  }

  protected ClassLoader projectClassLoader() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    return Util.createProjectClassLoader(contextClassLoader,
            getProject(),
            outputDirectory,
            null);
  }

  protected CodeGenManagerUtil.Framework discoverFramework() {
    if (hasDependency("quarkus")) {
      return CodeGenManagerUtil.Framework.QUARKUS;
    }

    if (hasDependency("spring")) {
      return CodeGenManagerUtil.Framework.SPRING;
    }

    return CodeGenManagerUtil.Framework.NONE;
  }

  private boolean hasDependency(String dependency) {
    return getProject().getConfigurations().stream().anyMatch(c -> c.getAllDependencies().stream().anyMatch(d -> d.getName().contains(dependency)));
  }

  @Internal
  protected GeneratedFileWriter getGeneratedFileWriter() {
    return GENERATED_FILE_WRITER_BUILDER.build(Path.of(baseDir.getAbsolutePath()));
  }

  public File getProjectBaseDir() {
    return projectBaseDir;
  }

  public Map<String, String> getProperties() {
    return properties;
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

  public Boolean getPersistence() {
    return persistence;
  }

  public Boolean getGenerateRules() {
    return generateRules;
  }

  public Boolean getGenerateProcesses() {
    return generateProcesses;
  }

  public Boolean getGenerateDecisions() {
    return generateDecisions;
  }

  public Boolean getGeneratePredictions() {
    return generatePredictions;
  }
}
