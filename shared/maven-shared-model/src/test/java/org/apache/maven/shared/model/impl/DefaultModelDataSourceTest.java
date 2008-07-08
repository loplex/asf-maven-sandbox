package org.apache.maven.shared.model.impl;

import org.apache.maven.shared.model.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.io.IOException;

public class DefaultModelDataSourceTest {

    private static List<ModelContainerFactory> factories = new ArrayList<ModelContainerFactory>();

    static {
        factories.add(new DummyModelContainerFactory());
    }

    @Test
    public void mergeModelContainers() throws IOException {
        List<ModelProperty> modelProperties = Arrays.asList(
                new ModelProperty("http://apache.org/maven/project", null),
                new ModelProperty("http://apache.org/maven/project/build", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection", null),

                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/version", "2.0.2"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/artifactId", "maven-compiler-plugin"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/groupId", "org.apache.maven.plugins"),

                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/version", "2.0.2"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/artifactId", "maven-compiler-plugin"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/groupId", "org.apache.maven.plugins"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/a", "a")
        );


        DummyModelContainerFactory factory = new DummyModelContainerFactory();

        DefaultModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        List<ModelProperty> mps = datasource.mergeModelContainers(
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(4, 8))),
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(8, 13))));
        assertTrue(mps.containsAll(new ArrayList<ModelProperty>(modelProperties.subList(4, 8))));
    }

    @Test
    public void join1() throws DataSourceException, IOException {
        List<ModelProperty> modelProperties = Arrays.asList(
                new ModelProperty("http://apache.org/maven/project", null),
                new ModelProperty("http://apache.org/maven/project/build", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/version", "2.0.2"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/artifactId", "maven-compiler-plugin"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/groupId", "org.apache.maven.plugins"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin", null),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/version", "2.0.2"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/artifactId", "maven-compiler-plugin"),
                new ModelProperty("http://apache.org/maven/project/build/pluginManagement/plugins#collection/plugin/groupId", "org.apache.maven.plugins"),
                new ModelProperty("http://apache.org/maven/project/version", "2.0.10-SNAPSHOT"),
                new ModelProperty("http://apache.org/maven/project/artifactId", "maven"),
                new ModelProperty("http://apache.org/maven/project/groupId", "org.apache.maven"),
                new ModelProperty("http://apache.org/maven/project/modelVersion", "4.0.0")
        );


        DummyModelContainerFactory factory = new DummyModelContainerFactory();

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);
        ModelContainer joinedModelContainer = datasource.join(
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(4, 8))),
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(8, 12))));

        for (ModelProperty mp : joinedModelContainer.getProperties()) {
            System.out.println("-" + mp);
        }

        if (!datasource.getModelProperties().containsAll(joinedModelContainer.getProperties())) {
            throw new IOException();
        }

        for (ModelProperty mp : datasource.getModelProperties()) {
            System.out.println("+" + mp);
        }

    }


    @Test
    public void query() throws DataSourceException {
        ModelProperty mpA = new ModelProperty("container-marker/a", null);
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("container-marker", null));
        modelProperties.add(mpA);

        modelProperties.add(new ModelProperty("container-marker", null));
        modelProperties.add(new ModelProperty("container-marker/b", null));

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        List<ModelContainer> containers = datasource.queryFor("container-marker");
        assertEquals("Number of containers: ", 2, containers.size());
        assertEquals("Properties for container 'a':", 2, containers.get(0).getProperties().size());
        assertEquals(mpA, containers.get(0).getProperties().get(1));
    }

    @Test
    public void queryWithOneContainerMarker() throws DataSourceException {
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("container-marker", null));

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        List<ModelContainer> containers = datasource.queryFor("container-marker");
        assertEquals("Number of containers: ", 1, containers.size());
        assertEquals("Properties for container 'a':", 1, containers.get(0).getProperties().size());
    }

    @Test
    public void queryWithMultipleContainerMarkers() throws DataSourceException {
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("container-marker", null));
        modelProperties.add(new ModelProperty("container-marker", null));
        modelProperties.add(new ModelProperty("acontainer-marker-1", null));
        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        List<ModelContainer> containers = datasource.queryFor("container-marker");
        assertEquals("Number of containers: ", 2, containers.size());
        assertEquals("Properties for container 'a':", 1, containers.get(0).getProperties().size());
    }

    @Test(expected = DataSourceException.class)
    public void queryWithUriNotInContainerFactory() throws DataSourceException {
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("a", null));

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        datasource.queryFor("bogus");
    }

    @Test
    public void joinEmptyContainer() throws DataSourceException {
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("a", null));

        DummyModelContainerFactory factory = new DummyModelContainerFactory();

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        ModelContainer modelContainerA = factory.create(new ArrayList<ModelProperty>(modelProperties));
        ModelContainer modelContainer = datasource.join(
                modelContainerA, factory.create(new ArrayList<ModelProperty>()));
        assertEquals(modelContainer, modelContainerA);
    }

    /*
    @Test(expected = DataSourceException.class)
    public void joinContainerWithElementsNotInDataSource() throws DataSourceException {
        ModelProperty mpA = new ModelProperty("a", null);
        ModelProperty mpB = new ModelProperty("b", null);
        ModelProperty mpC = new ModelProperty("c", null);

        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(mpA);
        modelProperties.add(mpB);

        DummyModelContainerFactory factory = new DummyModelContainerFactory();

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);
        modelProperties.add(mpC);
        datasource.join(
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(0, 3))),
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(1, 2))));
    }
    */

    @Test
    public void cannotModifyDataSourceFromInitializedList() {
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("a", null));
        modelProperties.add(new ModelProperty("b", null));

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        modelProperties.remove(0);

        assertEquals(2, datasource.getModelProperties().size());
    }

    @Test
    public void cannotModifyDataSourceFromReturnedList() {
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("a", null));
        modelProperties.add(new ModelProperty("b", null));

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);

        datasource.getModelProperties().remove(0);

        assertEquals(2, datasource.getModelProperties().size());
    }

    @Test
    public void join() throws DataSourceException {
        ModelProperty mpA = new ModelProperty("maven/a", null);
        ModelProperty mpB = new ModelProperty("maven/b", null);
        ModelProperty mpC = new ModelProperty("maven/a", null);

        List<ModelProperty> modelProperties = Arrays.asList(mpA, mpB, mpC);

        DummyModelContainerFactory factory = new DummyModelContainerFactory();

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);
        ModelContainer joinedModelContainer = datasource.join(
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(0, 1))),
                factory.create(new ArrayList<ModelProperty>(modelProperties.subList(1, 3))));

        assertEquals(2, joinedModelContainer.getProperties().size());
        assertFalse(joinedModelContainer.getProperties().contains(mpC));
    }

    @Test
    public void delete() {
        ModelProperty mpA = new ModelProperty("a", null);
        ModelProperty mpB = new ModelProperty("b", null);
        ModelProperty mpC = new ModelProperty("a", null);

        List<ModelProperty> modelProperties = Arrays.asList(mpA, mpB, mpC);
        DummyModelContainerFactory factory = new DummyModelContainerFactory();

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);
        datasource.delete(factory.create(new ArrayList<ModelProperty>(modelProperties.subList(0, 1))));

        assertEquals(2, datasource.getModelProperties().size());
        assertFalse(datasource.getModelProperties().contains(mpA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullModelContainer() throws IllegalArgumentException {
        List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
        modelProperties.add(new ModelProperty("a", null));

        ModelDataSource datasource = new DefaultModelDataSource();
        datasource.init(modelProperties, factories);
        datasource.delete(null);
    }

    private static class DummyModelContainerFactory implements ModelContainerFactory {

        public Collection<String> getUris() {
            return Arrays.asList("container-marker");
        }

        public ModelContainer create(final List<ModelProperty> modelProperties) {
            return new DummyModelContainer(modelProperties);
        }

        private static class DummyModelContainer implements ModelContainer {

            private List<ModelProperty> modelProperties;

            private DummyModelContainer(List<ModelProperty> modelProperties) {
                this.modelProperties = new ArrayList<ModelProperty>(modelProperties);
            }

            public List<ModelProperty> getProperties() {
                return modelProperties;
            }

            public ModelContainerAction containerAction(ModelContainer modelContainer) {
                return ModelContainerAction.NOP;
            }

            public ModelContainer createNewInstance(List<ModelProperty> modelProperties) {
                return new DummyModelContainer(modelProperties);
            }

            public void sort(List<ModelProperty> modelProperties) {

            }
        }
    }
}
