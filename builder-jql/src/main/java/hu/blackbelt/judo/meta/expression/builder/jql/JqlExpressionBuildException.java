package hu.blackbelt.judo.meta.expression.builder.jql;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import hu.blackbelt.judo.meta.expression.Expression;

public class JqlExpressionBuildException extends RuntimeException {

	private final EObject jclObject;
	private final List<JqlExpressionBuildingError> errors = new ArrayList<>();
	private final Expression partialExpression;

	public Expression getPartialExpression() {
		return partialExpression;
	}

	public EObject getJclObject() {
		return jclObject;
	}
    
	public JqlExpressionBuildException(Expression partialExpression, List<JqlExpressionBuildingError> errors) {
		super("Errors during building expression: " + errorDescriptions(errors));
		this.jclObject = null;
		this.partialExpression = partialExpression;
		this.errors.addAll(errors);
	}

	private static String errorDescriptions(List<JqlExpressionBuildingError> errors) {
		return errors.stream().map(JqlExpressionBuildingError::getDescription).collect(Collectors.joining());
	}

	public JqlExpressionBuildException(Expression partialExpression, List<JqlExpressionBuildingError> errors, final Throwable throwable) {
		super("Errors during building expression: " + errorDescriptions(errors), throwable);
		this.jclObject = null;
		this.partialExpression = partialExpression;
		this.errors.addAll(errors);
	}
	
	public List<JqlExpressionBuildingError> getErrors() {
		return errors;
	}
	
}
