package org.platonos.springfoxnative.mavenplugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.platonos.test.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class SpringfoxMojoTest {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private org.platonos.springfoxnative.mavenplugin.SpringfoxMojo springfoxMojo;

    private MavenProject mavenProjectMock;

    @BeforeEach
    void setup() {
        mavenProjectMock = new MavenProject();

        springfoxMojo = new org.platonos.springfoxnative.mavenplugin.SpringfoxMojo();
        ReflectionTestUtils.setField(
                springfoxMojo,
                "project",
                mavenProjectMock
        );
    }

    private Set<Artifact> getArtifacts() {
        return getClassPath().stream()
                .map(entry -> createArtifact(new File(entry)))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Artifact createArtifact(final File file) {
        final File pomFile = new File(file.getAbsolutePath().replace(".jar", ".pom"));

        if (!pomFile.exists()) {
            return null;
        }

        try {
            final DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final Document doc = builder.parse(pomFile);
            final Element docElement = doc.getDocumentElement();

            final String groupId = getChildByName(docElement, "groupId", "parent.groupId").getTextContent();
            final String artifactId = getChildByName(docElement, "artifactId", "parent.artifactId").getTextContent();
            final String version = getChildByName(docElement, "version", "parent.version").getTextContent();

            final VersionRange versionRange = VersionRange.createFromVersion(version);

            final DefaultArtifact artifact = new DefaultArtifact(
                    groupId,
                    artifactId,
                    versionRange,
                    null,
                    "jar",
                    null,
                    new DefaultArtifactHandler()
            );
            artifact.setFile(file);
            return artifact;
        } catch (Exception e) {
            return null;
        }
    }

    private Node getChildByName(final Node parent, final String name, final String alternativeName) {
        final String[] names = name.split("\\.");
        Node childNode = getChildByName(parent, names, 0);

        if (childNode != null) {
            return childNode;
        } else {
            return getChildByName(parent, alternativeName);
        }
    }

    private Node getChildByName(final Node parent, final String name) {
        final String[] names = name.split("\\.");
        return getChildByName(parent, names, 0);
    }

    private Node getChildByName(final Node parent, final String[] names, final int index) {
        final String name = names[index];

        final NodeList childs = parent.getChildNodes();

        Node foundChild = null;

        for (int i = 0; i < childs.getLength(); i++) {
            final Node child = childs.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().endsWith(name)) {
                foundChild = child;
            }
        }

        if (foundChild != null && index + 1 < names.length) {
            return getChildByName(foundChild, names, index + 1);
        } else {
            return foundChild;
        }
    }

    private List<String> getClassPath() {
        Properties properties = System.getProperties();
        String pathSeparator = File.pathSeparator;
        String classPathStr = (String) properties.get("java.class.path");
        return Arrays.asList(classPathStr.split(pathSeparator));
    }

    @Test
    void testExecute() throws MojoExecutionException, MojoFailureException {
        mavenProjectMock.setDependencyArtifacts(getArtifacts());
        springfoxMojo.execute();
    }
}