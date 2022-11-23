package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.inject.Inject;
import java.io.File;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.projectSourceDirectory;

@CacheableTask
public class KogitoCompileTask extends JavaCompile {

  @org.gradle.api.tasks.Optional
  @InputFiles
  @CompileClasspath
  private File generatedSources;

  @org.gradle.api.tasks.Optional
  @InputFiles
  @CompileClasspath
  private File generatedResources;

  @Inject
  public KogitoCompileTask(KogitoExtension extension, AbstractCompile compile) {
    super();
    this.generatedSources = extension.getGeneratedSources();
    this.generatedResources = extension.getGeneratedResources();
    source(
        getGeneratedSources(), getGeneratedResources(), projectSourceDirectory(this.getProject())
        .getSrcDirs()
        .stream()
        .findFirst()
        .orElse(getGeneratedSources()));
    setClasspath(compile.getClasspath());
    setDestinationDir(compile.getDestinationDir());
  }

  @Override
  public FileCollection getClasspath() {
    return getServices().get(FileCollectionFactory.class).fixed(Util.classpathFiles(getProject()));
  }

  public File getGeneratedSources() {
    return generatedSources;
  }

  public File getGeneratedResources() {
    return generatedResources;
  }
}
