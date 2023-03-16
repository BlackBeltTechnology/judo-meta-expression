package hu.blackbelt.judo.meta.expression.runtime;

/*-
 * #%L
 * JUDO :: Expression :: Model
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

import java.net.URI;
import java.net.URISyntaxException;

public abstract class ExpressionEpsilonValidator {

    public static URI calculateExpressionValidationScriptURI() throws URISyntaxException {
        URI expressionRoot = ExpressionModel.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        if (expressionRoot.toString().endsWith(".jar")) {
            expressionRoot = new URI("jar:" + expressionRoot.toString() + "!/validations/");
        } else if (expressionRoot.toString().startsWith("jar:bundle:")) {
// bundle://37.0:0/validations/
// jar:bundle://37.0:0/!/validations/expression.evl
            expressionRoot = new URI(
                    expressionRoot.toString().substring(4, expressionRoot.toString().indexOf("!")) + "validations/");
        } else {
            expressionRoot = new URI(expressionRoot.toString() + "/validations/");
        }
        return expressionRoot;

    }

}
