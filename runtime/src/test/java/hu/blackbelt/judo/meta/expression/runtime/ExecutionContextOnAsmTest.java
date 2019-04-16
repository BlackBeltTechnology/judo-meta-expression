package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;

abstract class ExecutionContextOnAsmTest extends ExecutionContextTest {

    void setUp() throws Exception {
        final Resource asm = getAsmResource();
        final Resource measures = getMeasureResouce();

        if (asm != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND")
                    .aliases(ImmutableList.of("ASM"))
                    .resource(asm)
                    .build());
        }

        if (measures != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND_MEASURES")
                    .aliases(ImmutableList.of("MEASURES"))
                    .resource(measures)
                    .build());
        }

        modelContexts.add(wrappedEmfModelContextBuilder()
                .log(log)
                .name("TEST")
                .aliases(ImmutableList.of("EXPR"))
                .resource(getExpressionResource())
                .build());
    }

    protected Resource getAsmResource() throws Exception {
        // Set our custom handler
        URIHandler uriHandler = new NioFilesystemnRelativePathURIHandlerImpl("urn", FileSystems.getDefault(), targetDir().getAbsolutePath());

        final ResourceSet asmModelResourceSet = AsmModelLoader.createAsmResourceSet(uriHandler, new AsmModelLoader.LocalAsmPackageRegistration());
        AsmModelLoader.loadAsmModel(asmModelResourceSet,
                URI.createURI(new File(srcDir(), "test/models/northwind-asm.model").getAbsolutePath()),
                "test",
                "1.0.0");

        return asmModelResourceSet.getResources().get(0);
    }

    protected Resource getMeasureResouce() throws IOException  {
        final ResourceSet measureModelResourceSet = MeasureModelLoader.createMeasureResourceSet();
        MeasureModelLoader.loadMeasureModel(measureModelResourceSet,
                URI.createURI(new File(srcDir(), "test/models/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");

        return measureModelResourceSet.getResources().get(0);
    }

    @Override
    protected String getEvlSource() {
        return "epsilon/validations/expressionOnAsm.evl";
    }

    public File targetDir(){
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath);
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }
}
