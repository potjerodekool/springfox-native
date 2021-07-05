package org.platonos.springfoxnative.mavenplugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.platonos.springfoxnative.shared.SpringFox;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "springfox-native", defaultPhase = LifecyclePhase.COMPILE)
public class SpringfoxMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    private Set<Artifact> getDependencyArtifacts() {
        final Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
        return dependencyArtifacts.stream()
                .filter(artifact -> artifact.getGroupId().startsWith("org.springframework") ||
                        artifact.getGroupId().startsWith("io.springfox")).collect(Collectors.toSet());
    }

    @Override
    public void execute() {
        final List<File> dependencyFiles = getDependencyArtifacts()
                .stream()
                .map(Artifact::getFile)
                .filter(file -> file.getName().endsWith(".jar"))
                .collect(Collectors.toList());

        final SpringFox springFox = new SpringFox();
        springFox.execute(dependencyFiles);
    }

}
