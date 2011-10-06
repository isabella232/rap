/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal.engine;

import org.eclipse.rap.rwt.testfixture.internal.TestResourceManager;
import org.eclipse.rwt.internal.application.ApplicationContext;


public class ApplicationContextHelper {

  public static void setIgnoreResoureRegistration( boolean ignore ) {
    ApplicationContext.ignoreResoureRegistration = ignore;
  }
  
  public static void setIgnoreResoureDeletion( boolean ignore ) {
    ApplicationContext.ignoreResoureDeletion = ignore;
  }

  public static void useDefaultResourceManager() {
    ApplicationContext.testResourceManager = null;
  }

  public static void useTestResourceManager() {
    ApplicationContext.testResourceManager = new TestResourceManager();
  }
}
