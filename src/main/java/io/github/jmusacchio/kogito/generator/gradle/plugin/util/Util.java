package io.github.jmusacchio.kogito.generator.gradle.plugin.util;

import org.drools.codegen.common.GeneratedFileWriter;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSetContainer;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.util.maven.support.ReleaseIdImpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.drools.compiler.kie.builder.impl.KieBuilderImpl.setDefaultsforEmptyKieModule;

public final class Util {
  private static final GeneratedFileWriter.Builder generatedFileWriterBuilder = GeneratedFileWriter.builder("kogito", "kogito.codegen.resources.directory", "kogito.codegen.sources.directory");

  public static GeneratedFileWriter getGeneratedFileWriter(File baseDir) {
    return generatedFileWriterBuilder
            .build(Path.of(baseDir.getAbsolutePath()));
  }

  public static List<URL> getProjectFiles(
      final Project project,
      final List<InternalKieModule> kmoduleDeps
  ) throws IOException {
    final List<URL> urls = classpathUrls(project);

    final List<File> artifacts = mainBuilds(project.getBuildDir(), file -> file);

    for (final File artifact : artifacts) {
      if (artifact.getName().endsWith(".jar")) {
        populateURLsFromJarArtifact(project, artifact, kmoduleDeps);
      }
    }

    return urls;
  }

  public static ClassLoader createProjectClassLoader(
      final ClassLoader parentClassLoader,
      final Project project,
      final File outputDirectory,
      final List<InternalKieModule> kmoduleDeps
  ) {
    try {
      final List<URL> urls = getProjectFiles(project, kmoduleDeps);
      urls.add(outputDirectory.toURI().toURL());
      return URLClassLoader.newInstance(urls.toArray(new URL[0]), parentClassLoader);
    } catch (IOException var5) {
      throw new RuntimeException("Error setting up Kie ClassLoader", var5);
    }
  }

  public static SourceDirectorySet projectSourceDirectory(Project project) {
    SourceSetContainer sourceSetContainer = (SourceSetContainer) project.getProperties().get("sourceSets");
    return sourceSetContainer.getByName("main").getAllJava();
  }

  private static void populateURLsFromJarArtifact(
      final Project project,
      final File file,
      final List<InternalKieModule> kmoduleDeps
  ) throws IOException {
    if (file != null && file.isFile()) {
      final KieModuleModel depModel = getDependencyKieModel(file);
      if (kmoduleDeps != null && depModel != null) {
        final ReleaseId releaseId = new ReleaseIdImpl(project.getGroup().toString(), project.getName(), project.getVersion().toString());
        kmoduleDeps.add(new ZipKieModule(releaseId, depModel, file));
      }
    }
  }

  private static KieModuleModel getDependencyKieModel(final File jar) throws IOException {
    try (final ZipFile zipFile = new ZipFile(jar)) {
      final ZipEntry zipEntry = zipFile.getEntry(KieModuleModelImpl.KMODULE_JAR_PATH.asString());
      if (zipEntry != null) {
        final KieModuleModel kieModuleModel = KieModuleModelImpl.fromXML(zipFile.getInputStream(zipEntry));
        setDefaultsforEmptyKieModule(kieModuleModel);
        return kieModuleModel;
      }
    }
    return null;
  }

  public static List<File> classpathFiles(final Project project) {
    return classpath(project, file -> file);
  }

  public static List<URL> classpathUrls(final Project project) {
    return classpath(project, file -> {
      try {
        return file.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static <T> List<T> classpath(final Project project, final Function<File, T> fn) {
    return Stream.concat(
      Stream.concat(
        mainBuilds(project.getBuildDir(), fn).stream(),
        project.getRootProject()
          .getSubprojects()
          .stream()
          .flatMap(s -> mainBuilds(s.getBuildDir(), fn).stream())
      ),
      project.getConfigurations()
        .getByName("compileClasspath")
        .getFiles()
        .stream()
        .filter(f -> !f.getName().endsWith(".pom"))
        .map(f -> fn.apply(f))
    ).collect(Collectors.toList());
  }

  private static <T> List<T> mainBuilds(final File buildDir, final Function<File, T> fn) {
    if (buildDir.getName().equals("main")) {
      return asList(fn.apply(buildDir));
    } else if (buildDir.listFiles() != null) {
      return Arrays.stream(buildDir.listFiles()).flatMap(d -> mainBuilds(d, fn).stream()).collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  private Util() {
  }
}
