package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import org.gradle.api.tasks.TaskAction;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.GenerateModelExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;;

import javax.inject.Inject;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.projectSourceDirectory;

public class ScaffoldTask extends GenerateModelTask {

  @Inject
  public ScaffoldTask(KogitoExtension extension, GenerateModelExtension modelExtension) {
    super(extension, modelExtension);
    setOutputDirectory(getProject().getProjectDir());
    setGeneratedSources(
        projectSourceDirectory(this.getProject())
            .getSrcDirs()
            .stream()
            .findFirst()
            .orElse(getGeneratedSources())
    );
    setOnDemand(true);
  }

  @TaskAction
  @Override
  public void execute() {
    addCompileSourceRoots();
    generateModel();
  }
}
