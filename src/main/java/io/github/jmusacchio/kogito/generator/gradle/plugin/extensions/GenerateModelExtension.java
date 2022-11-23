package io.github.jmusacchio.kogito.generator.gradle.plugin.extensions;

import org.gradle.api.Project;
import org.kie.kogito.codegen.core.utils.GeneratedFileWriter;

import java.io.File;

public class GenerateModelExtension {

  private File customizableSourcesPath;

  private boolean generatePartial;

  private boolean onDemand;

  private boolean keepSources;

  private String buildOutputDirectory;

  public GenerateModelExtension(Project project) {
    customizableSourcesPath = new File(project.getBuildDir() + "/" + GeneratedFileWriter.DEFAULT_SOURCES_DIR);
    buildOutputDirectory = project.getBuildDir() + "/classes";
  }

  public File getCustomizableSourcesPath() {
    return customizableSourcesPath;
  }

  public void setCustomizableSourcesPath(File customizableSourcesPath) {
    this.customizableSourcesPath = customizableSourcesPath;
  }

  public boolean isGeneratePartial() {
    return generatePartial;
  }

  public void setGeneratePartial(boolean generatePartial) {
    this.generatePartial = generatePartial;
  }

  public boolean isOnDemand() {
    return onDemand;
  }

  public void setOnDemand(boolean onDemand) {
    this.onDemand = onDemand;
  }

  public boolean isKeepSources() {
    return keepSources;
  }

  public void setKeepSources(boolean keepSources) {
    this.keepSources = keepSources;
  }

  public String getBuildOutputDirectory() {
    return buildOutputDirectory;
  }

  public void setBuildOutputDirectory(String buildOutputDirectory) {
    this.buildOutputDirectory = buildOutputDirectory;
  }
}
