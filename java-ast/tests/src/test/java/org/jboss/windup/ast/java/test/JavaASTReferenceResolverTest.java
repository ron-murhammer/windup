package org.jboss.windup.ast.java.test;

import java.nio.file.Paths;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.ast.java.ASTProcessor;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaASTReferenceResolverTest extends AbstractJavaASTTest
{

    @Test
    public void testHelloWorld()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/helloworld/HelloWorld.java"));

        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.helloworld.HelloWorld", ResolutionStatus.RESOLVED, TypeReferenceLocation.TYPE, 3, 0, 174,
                                "public class HelloWorld {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.helloworld.HelloWorld.main(String[])", ResolutionStatus.RESOLVED, TypeReferenceLocation.METHOD,
                                5, 23, 4,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("void", ResolutionStatus.RESOLVED, TypeReferenceLocation.RETURN_TYPE, 5, 4, 108,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("java.lang.String[]", ResolutionStatus.RESOLVED, TypeReferenceLocation.METHOD_PARAMETER, 5, 4, 108,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("java.lang.Exception", ResolutionStatus.RESOLVED, TypeReferenceLocation.THROWS_METHOD_DECLARATION, 5, 51, 9,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("java.io.PrintStream.println(java.lang.String)", ResolutionStatus.RESOLVED, TypeReferenceLocation.METHOD_CALL,
                                6, 19, 7,
                                "System.out.println(\"Hello world!\")")));

        Assert.assertEquals(6, references.size());
    }

    @Test
    public void testSimpleMain()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/simple/Main.java"));

        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.MyBClass", ResolutionStatus.RESOLVED, TypeReferenceLocation.VARIABLE_DECLARATION, 9, 8,
                                28,
                                "MyBClass b=new MyBClass();")));
        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.MyAClass.interfaceMethod()", ResolutionStatus.RESOLVED, TypeReferenceLocation.METHOD_CALL,
                                12, 26, 15,
                                "c.returnAnother().interfaceMethod()")));
        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.SomeInterface.interfaceMethod()", ResolutionStatus.RESOLVED,
                                TypeReferenceLocation.METHOD_CALL, 12, 26, 15,
                                "c.returnAnother().interfaceMethod()")));
        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.ClassReturningAnother.returnAnother()", ResolutionStatus.RESOLVED,
                                TypeReferenceLocation.METHOD_CALL, 12, 10, 13,
                                "c.returnAnother()")));
    }

    @Test
    public void testMyBClass()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/simple/MyBClass.java"));

        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.MyBClass", ResolutionStatus.RESOLVED, TypeReferenceLocation.TYPE, 4, 0, 161,
                                "public class MyBClass extends MyAClass {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.MyAClass", ResolutionStatus.RESOLVED, TypeReferenceLocation.TYPE, 4, 0, 161,
                                "public class MyBClass extends MyAClass {")));
    }

    @Test
    public void testMyAClass()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/simple/MyAClass.java"));

        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.MyAClass", ResolutionStatus.RESOLVED, TypeReferenceLocation.TYPE, 3, 0, 128,
                                "public class MyAClass implements SomeInterface {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.simple.SomeInterface", ResolutionStatus.RESOLVED, TypeReferenceLocation.IMPLEMENTS_TYPE, 3, 0,
                                99,
                                "public class MyAClass implements SomeInterface {")));
    }

    @Test
    public void testJavaLangReferences()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/javalang/JavaLangReferences.java"));

        Assert.assertTrue(references.contains(
                    new ClassReference("testclasses.javalang.JavaLangReferences", ResolutionStatus.RESOLVED, TypeReferenceLocation.TYPE, 3, 0, 191,
                                "public class JavaLangReferences {")));
        Assert.assertTrue(references.contains(
                    new ClassReference("void", ResolutionStatus.RESOLVED, TypeReferenceLocation.RETURN_TYPE, 5, 4, 119, "public void someMethod(){")));
        Assert.assertTrue(references.contains(
                    new ClassReference("java.lang.String", ResolutionStatus.RESOLVED, TypeReferenceLocation.VARIABLE_DECLARATION, 7, 8, 39,
                                "String a=\"This is an example String\";")));
        Assert.assertTrue(references.contains(
                    new ClassReference("java.lang.String", ResolutionStatus.RESOLVED, TypeReferenceLocation.VARIABLE_DECLARATION, 8, 8, 26,
                                "String b=a.substring(1);")));
        Assert.assertTrue(references.contains(
                    new ClassReference("java.lang.String.substring(int)", ResolutionStatus.RESOLVED, TypeReferenceLocation.METHOD_CALL, 8, 21, 9,
                                "a.substring(1)")));
    }

    @Test
    public void testWildcardImport()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/simple/ExampleWildcardImport.java"));

        Assert.assertTrue(references.contains(new ClassReference("java.util.*", ResolutionStatus.RESOLVED, TypeReferenceLocation.IMPORT, 10, 7, 9,
                    "import java.util.*;")));
        Assert.assertTrue(references.contains(new ClassReference("java.net.*", ResolutionStatus.RESOLVED, TypeReferenceLocation.IMPORT, 11, 7, 8,
                    "import java.net.*;")));
        Assert.assertTrue(references.contains(new ClassReference("java.awt.*", ResolutionStatus.RESOLVED, TypeReferenceLocation.IMPORT, 12, 7, 8,
                    "import java.awt.*;")));
    }

    @Test
    public void testNonImportedFQCN()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/simple/FullNonImportedFQCNNotOnClasspath.java"));

        Assert.assertTrue(references.contains(new ClassReference("com.proprietary.Constants.MY_CONSTANT", ResolutionStatus.RECOVERED,
                    TypeReferenceLocation.VARIABLE_INITIALIZER,
                    5, 4, 56, "private int foo=com.proprietary.Constants.MY_CONSTANT;")));
        Assert.assertTrue(references.contains(new ClassReference("OtherConstants.OTHER_CONSTANT", ResolutionStatus.RECOVERED,
                    TypeReferenceLocation.VARIABLE_INITIALIZER,
                    6, 4, 49, "private int foo2=OtherConstants.OTHER_CONSTANT;")));
    }

    @Test
    public void testInterfaceExtension()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/simple/EventServer.java"));

        Assert.assertTrue(references.contains(new ClassReference("java.rmi.Remote", ResolutionStatus.RESOLVED, TypeReferenceLocation.INHERITANCE,
                    6, 0, 117, "public interface EventServer extends Remote {")));
    }

    @Test
    public void testInnerClassScenarios()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/innerclasses/SampleWithInnerClasses.java"));

        boolean foundReturnTypeSimpleNested = false;
        boolean foundConstructorCallSimpleNested = false;
        boolean foundUseInnerAnonymous = false;
        boolean foundDefinedInMethod = false;
        boolean foundUnresolvedInnerWithoutParen = false;
        boolean foundUnresolvedInnerWithParen = false;

        for (ClassReference reference : references)
        {
            System.out.println("Line: " + reference.getLineNumber() + ", Name: " + reference.getQualifiedName() + ", Resolution Status: "
                        + reference.getResolutionStatus() + ", Location: " + reference.getLocation());
            if ("testclasses.innerclasses.SampleWithInnerClasses.SimpleNested".equals(reference.getQualifiedName())
                        && reference.getLocation() == TypeReferenceLocation.RETURN_TYPE)
            {
                foundReturnTypeSimpleNested = true;
                Assert.assertEquals("This should be a resolved binding", ResolutionStatus.RESOLVED, reference.getResolutionStatus());
            }
            else if (reference.getLineNumber() == 12
                        && "testclasses.innerclasses.SampleWithInnerClasses.SimpleNested".equals(reference.getQualifiedName())
                        && reference.getLocation() == TypeReferenceLocation.CONSTRUCTOR_CALL)
            {
                foundConstructorCallSimpleNested = true;
                Assert.assertEquals("This should be a resolved binding", ResolutionStatus.RESOLVED, reference.getResolutionStatus());
            }
            else if (reference.getQualifiedName().equals("DefinedInside()"))
            {
                foundDefinedInMethod = true;
                Assert.assertEquals("This should be a resolved binding", ResolutionStatus.RESOLVED, reference.getResolutionStatus());
            }
            else if (reference.getLineNumber() == 30
                        && reference.getQualifiedName().equals("testclasses.innerclasses.SampleWithInnerClasses.SimpleNested()"))
            {
                foundUseInnerAnonymous = true;
                Assert.assertEquals("This should be a resolved binding", ResolutionStatus.RESOLVED, reference.getResolutionStatus());
            }
            else if (reference.getLineNumber() == 41 && reference.getQualifiedName().equals("ThisClassDoesNotExist()"))
            {
                foundUnresolvedInnerWithParen = true;
                Assert.assertEquals("This should be a resolved binding", ResolutionStatus.UNRESOLVED, reference.getResolutionStatus());
            }
            else if (reference.getLineNumber() == 41 && reference.getQualifiedName().equals("ThisClassDoesNotExist"))
            {
                foundUnresolvedInnerWithoutParen = true;
                Assert.assertEquals("This should be a resolved binding", ResolutionStatus.UNRESOLVED, reference.getResolutionStatus());
            }
        }
        Assert.assertTrue(foundReturnTypeSimpleNested);
        Assert.assertTrue(foundConstructorCallSimpleNested);
        Assert.assertTrue(foundDefinedInMethod);
        Assert.assertTrue(foundUseInnerAnonymous);
        Assert.assertTrue(foundUnresolvedInnerWithoutParen);
        Assert.assertTrue(foundUnresolvedInnerWithParen);
    }
}
