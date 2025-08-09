package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import org.gradle.api.tasks.TaskAction;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.GenerateModelExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;

import javax.inject.Inject;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.projectSourceDirectory;

public class ScaffoldTask extends GenerateModelTask {

  @Inject
  public ScaffoldTask(KogitoExtension extension, GenerateModelExtension modelExtension) {
    super(extension, modelExtension);
    setOutputDirectory(getProject().getProjectDir());
    setBaseDir(
        projectSourceDirectory(this.getProject())
            .getSrcDirs()
            .stream()
            .findFirst()
            .orElse(getBaseDir())
    );
    setOnDemand(true);
  }

  @TaskAction
  @Override
  public void execute() {
    addCompileSourceRoots();
    ClassLoader projectClassLoader = projectClassLoader();
    KogitoBuildContext kogitoBuildContext = getKogitoBuildContext(projectClassLoader);
    generateModel(kogitoBuildContext);
  }
}
