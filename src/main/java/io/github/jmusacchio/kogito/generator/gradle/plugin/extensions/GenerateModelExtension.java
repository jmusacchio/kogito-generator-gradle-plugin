package io.github.jmusacchio.kogito.generator.gradle.plugin.extensions;

import org.gradle.api.Project;

public class GenerateModelExtension {

  private boolean generatePartial;

  private boolean onDemand;

  private boolean keepSources;

  public GenerateModelExtension(Project project) {}

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

}
