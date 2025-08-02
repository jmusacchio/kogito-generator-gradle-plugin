package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util;
import org.drools.codegen.common.GeneratedFileWriter;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.inject.Inject;
import java.io.File;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.getGeneratedFileWriter;
import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.projectSourceDirectory;

@CacheableTask
public class KogitoCompileTask extends JavaCompile {

  @Internal
  private File baseDir;

  @Inject
  public KogitoCompileTask(KogitoExtension extension, AbstractCompile compile) {
    super();
    this.baseDir = extension.getBaseDir();
    GeneratedFileWriter generatedFiles = getGeneratedFileWriter(getBaseDir());
    source(
        generatedFiles.getScaffoldedSourcesDir().toFile(), projectSourceDirectory(this.getProject())
        .getSrcDirs()
        .stream()
        .findFirst()
        .orElse(getBaseDir()));
    setClasspath(compile.getClasspath());
    getDestinationDirectory().set(compile.getDestinationDirectory());
  }

  @Override
  public FileCollection getClasspath() {
    return getServices().get(FileCollectionFactory.class).fixed(Util.classpathFiles(getProject()));
  }

  public File getBaseDir() {
    return baseDir;
  }
}
