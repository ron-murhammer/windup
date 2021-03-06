package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;
import org.jboss.windup.rules.apps.javaee.model.JPANamedQueryModel;
import org.jboss.windup.rules.apps.javaee.service.JPAEntityService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with JPA related annotations, and adds JPA related metadata for these.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class DiscoverJPAAnnotationsRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logging.get(DiscoverJPAAnnotationsRuleProvider.class);

    private static final String ENTITY_ANNOTATIONS = "entityAnnotations";
    private static final String TABLE_ANNOTATIONS_LIST = "tableAnnotations";
    private static final String NAMED_QUERY_LIST = "namedQuery";
    private static final String NAMED_QUERIES_LIST = "namedQueries";

    public DiscoverJPAAnnotationsRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverJPAAnnotationsRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(JavaClass
                                .references("javax.persistence.Entity")
                                .at(TypeReferenceLocation.ANNOTATION)
                                .as(ENTITY_ANNOTATIONS)
                                .or(JavaClass.references("javax.persistence.Table").at(TypeReferenceLocation.ANNOTATION).as(TABLE_ANNOTATIONS_LIST))
                                .or(JavaClass.references("javax.persistence.NamedQuery").at(TypeReferenceLocation.ANNOTATION).as(NAMED_QUERY_LIST))
                                .or(JavaClass.references("javax.persistence.NamedQueries").at(TypeReferenceLocation.ANNOTATION)
                                            .as(NAMED_QUERIES_LIST)))
                    .perform(Iteration.over(ENTITY_ANNOTATIONS).perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractEntityBeanMetadata(event, payload);
                        }
                    }).endIteration())
                    .withId(ruleIDPrefix + "_JPAEntityBeanRule");
    }

    private String getAnnotationLiteralValue(JavaAnnotationTypeReferenceModel model, String name)
    {
        JavaAnnotationTypeValueModel valueModel = model.getAnnotationValues().get(name);

        if (valueModel instanceof JavaAnnotationLiteralTypeValueModel)
        {
            JavaAnnotationLiteralTypeValueModel literalTypeValue = (JavaAnnotationLiteralTypeValueModel) valueModel;
            return literalTypeValue.getLiteralValue();
        }
        else
        {
            return null;
        }
    }

    private void extractEntityBeanMetadata(GraphRewrite event, JavaTypeReferenceModel entityTypeReference)
    {
        ((SourceFileModel) entityTypeReference.getFile()).setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel entityAnnotationTypeReference = (JavaAnnotationTypeReferenceModel) entityTypeReference;
        JavaAnnotationTypeReferenceModel tableAnnotationTypeReference = null;
        for (WindupVertexFrame annotationTypeReferenceBase : Variables.instance(event).findVariable(TABLE_ANNOTATIONS_LIST))
        {
            JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceBase;
            if (annotationTypeReference.getFile().equals(entityTypeReference.getFile()))
            {
                tableAnnotationTypeReference = annotationTypeReference;
                break;
            }
        }

        JavaClassModel ejbClass = getJavaClass(entityTypeReference);

        String ejbName = getAnnotationLiteralValue(entityAnnotationTypeReference, "name");
        if (ejbName == null)
        {
            ejbName = ejbClass.getClassName();
        }
        String tableName = tableAnnotationTypeReference == null ? ejbName : getAnnotationLiteralValue(tableAnnotationTypeReference, "name");
        if (tableName == null)
        {
            tableName = ejbName;
        }
        String catalogName = tableAnnotationTypeReference == null ? null : getAnnotationLiteralValue(tableAnnotationTypeReference, "catalog");
        String schemaName = tableAnnotationTypeReference == null ? null : getAnnotationLiteralValue(tableAnnotationTypeReference, "schema");

        JPAEntityService jpaService = new JPAEntityService(event.getGraphContext());
        JPAEntityModel jpaEntity = jpaService.create();

        jpaEntity.setEntityName(ejbName);
        jpaEntity.setJavaClass(ejbClass);
        jpaEntity.setTableName(tableName);
        jpaEntity.setCatalogName(catalogName);
        jpaEntity.setSchemaName(schemaName);

        GraphService<JPANamedQueryModel> namedQueryService = new GraphService<>(event.getGraphContext(), JPANamedQueryModel.class);

        Iterable<? extends WindupVertexFrame> namedQueriesList = Variables.instance(event).findVariable(NAMED_QUERIES_LIST);
        if (namedQueriesList != null)
        {
            for (WindupVertexFrame annotationTypeReferenceBase : namedQueriesList)
            {
                JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceBase;

                if (annotationTypeReference.getFile().equals(entityTypeReference.getFile()))
                {
                    JavaAnnotationTypeValueModel value = annotationTypeReference.getAnnotationValues().get("value");
                    if (value != null && value instanceof JavaAnnotationListTypeValueModel)
                    {
                        JavaAnnotationListTypeValueModel referenceList = (JavaAnnotationListTypeValueModel) value;

                        if (referenceList.getList() != null)
                        {
                            for (JavaAnnotationTypeValueModel ref : referenceList.getList())
                            {
                                if (ref instanceof JavaAnnotationTypeReferenceModel)
                                {
                                    JavaAnnotationTypeReferenceModel reference = (JavaAnnotationTypeReferenceModel) ref;
                                    addNamedQuery(namedQueryService, jpaEntity, reference);
                                }
                                else
                                {
                                    LOG.warning("Unexpected Annotation");
                                }
                            }
                        }
                    }
                }
            }
        }

        Iterable<? extends WindupVertexFrame> namedQueryList = Variables.instance(event).findVariable(NAMED_QUERY_LIST);
        if (namedQueryList != null)
        {
            for (WindupVertexFrame annotationTypeReferenceBase : namedQueryList)
            {
                JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceBase;

                if (annotationTypeReference.getFile().equals(entityTypeReference.getFile()))
                {
                    JavaAnnotationTypeReferenceModel reference = (JavaAnnotationTypeReferenceModel) annotationTypeReference;
                    addNamedQuery(namedQueryService, jpaEntity, reference);
                }
            }
        }
    }

    private void addNamedQuery(GraphService<JPANamedQueryModel> namedQueryService, JPAEntityModel jpaEntity,
                JavaAnnotationTypeReferenceModel reference)
    {
        String name = getAnnotationLiteralValue(reference, "name");
        String query = getAnnotationLiteralValue(reference, "query");

        LOG.info("Found query: " + name + " -> " + query);

        JPANamedQueryModel namedQuery = namedQueryService.create();
        namedQuery.setQueryName(name);
        namedQuery.setQuery(query);

        namedQuery.setJpaEntity(jpaEntity);
    }

    private JavaClassModel getJavaClass(JavaTypeReferenceModel javaTypeReference)
    {
        JavaClassModel result = null;
        FileModel originalFile = javaTypeReference.getFile();
        if (originalFile instanceof JavaSourceFileModel)
        {
            JavaSourceFileModel javaSource = (JavaSourceFileModel) originalFile;
            for (JavaClassModel javaClassModel : javaSource.getJavaClasses())
            {
                // there can be only one public one, and the annotated class should be public
                if (javaClassModel.isPublic() != null && javaClassModel.isPublic())
                {
                    result = javaClassModel;
                    break;
                }
            }

            if (result == null)
            {
                // no public classes found, so try to find any class (even non-public ones)
                result = javaSource.getJavaClasses().iterator().next();
            }
        }
        else if (originalFile instanceof JavaClassFileModel)
        {
            result = ((JavaClassFileModel) originalFile).getJavaClass();
        }
        else
        {
            LOG.warning("Unrecognized file type with annotation found at: \"" + originalFile.getFilePath() + "\"");
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}