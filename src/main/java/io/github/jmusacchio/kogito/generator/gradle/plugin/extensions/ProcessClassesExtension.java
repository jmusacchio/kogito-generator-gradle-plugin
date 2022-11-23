package io.github.jmusacchio.kogito.generator.gradle.plugin.extensions;

import static org.kie.kogito.codegen.json.JsonSchemaGenerator.DEFAULT_SCHEMA_VERSION;

public class ProcessClassesExtension {

  private String schemaVersion;

  public ProcessClassesExtension() {
    schemaVersion = DEFAULT_SCHEMA_VERSION.name();
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }
}
