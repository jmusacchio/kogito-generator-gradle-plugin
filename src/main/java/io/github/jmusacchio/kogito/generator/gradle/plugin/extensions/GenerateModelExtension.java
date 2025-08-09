package io.github.jmusacchio.kogito.generator.gradle.plugin.extensions;

import static org.kie.kogito.codegen.json.JsonSchemaGenerator.DEFAULT_SCHEMA_VERSION;

public class GenerateModelExtension {

  private boolean generatePartial;

  private boolean onDemand;

  private boolean keepSources;

  private String schemaVersion;

  private String compilerSourceJavaVersion;

  private String compilerTargetJavaVersion;

  public GenerateModelExtension() {
    schemaVersion = DEFAULT_SCHEMA_VERSION.name();
    compilerSourceJavaVersion = "17";
    compilerTargetJavaVersion = "17";
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

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public String getCompilerSourceJavaVersion() {
    return compilerSourceJavaVersion;
  }

  public void setCompilerSourceJavaVersion(String compilerSourceJavaVersion) {
    this.compilerSourceJavaVersion = compilerSourceJavaVersion;
  }

  public String getCompilerTargetJavaVersion() {
    return compilerTargetJavaVersion;
  }

  public void setCompilerTargetJavaVersion(String compilerTargetJavaVersion) {
    this.compilerTargetJavaVersion = compilerTargetJavaVersion;
  }
}
