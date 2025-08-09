package io.github.jmusacchio.kogito.generator.gradle.plugin;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.KogitoPlugin.GENERATE_MODEL_TASK;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KogitoPluginFunctionalTest {

    @TempDir
    private File projectTestDir;
    private final File projectDir = new File("src/test/resources/acceptance-project");
    private GradleRunner gradleRunner = null;

    private static final String QUARKUS_PROJECT = ":process-decisions-quarkus";
    private static final String SPRING_BOOT_PROJECT = ":process-decisions-springboot";
    private static final String EMBEDDED_PROJECT = ":dmn-embedded-mode-example";

    @BeforeEach
    public void createTemporaryAcceptanceProjectFromTemplate() throws IOException {
        copyDirectory(
            this.projectDir.getPath(),
            this.projectTestDir.getPath()
        );
        gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(projectTestDir);
    }

    @Test
    public void springBootDefaultBuild() {
        BuildResult result = gradleRunner.withArguments(SPRING_BOOT_PROJECT + ":build").build();
        assertEquals(SUCCESS, result.task(SPRING_BOOT_PROJECT + ":" + GENERATE_MODEL_TASK).getOutcome());
        assertEquals(SUCCESS, result.task(SPRING_BOOT_PROJECT + ":" + TEST_TASK_NAME).getOutcome());
    }

    @Test
    public void springBootNoAutoBuild() {
        BuildResult result = gradleRunner.withArguments("-PautoBuild=false", SPRING_BOOT_PROJECT + ":build").buildAndFail();
        assertNull(result.task(SPRING_BOOT_PROJECT + ":" + GENERATE_MODEL_TASK));
        assertEquals(FAILED, result.task(SPRING_BOOT_PROJECT + ":" + TEST_TASK_NAME).getOutcome());
    }

    @Test
    public void quarkusDefaultBuild() {
        BuildResult result = gradleRunner.withArguments(QUARKUS_PROJECT + ":build").build();
        assertEquals(SUCCESS, result.task(QUARKUS_PROJECT + ":" + GENERATE_MODEL_TASK).getOutcome());
        assertEquals(SUCCESS, result.task(QUARKUS_PROJECT + ":" + TEST_TASK_NAME).getOutcome());
    }

    @Test
    public void quarkusNoAutoBuild() {
        BuildResult result = gradleRunner.withArguments("-PautoBuild=false", QUARKUS_PROJECT + ":build").buildAndFail();
        assertNull(result.task(QUARKUS_PROJECT + ":" + GENERATE_MODEL_TASK));
        assertEquals(FAILED, result.task(QUARKUS_PROJECT + ":" + TEST_TASK_NAME).getOutcome());
    }

    @Test
    public void embeddedDefaultBuild() {
        BuildResult result = gradleRunner.withArguments(EMBEDDED_PROJECT + ":build").build();
        assertEquals(SUCCESS, result.task(EMBEDDED_PROJECT + ":" + GENERATE_MODEL_TASK).getOutcome());
        assertEquals(NO_SOURCE, result.task(EMBEDDED_PROJECT + ":" + TEST_TASK_NAME).getOutcome());
    }

    @Test
    public void embeddedNoAutoBuild() {
        BuildResult result = gradleRunner.withArguments("-PautoBuild=false", EMBEDDED_PROJECT + ":build").build();
        assertNull(result.task(EMBEDDED_PROJECT + ":" + GENERATE_MODEL_TASK));
        assertEquals(NO_SOURCE, result.task(EMBEDDED_PROJECT + ":" + TEST_TASK_NAME).getOutcome());
    }

    private static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
            throws IOException {
        var path = Paths.get(sourceDirectoryLocation);
        var parent = path.getFileName().toString();
        try (var file = Files.walk(path)) {
            file.forEach(source -> {
                var name = source.toString();
                var destination = Paths.get(
                    destinationDirectoryLocation,
                    name.substring(name.indexOf(parent) + parent.length())
                );
                try {
                    Files.copy(source, destination, REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
