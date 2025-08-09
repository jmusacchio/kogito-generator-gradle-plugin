package io.github.jmusacchio.kogito.generator.gradle.plugin;

import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.GenerateModelTask;
import io.github.jmusacchio.kogito.generator.gradle.plugin.tasks.ScaffoldTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.KogitoPlugin.GENERATE_MODEL_TASK;
import static io.github.jmusacchio.kogito.generator.gradle.plugin.KogitoPlugin.SCAFFOLD_TASK;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class KogitoPluginTest {

    @Test
    public void kogitoPluginAddsKogitoTaskToProject() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(KogitoPlugin.class);
        assertInstanceOf(GenerateModelTask.class, project.getTasksByName(GENERATE_MODEL_TASK, false).iterator().next());
        assertInstanceOf(ScaffoldTask.class, project.getTasksByName(SCAFFOLD_TASK, false).iterator().next());
    }
}