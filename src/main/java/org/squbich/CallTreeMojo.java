package org.squbich;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.squbich.calltree.filter.FilterConfiguration;
import org.squbich.calltree.resolver.CompiledAggregate;
import org.squbich.calltree.resolver.SourceAggregate;
import org.squbich.calltree.resolver.SourceResolver;
import org.squbich.calltree.resolver.TypeResolver;
import org.squbich.configuration.OutputConfiguration;
import org.squbich.configuration.PrinterConfiguration;

@Mojo(name = "call-tree", defaultPhase = LifecyclePhase.SITE, requiresDependencyResolution = ResolutionScope.TEST,
        requiresDependencyCollection = ResolutionScope.TEST, requiresProject = true, threadSafe = true)
public class CallTreeMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(required = true)
    private String rootCaller;

    @Parameter(defaultValue = "${project.groupId}", readonly = true)
    private String packageScan;

    @Parameter
    private OutputConfiguration output;

    @Parameter
    private FilterConfiguration filter;

    public void execute() throws MojoExecutionException {
        getLog().debug(packageScan);
        getLog().debug(String.valueOf(output));
        getLog().debug(String.valueOf(filter));

        List<File> sourceFiles = new ArrayList<>();
        List<CompiledAggregate> libFiles = new ArrayList<>();
        project.getArtifacts().forEach(artifact -> {
            String artifactFileName = artifact.getFile().getAbsolutePath();

            String artifactDirectory = artifact.getFile().getParent();
            getLog().debug("artifactDirectory: " + artifactDirectory);
            String sourceJarName = artifactDirectory + "/" + artifact.getArtifactId() + "-" + artifact.getVersion() + "-sources.jar";

            if (artifact.getGroupId().startsWith(project.getGroupId()) && new File(sourceJarName).exists()) {
                getLog().debug("sourceJarName: " + sourceJarName);
                sourceFiles.add(new File(sourceJarName));
            }
            else {
                getLog().debug("libJarName: " + artifactFileName);
                CompiledAggregate aggregate = new CompiledAggregate();
                aggregate.setName(artifactFileName);
                libFiles.add(aggregate);
            }
        });


        SourceResolver sourceResolver = new SourceResolver();
        List<SourceAggregate> sourceAggregates = sourceResolver.solve(sourceFiles);
        TypeResolver typeResolver = new TypeResolver(sourceAggregates, libFiles);


        CallHierarchyPrinter.of(typeResolver, PrinterConfiguration.of(getRootCaller(), getPackageScan(), geOutputConfiguration(), getFilter()))
                .print();

    }

    public OutputConfiguration geOutputConfiguration() {
        if (output == null || (output.getFormat() == null && output.getTarget() == null)) {
            return OutputConfiguration.ofDefault();
        }

        return OutputConfiguration.ofDefaultWhenNull(output.getTarget(), output.getFormat(), output.getElements());
    }

    public MavenProject getProject() {
        return project;
    }

    public MavenSession getSession() {
        return session;
    }

    public String getRootCaller() {
        return rootCaller;
    }

    public String getPackageScan() {
        return packageScan;
    }

    public OutputConfiguration getOutput() {
        return output;
    }

    public FilterConfiguration getFilter() {
        return filter;
    }
}
