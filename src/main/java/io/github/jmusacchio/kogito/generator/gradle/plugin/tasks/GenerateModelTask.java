package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.GenerateModelExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util;
import org.drools.codegen.common.GeneratedFile;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.manager.CompilerHelper;
import org.kie.kogito.codegen.manager.GenerateModelHelper;
import org.kie.kogito.codegen.manager.processes.PersistenceGenerationHelper;
import org.kie.kogito.codegen.manager.util.CodeGenManagerUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.classpathFiles;
import static io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util.projectSourceDirectory;
import static org.kie.efesto.common.api.constants.Constants.INDEXFILE_DIRECTORY_PROPERTY;
import static org.kie.kogito.codegen.manager.CompilerHelper.RESOURCES;
import static org.kie.kogito.codegen.manager.CompilerHelper.SOURCES;
import static org.kie.kogito.codegen.manager.util.CodeGenManagerUtil.discoverKogitoRuntimeContext;

@CacheableTask
public class GenerateModelTask extends AbstractKieTask {

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generatePartial;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean onDemand;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean keepSources;

  @org.gradle.api.tasks.Optional
  @Input
  private String schemaVersion;

  @Input
  private String compilerSourceJavaVersion;

  @Input
  private String compilerTargetJavaVersion;

  @Inject
  public GenerateModelTask(KogitoExtension extension, GenerateModelExtension modelExtension) {
    super(extension);
    this.generatePartial = modelExtension.isGeneratePartial();
    this.onDemand = modelExtension.isOnDemand();
    this.keepSources = modelExtension.isKeepSources();
    this.schemaVersion = modelExtension.getSchemaVersion();
    this.compilerSourceJavaVersion = modelExtension.getCompilerSourceJavaVersion();
    this.compilerTargetJavaVersion = modelExtension.getCompilerTargetJavaVersion();
  }

  @TaskAction
  public void execute() {
    getLogger().debug("Compiler Target Java Version:" + compilerTargetJavaVersion);
    getLogger().debug("Compiler Source Java Version:" + compilerSourceJavaVersion);
    getLogger().debug("Compiler Source Encoding:" + projectSourceEncoding);
    getLogger().debug("Targeting directory: " + getOutputDirectory());

    boolean indexFileDirectorySet = false;
    if (getOutputDirectory() == null) {
      throw new GradleException("${project.build.directory} is null");
    }
    if (System.getProperty(INDEXFILE_DIRECTORY_PROPERTY) == null) {
      System.setProperty(INDEXFILE_DIRECTORY_PROPERTY, getOutputDirectory().toString());
      indexFileDirectorySet = true;
    }
    addCompileSourceRoots();
    Map<String, Collection<GeneratedFile>> generatedModelFiles;
    ClassLoader projectClassLoader = projectClassLoader();
    KogitoBuildContext kogitoBuildContext = getKogitoBuildContext(projectClassLoader);
    if (isOnDemand()) {
      getLogger().info("On-Demand Mode is On. Use mvn compile kogito:scaffold");
      generatedModelFiles = new HashMap<>();
    } else {
      generatedModelFiles = generateModel(kogitoBuildContext);
    }
    if (indexFileDirectorySet) {
      System.clearProperty(INDEXFILE_DIRECTORY_PROPERTY);
    }

    // Compile and write model files
    compileAndDump(generatedModelFiles, projectClassLoader);

    Map<String, Collection<GeneratedFile>> generatedPersistenceFiles = generatePersistence(kogitoBuildContext, projectClassLoader);

    compileAndDump(generatedPersistenceFiles, projectClassLoader);

    if (!keepSources) {
      CodeGenManagerUtil.deleteDrlFiles(getOutputDirectory().toPath());
    }
  }

  KogitoBuildContext getKogitoBuildContext(ClassLoader projectClassLoader) {
    return discoverKogitoRuntimeContext(projectClassLoader,
            getProjectBaseDir().toPath(),
            new KogitoGAV(getProject().getGroup().toString(),
                    getProject().getName(),
                    getProject().getVersion().toString()),
            new CodeGenManagerUtil.ProjectParameters(discoverFramework(),
                    Boolean.toString(getGenerateDecisions()),
                    Boolean.toString(getGeneratePredictions()),
                    Boolean.toString(getGenerateProcesses()),
                    Boolean.toString(getGenerateRules()),
                    getPersistence()),
            className -> Util.hasClassOnClasspath(getProject(), className));
  }

  protected Boolean isOnDemand() {
    return onDemand;
  }

  protected void addCompileSourceRoots() {
    projectSourceDirectory(this.getProject()).srcDirs(getGeneratedFileWriter().getScaffoldedSourcesDir());
  }

  protected Map<String, Collection<GeneratedFile>> generateModel(KogitoBuildContext kogitoBuildContext) {
    setSystemProperties(getProperties());
    return GenerateModelHelper.generateModelFiles(kogitoBuildContext, generatePartial);
  }

  protected Map<String, Collection<GeneratedFile>> generatePersistence(KogitoBuildContext kogitoBuildContext, ClassLoader projectClassloader) {
    return PersistenceGenerationHelper.generatePersistenceFiles(kogitoBuildContext, projectClassloader, schemaVersion);
  }

  protected void compileAndDump(Map<String, Collection<GeneratedFile>> generatedFiles, ClassLoader classloader) {
    try {
      // Compile and write files
      CompilerHelper.compileAndDumpGeneratedSources(generatedFiles.get(SOURCES),
              classloader,
              classpathFiles(getProject()).stream().map(File::getAbsolutePath).collect(Collectors.toList()),
              getBaseDir(),
              projectSourceEncoding,
              compilerSourceJavaVersion,
              compilerTargetJavaVersion);
      // Dump resources
      CompilerHelper.dumpResources(generatedFiles.get(RESOURCES), getBaseDir());
    } catch (Exception e) {
      throw new GradleException("Error during processing model classes: " + e.getMessage(), e);
    }
  }

  public Boolean getGeneratePartial() {
    return generatePartial;
  }

  public void setOnDemand(Boolean onDemand) {
    this.onDemand = onDemand;
  }

  public Boolean getKeepSources() {
    return keepSources;
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public String getCompilerSourceJavaVersion() {
    return compilerSourceJavaVersion;
  }

  public String getCompilerTargetJavaVersion() {
    return compilerTargetJavaVersion;
  }
}