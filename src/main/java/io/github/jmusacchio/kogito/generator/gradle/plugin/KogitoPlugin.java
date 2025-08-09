package io.github.jmusacchio.kogito.generator.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.GenerateModelExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.GenerateModelTask;
import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.ScaffoldTask;

public class KogitoPlugin implements Plugin<Project> {

  static final String GROUP = "kogito generator";

  static final String KOGITO_EXTENSION = "kogito";

  static final String GENERATE_MODEL_TASK = "kogitoGenerateModel";

  static final String SCAFFOLD_TASK = "kogitoScaffold";

  @Override
  public void apply(Project project) {
    project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
      String property = "org.gradle.appname";

      if (System.getProperty(property) == null) {
        System.setProperty(property, "gradle");
      }

      KogitoExtension extension = project.getExtensions().create(KOGITO_EXTENSION, KogitoExtension.class, project);
      GenerateModelExtension generateModelExtension = project.getExtensions().create(GENERATE_MODEL_TASK, GenerateModelExtension.class);

      project.afterEvaluate(p -> {
        Task javaCompile = project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME).get();

        project.getTasks().register(GENERATE_MODEL_TASK, GenerateModelTask.class, extension, generateModelExtension).configure(task -> {
          task.setGroup(GROUP);
          task.setDescription("Generates code via Kogito Application Generator API for supported kogito specification documents like bpmn, dmn, drl, etc");
          task.dependsOn(javaCompile);
        });

        project.getTasks().register(SCAFFOLD_TASK, ScaffoldTask.class, extension, generateModelExtension).configure(task -> {
          task.setGroup(GROUP);
          task.setDescription("Similar to kogitoGenerateModel task but placing generated java classes on project main java source directory");
          task.dependsOn(javaCompile);
        });
      });
    });
    project.getPlugins()
    .withType(BasePlugin.class, basePlugin ->
      project.afterEvaluate(p -> {
        KogitoExtension extension = project.getExtensions().getByType(KogitoExtension.class);
        if (extension.isAutoBuild()) {
          Task generateModel = project.getTasks().named(GENERATE_MODEL_TASK).get();

          Task assemble = project.getTasks().named(BasePlugin.ASSEMBLE_TASK_NAME).get();

          assemble.dependsOn(generateModel);
        }
      })
    );
  }
}
