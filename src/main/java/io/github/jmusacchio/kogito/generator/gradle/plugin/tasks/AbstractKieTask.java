package io.github.jmusacchio.kogito.generator.gradle.plugin.tasks;

import io.github.jmusacchio.kogito.generator.gradle.plugin.util.Util;
import org.drools.codegen.common.AppPaths;
import org.drools.codegen.common.DroolsModelBuildContext;
import org.drools.codegen.common.GeneratedFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.SpringBootKogitoBuildContext;
import org.kie.kogito.codegen.core.utils.GeneratedFileWriter;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.prediction.PredictionCodegen;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.process.persistence.PersistenceGenerator;
import org.kie.kogito.codegen.rules.RuleCodegen;
import io.github.jmusacchio.kogito.generator.gradle.plugin.extensions.KogitoExtension;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractKieTask extends DefaultTask {

  @org.gradle.api.tasks.Optional
  @Input
  private Map<String, String> properties;

  @InputFiles
  @CompileClasspath
  private File outputDirectory;

  @org.gradle.api.tasks.Optional
  @InputFiles
  @CompileClasspath
  private File generatedSources;

  @org.gradle.api.tasks.Optional
  @InputFiles
  @CompileClasspath
  private File generatedResources;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean persistence;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generateRules;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generateProcesses;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generateDecisions;

  @org.gradle.api.tasks.Optional
  @Input
  private Boolean generatePredictions;

  @Internal
  private File projectDir;

  @Internal
  private Reflections reflections;

  public AbstractKieTask(KogitoExtension extension) {
    this.projectDir = extension.getProjectDir();
    this.properties = extension.getProperties();
    this.outputDirectory = extension.getOutputDirectory();
    this.generatedSources = extension.getGeneratedSources();
    this.generatedResources = extension.getGeneratedResources();
    this.persistence = extension.isPersistence();
    this.generateRules = extension.isGenerateRules();
    this.generateProcesses = extension.isGenerateProcesses();
    this.generateDecisions = extension.isGenerateDecisions();
    this.generatePredictions = extension.isGeneratePredictions();
  }

  protected void setSystemProperties(Map<String, String> properties) {
    if (properties != null) {
      this.getLogger().debug("Additional system properties: " + properties);
      Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();

      while(iterator.hasNext()) {
        Map.Entry<String, String> property = iterator.next();
        System.setProperty(property.getKey(), property.getValue());
      }

      this.getLogger().debug("Configured system properties were successfully set.");
    }
  }

  protected KogitoBuildContext discoverKogitoRuntimeContext(ClassLoader classLoader) {
    AppPaths appPaths = AppPaths.fromProjectDir(
        getProjectDir().toPath(),
        getOutputDirectory().toPath()
    );
    KogitoBuildContext context = this.contextBuilder()
        .withClassAvailabilityResolver(this::hasClassOnClasspath)
        .withClassSubTypeAvailabilityResolver(this.classSubTypeAvailabilityResolver())
        .withApplicationProperties(appPaths.getResourceFiles())
        .withPackageName(this.appPackageName()).withClassLoader(classLoader)
        .withAppPaths(appPaths)
        .withGAV(new KogitoGAV(
            getProject().getGroup().toString(),
            getProject().getName(),
            getProject().getVersion().toString()
        ))
        .build();
    this.additionalProperties(context);
    return context;
  }

  public Reflections getReflections() {
    if (this.reflections == null) {
      URLClassLoader classLoader = (URLClassLoader)this.projectClassLoader();
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addUrls(classLoader.getURLs());
      builder.addClassLoaders(classLoader);
      builder.setExpandSuperTypes(false);
      this.reflections = new Reflections(builder);
    }

    return this.reflections;
  }

  protected Predicate<Class<?>> classSubTypeAvailabilityResolver() {
    return (clazz) ->
      this.getReflections().getSubTypesOf(clazz).stream().anyMatch((c) ->
        !c.isInterface() && !Modifier.isAbstract(c.getModifiers())
      );
  }

  protected ClassLoader projectClassLoader() {
    return Util.createProjectClassLoader(
        this.getClass().getClassLoader(),
        getProject(),
        getOutputDirectory(),
        null
    );
  }

  protected String appPackageName() {
    return DroolsModelBuildContext.DEFAULT_PACKAGE_NAME;
  }

  private void additionalProperties(KogitoBuildContext context) {
    this.classToCheckForREST().ifPresent((restClass) -> {
      if (!context.hasClassAvailable(restClass)) {
        this.getLogger().info("Disabling REST generation because class '" + restClass + "' is not available");
        context.setApplicationProperty(DroolsModelBuildContext.KOGITO_GENERATE_REST, "false");
      }
    });
    this.classToCheckForDI().ifPresent((diClass) -> {
      if (!context.hasClassAvailable(diClass)) {
        this.getLogger().info("Disabling dependency injection generation because class '" + diClass + "' is not available");
        context.setApplicationProperty(DroolsModelBuildContext.KOGITO_GENERATE_DI, "false");
      }
    });
    context.setApplicationProperty(Generator.CONFIG_PREFIX + RuleCodegen.GENERATOR_NAME, getGenerateRules().toString());
    context.setApplicationProperty(Generator.CONFIG_PREFIX + ProcessCodegen.GENERATOR_NAME, getGenerateProcesses().toString());
    context.setApplicationProperty(Generator.CONFIG_PREFIX + PredictionCodegen.GENERATOR_NAME, getGeneratePredictions().toString());
    context.setApplicationProperty(Generator.CONFIG_PREFIX + DecisionCodegen.GENERATOR_NAME, getGenerateDecisions().toString());
    context.setApplicationProperty(Generator.CONFIG_PREFIX + PersistenceGenerator.GENERATOR_NAME, getPersistence().toString());
  }

  private KogitoBuildContext.Builder contextBuilder() {
    switch (this.discoverFramework()) {
      case QUARKUS:
        return QuarkusKogitoBuildContext.builder();
      case SPRING:
        return SpringBootKogitoBuildContext.builder();
      default:
        return JavaKogitoBuildContext.builder();
    }
  }

  private Optional<String> classToCheckForREST() {
    switch (this.discoverFramework()) {
      case QUARKUS:
        return Optional.of(QuarkusKogitoBuildContext.QUARKUS_REST);
      case SPRING:
        return Optional.of(SpringBootKogitoBuildContext.SPRING_REST);
      default:
        return Optional.empty();
    }
  }

  private Optional<String> classToCheckForDI() {
    switch (this.discoverFramework()) {
      case QUARKUS:
        return Optional.of(QuarkusKogitoBuildContext.QUARKUS_DI);
      case SPRING:
        return Optional.of(SpringBootKogitoBuildContext.SPRING_DI);
      default:
        return Optional.empty();
    }
  }

  private Framework discoverFramework() {
    if (this.hasDependency("quarkus")) {
      return Framework.QUARKUS;
    } else {
      return this.hasDependency("spring") ? Framework.SPRING : Framework.NONE;
    }
  }

  private boolean hasDependency(String dependency) {
    return getProject()
        .getConfigurations()
        .stream().anyMatch(
            c -> c.getAllDependencies().stream().anyMatch(d -> d.getName().contains(dependency))
        );
  }

  private boolean hasClassOnClasspath(String className) {
    try {
      URL[] urls = Util.classpathUrls(getProject()).stream().toArray(URL[]::new);
      try (URLClassLoader cl = new URLClassLoader(urls)) {
        cl.loadClass(className);
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  protected void writeGeneratedFiles(Collection<GeneratedFile> generatedFiles) {
    generatedFiles.forEach(this::writeGeneratedFile);
  }

  protected void writeGeneratedFile(GeneratedFile generatedFile) {
    GeneratedFileWriter writer = new GeneratedFileWriter(
        getOutputDirectory().toPath(),
        getGeneratedSources().toPath(),
        getGeneratedResources().toPath(),
        getGeneratedSources().toPath()
    );
    this.getLogger().info("Generating: " + generatedFile.relativePath());
    writer.write(generatedFile);
  }

  private enum Framework {
    QUARKUS,
    SPRING,
    NONE;

    Framework() {
    }
  }

  public File getProjectDir() {
    return projectDir;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public File getGeneratedSources() {
    return generatedSources;
  }

  public void setGeneratedSources(File generatedSources) {
    this.generatedSources = generatedSources;
  }

  public File getGeneratedResources() {
    return generatedResources;
  }

  public Boolean getPersistence() {
    return persistence;
  }

  public Boolean getGenerateRules() {
    return generateRules;
  }

  public Boolean getGenerateProcesses() {
    return generateProcesses;
  }

  public Boolean getGenerateDecisions() {
    return generateDecisions;
  }

  public Boolean getGeneratePredictions() {
    return generatePredictions;
  }
}
