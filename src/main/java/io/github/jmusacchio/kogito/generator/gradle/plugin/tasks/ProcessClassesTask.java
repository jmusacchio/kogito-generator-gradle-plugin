package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.ProcessClassesExtension;
import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.util.PortablePath;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.kie.kogito.Model;
import org.kie.kogito.ProcessInput;
import org.kie.kogito.UserTask;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.json.JsonSchemaGenerator;
import org.kie.kogito.codegen.process.persistence.PersistenceGenerator;
import org.kie.kogito.codegen.process.persistence.marshaller.ReflectionMarshallerGenerator;
import org.kie.kogito.codegen.process.persistence.proto.ReflectionProtoGenerator;
import org.kie.memorycompiler.CompilationResult;
import org.kie.memorycompiler.JavaCompiler;
import org.kie.memorycompiler.JavaCompilerFactory;
import org.kie.memorycompiler.JavaCompilerSettings;
import org.kie.memorycompiler.JavaConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.classpathFiles;
import static java.util.Arrays.asList;
import static org.kie.kogito.codegen.core.utils.GeneratedFileValidation.validateGeneratedFileTypes;

@CacheableTask
public class ProcessClassesTask extends AbstractKieTask {
  private static final JavaCompiler JAVA_COMPILER = JavaCompilerFactory.loadCompiler(JavaConfiguration.CompilerType.NATIVE, "1.8");

  @org.gradle.api.tasks.Optional
  @Input
  private String schemaVersion;

  @Inject
  public ProcessClassesTask(KogitoExtension extension, ProcessClassesExtension processClassesExtension, AbstractCompile compile) {
    super(extension);
    this.schemaVersion = processClassesExtension.getSchemaVersion();
    setOutputDirectory(compile.getDestinationDirectory().get().getAsFile());
  }

  @TaskAction
  public void execute() {
    try {
      JavaCompilerSettings settings = new JavaCompilerSettings();

      classpathFiles(getProject())
          .stream()
          .forEach(file -> settings.addClasspath(file));

      @SuppressWarnings({ "rawtype", "unchecked" })
      Set<Class<?>> modelClasses = (Set) getReflections().getSubTypesOf(Model.class);

      ReflectionProtoGenerator protoGenerator = ReflectionProtoGenerator.builder()
          .build(modelClasses);

      ClassLoader classLoader = projectClassLoader();
      KogitoBuildContext context = discoverKogitoRuntimeContext(classLoader);

      // Generate persistence files
      PersistenceGenerator persistenceGenerator = new PersistenceGenerator(context, protoGenerator, new ReflectionMarshallerGenerator(context, protoGenerator.getDataClasses()));
      Collection<GeneratedFile> persistenceFiles = persistenceGenerator.generate();

      validateGeneratedFileTypes(persistenceFiles, asList(GeneratedFileType.Category.SOURCE, GeneratedFileType.Category.INTERNAL_RESOURCE, GeneratedFileType.Category.STATIC_HTTP_RESOURCE));

      Collection<GeneratedFile> generatedClasses = persistenceFiles.stream().filter(x -> x.category().equals(GeneratedFileType.Category.SOURCE)).collect(Collectors.toList());
      Collection<GeneratedFile> generatedResources = persistenceFiles.stream()
          .filter(x -> x.category().equals(GeneratedFileType.Category.INTERNAL_RESOURCE) || x.category().equals(GeneratedFileType.Category.STATIC_HTTP_RESOURCE))
          .collect(Collectors.toList());

      // Compile and write persistence files
      compileAndWriteClasses(generatedClasses, classLoader, settings);

      // Dump resources
      this.writeGeneratedFiles(generatedResources);

      // Json schema generation
      Stream<Class<?>> processClassStream = getReflections().getTypesAnnotatedWith(ProcessInput.class).stream();
      writeGeneratedFiles(generateJsonSchema(processClassStream));

      Stream<Class<?>> userTaskClassStream = getReflections().getTypesAnnotatedWith(UserTask.class).stream();
      writeGeneratedFiles(generateJsonSchema(userTaskClassStream));
    } catch (Exception var12) {
      throw new RuntimeException("Error during processing model classes", var12);
    }
  }

  private void compileAndWriteClasses(Collection<GeneratedFile> generatedClasses, ClassLoader cl, JavaCompilerSettings settings) {
    MemoryFileSystem srcMfs = new MemoryFileSystem();
    MemoryFileSystem trgMfs = new MemoryFileSystem();

    String[] sources = new String[generatedClasses.size()];
    int index = 0;
    for (GeneratedFile entry : generatedClasses) {
      String fileName = entry.relativePath();
      sources[index++] = fileName;
      srcMfs.write(fileName, entry.contents());
    }

    if (sources.length > 0) {
      CompilationResult result = JAVA_COMPILER.compile(sources, srcMfs, trgMfs, cl, settings);
      if (result.getErrors().length > 0) {
        throw new RuntimeException(Arrays.toString(result.getErrors()));
      }

      for (PortablePath path : trgMfs.getFilePaths()) {
        byte[] data = trgMfs.getBytes(path);
        writeGeneratedFile(new GeneratedFile(GeneratedFileType.COMPILED_CLASS, path.asString(), data));
      }
    }
  }

  private Collection<GeneratedFile> generateJsonSchema(Stream<Class<?>> classes) throws IOException {
    return new JsonSchemaGenerator.ClassBuilder(classes)
        .withSchemaVersion(getSchemaVersion()).build()
        .generate();
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }
}
