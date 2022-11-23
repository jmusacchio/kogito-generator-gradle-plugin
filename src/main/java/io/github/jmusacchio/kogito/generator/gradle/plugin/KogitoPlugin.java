package io.github.jmusacchio.kogito.generator.gradle.plugin;

import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.ProcessClassesExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.GenerateModelExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.KogitoCompileTask;
import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.GenerateModelTask;
import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.ProcessClassesTask;
import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.ScaffoldTask;

public class KogitoPlugin implements Plugin<Project> {

  static final String GROUP = "kogito generator";

  static final String KOGITO_EXTENSION = "kogito";

  static final String GENERATE_MODEL_TASK = "kogitoGenerateModel";

  static final String PROCESS_CLASSES_TASK = "kogitoProcessClasses";

  static final String COMPILE_KOGITO_TASK = "compileKogito";

  static final String SCAFFOLD_TASK = "kogitoScaffold";

  @Override
  public void apply(Project project) {
    project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
      KogitoExtension extension = project.getExtensions().create(KOGITO_EXTENSION, KogitoExtension.class, project);
      GenerateModelExtension generateModelExtension = project.getExtensions().create(GENERATE_MODEL_TASK, GenerateModelExtension.class, project);
      ProcessClassesExtension processClassesExtension = project.getExtensions().create(PROCESS_CLASSES_TASK, ProcessClassesExtension.class);

      Task compile = project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME).getOrNull();

      if (compile != null) {
        project.getTasks().register(GENERATE_MODEL_TASK, GenerateModelTask.class, extension, generateModelExtension).configure(task -> {
          task.setGroup(GROUP);
          task.setDescription("Generates code via Kogito Application Generator API for supported kogito specification documents like bpmn, dmn, drl, etc");
          task.dependsOn(compile);
        });

        project.getTasks().register(SCAFFOLD_TASK, ScaffoldTask.class, extension, generateModelExtension).configure(task -> {
          task.setGroup(GROUP);
          task.setDescription("Similar to kogitoGenerateModel task but placing generated java classes on project main java source directory");
          task.dependsOn(compile);
        });

        project.getTasks().register(PROCESS_CLASSES_TASK, ProcessClassesTask.class, extension, processClassesExtension, compile).configure(task -> {
          task.setGroup(GROUP);
          task.setDescription("Generates persistence code via Kogito Persistence Generator API for supported kogito persistence types like jdbc, mongodb, kafka, etc");
        });

        project.getTasks().register(COMPILE_KOGITO_TASK, KogitoCompileTask.class, extension, compile).configure(task -> {
          task.setGroup(GROUP);
          task.setDescription("Compiles kogito generated code");
        });

        Task generateModel = project.getTasks().findByPath(GENERATE_MODEL_TASK);
        Task processClasses = project.getTasks().findByPath(PROCESS_CLASSES_TASK);
        Task compileGenerate = project.getTasks().findByPath(COMPILE_KOGITO_TASK);

        processClasses.dependsOn(compileGenerate);

        compileGenerate.finalizedBy(processClasses);

        project.afterEvaluate(p -> {
          if (extension.isAutoBuild()) {
            compile.finalizedBy(generateModel);
            generateModel.finalizedBy(compileGenerate);

            project.getTasks().named(JavaPlugin.CLASSES_TASK_NAME).configure(classes ->
              classes.mustRunAfter(compileGenerate)
            );
          }
        });
      }
    });
  }
}
