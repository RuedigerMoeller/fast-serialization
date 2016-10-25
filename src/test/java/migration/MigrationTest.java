package migration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

public class MigrationTest {

    FSTConfiguration cfg = FSTConfiguration.createDefaultConfiguration();
    File tmpDir1 = new File("cl1");
    File tmpDir2 = new File("cl2");
    private Field cls1Field1;
    private Class cls1;
    private Class cls2;
    private Field cls2Field1;
    private Field cls2Field2;

    @Before
    public void setUp() throws Exception {
        tmpDir1.mkdirs();

        URLClassLoader cl1 = new URLClassLoader(new URL[]{tmpDir1.toURI().toURL()});
        final ClassPool def = ClassPool.getDefault();
        CtClass string = def.get("java.lang.String");
        CtClass serializable = def.get("java.io.Serializable");
        final ClassPool pool = new ClassPool();
        pool.appendSystemPath();

        CtClass POJO_v1 = pool.makeClass("migration.DTO");
        POJO_v1.addInterface(serializable);

        final CtField field1 = new CtField(string, "field1", POJO_v1);
        field1.setModifiers(Modifier.PUBLIC);
        POJO_v1.addField(field1, "\"fieldInit1\"");
        cls1 = POJO_v1.toClass(cl1);
        cls1Field1 = cls1.getDeclaredField("field1");
        cls1Field1.setAccessible(true);
    }

    @Before
    public void setUp2() throws Exception {
        tmpDir2.mkdirs();
        URLClassLoader cl1 = new URLClassLoader(new URL[]{tmpDir2.toURI().toURL()});
        final ClassPool def = ClassPool.getDefault();
        CtClass string = def.get("java.lang.String");
        CtClass serializable = def.get("java.io.Serializable");
        final ClassPool pool = new ClassPool();
        pool.appendSystemPath();

        CtClass POJO_v1 = pool.makeClass("migration.DTO");
        POJO_v1.addInterface(serializable);

        final CtField field1 = new CtField(string, "field1", POJO_v1);
        field1.setModifiers(Modifier.PUBLIC);
        POJO_v1.addField(field1, "\"fieldInit1\"");

        final CtField field2 = new CtField(string, "field2", POJO_v1);
        field2.setModifiers(Modifier.PUBLIC);
        POJO_v1.addField(field2, "\"fieldInit2\"");

        cls2 = POJO_v1.toClass(cl1);
        cls2Field1 = cls2.getDeclaredField("field1");
        cls2Field1.setAccessible(true);

        cls2Field2 = cls2.getDeclaredField("field2");
        cls2Field2.setAccessible(true);

    }

    @Test
    public void simpleSerialization() throws Exception {
        final DTO dto = new DTO();
        final byte[] bytes = cfg.asByteArray(dto);
        final DTO dto2 = (DTO) cfg.asObject(bytes);
        Assert.assertEquals(dto.field1, dto2.field1);
        Assert.assertEquals(dto.field2, dto2.field2);
    }

    @Test
    public void migrationAddFields() throws Exception {
        // Here we check simple dto migration.
        // serialize object with one field from different classloader
        final Object dto = cls1.newInstance();
        final byte[] bytes = cfg.asByteArray(dto);
        // deserialize object with two fields with current classLoader
        final DTO dto2 = (DTO) cfg.asObject(bytes);
        Assert.assertEquals(dto2.field1, cls1Field1.get(dto));
    }

    @Test
    public void noMigrationDifferentClassloaders() throws Exception {
        // create object wit two fields from different classLoader
        final Object dto = cls2.newInstance();
        final byte[] bytes = cfg.asByteArray(dto);
        // deserialize object with two fields with current classLoader
        final DTO dto2 = (DTO) cfg.asObject(bytes);
        Assert.assertEquals(dto2.field1, cls2Field1.get(dto));
        Assert.assertEquals(dto2.field2, cls2Field2.get(dto));

    }

    @After
    public void tearDown() throws Exception {
        for (File file : tmpDir1.listFiles()) {
            file.delete();
        }
        for (File file : tmpDir2.listFiles()) {
            file.delete();
        }
    }

}
