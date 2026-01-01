package hu.blackbelt.judo.meta.expression.validation.mock;

/*-
 * #%L
 * Judo :: Expression :: Model :: Test
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.slf4j.Logger;

/**
 * Utility class for creating empty EMF resources for EVL mock validation.
 * 
 * <p>This allows EVL scripts to run with mock model adapters. The key insight is that:
 * <ul>
 *   <li>Expression elements (IntegerAttribute, etc.) ARE real EMF EObjects</li>
 *   <li>The ModelAdapter is injected as a Java service</li>
 *   <li>EVL scripts call methods on expression EObjects which delegate to ModelAdapter</li>
 *   <li>Mock types returned by ModelAdapter are passed back to ModelAdapter methods</li>
 * </ul>
 * 
 * <p>Therefore, EVL scripts work with mocks as long as they don't try to navigate
 * INTO mock objects (which the expression EVL scripts don't do).</p>
 * 
 * <p>Usage with WrappedEmfModelContext:</p>
 * <pre>{@code
 * Resource emptyAdapted = MockEvlModelContext.createEmptyResource("ADAPTED");
 * 
 * modelContexts.add(
 *     wrappedEmfModelContextBuilder()
 *         .log(log)
 *         .name("ADAPTED")
 *         .resource(emptyAdapted)
 *         .validateModel(false)
 *         .build()
 * );
 * }</pre>
 */
public class MockEvlModelContext {

    /**
     * Create an empty EMF resource that can be used as a placeholder for EVL validation.
     * 
     * <p>This is useful when running EVL validation with mock model adapters where
     * the "adapted" model (PSM/ESM) is not available but the EVL scripts only use
     * it through the injected modelAdapter service.</p>
     *
     * @param name a descriptive name for the resource (used in URI)
     * @return an empty EMF resource
     */
    public static Resource createEmptyResource(String name) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("*", new XMIResourceFactoryImpl());
        
        return resourceSet.createResource(
                URI.createURI("mock://" + name.toLowerCase() + ".model"));
    }

    /**
     * Create an empty EMF resource with a specific ResourceSet.
     * 
     * @param resourceSet the ResourceSet to use
     * @param name a descriptive name for the resource (used in URI)
     * @return an empty EMF resource in the given ResourceSet
     */
    public static Resource createEmptyResource(ResourceSet resourceSet, String name) {
        return resourceSet.createResource(
                URI.createURI("mock://" + name.toLowerCase() + ".model"));
    }
}
