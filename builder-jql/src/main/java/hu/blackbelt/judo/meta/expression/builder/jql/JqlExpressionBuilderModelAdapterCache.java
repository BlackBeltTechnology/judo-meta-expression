package hu.blackbelt.judo.meta.expression.builder.jql;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import lombok.Getter;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JqlExpressionBuilderModelAdapterCache<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> {

    private final Resource templateExpressionModel;
    private final ModelAdapter modelAdapter;

    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new HashMap<>());
    private final Map<String, MeasureName> measureNames = new HashMap<>();
    private final Map<String, MeasureName> durationMeasures = new HashMap<>();
    private final Map<String, TypeName> enumTypes = new HashMap<>();
    private final Map<String, TypeName> primitiveTypes = new HashMap<>();
    private final EMap<TO, TypeName> transferObjectTypes = ECollections.asEMap(new HashMap<>());

    private final static CacheLoader<ModelAdapter, JqlExpressionBuilderModelAdapterCache> cacheLoader = new CacheLoader<>() {
        @Override
        public JqlExpressionBuilderModelAdapterCache load(ModelAdapter modelAdapter) {

            final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                    .uri(URI.createURI("expression:filter-" + UUID.randomUUID()))
                    .build();

            JqlExpressionBuilderModelAdapterCache cache = new JqlExpressionBuilderModelAdapterCache(modelAdapter, expressionModelResourceSupport.getResource());
            return cache;
        }
    };

    private final static LoadingCache<ModelAdapter, JqlExpressionBuilderModelAdapterCache> cacheProvider = CacheBuilder
            .newBuilder()
            .expireAfterAccess(Long.parseLong(System.getProperty("JqlExpressionBuilderModelAdapterCacheExpiration", "86400")), TimeUnit.SECONDS)
            .build(cacheLoader);

    public static JqlExpressionBuilderModelAdapterCache getCache(ModelAdapter modelAdapter) {
        JqlExpressionBuilderModelAdapterCache cache = null;
        try {
            cache = cacheProvider.get(modelAdapter);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return cache;
    }

    private JqlExpressionBuilderModelAdapterCache(ModelAdapter modelAdapter, Resource expressionModel) {
        this.modelAdapter = modelAdapter;
        this.templateExpressionModel = expressionModel;
    }

    public Resource getTemplateExpressionModel() {
        return templateExpressionModel;
    }

    public EMap<C, Instance> getEntityInstances() {
        return entityInstances;
    }

    public Map<String, MeasureName> getMeasureNames() {
        return measureNames;
    }

    public Map<String, MeasureName> getDurationMeasures() {
        return durationMeasures;
    }

    public Map<String, TypeName> getEnumTypes() {
        return enumTypes;
    }

    public Map<String, TypeName> getPrimitiveTypes() {
        return primitiveTypes;
    }

    public EMap<TO, TypeName> getTransferObjectTypes() {
        return transferObjectTypes;
    }
}
