/*
 * Copyright (C) 2018 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */
package au.org.ala.test.spock

import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * Spock Environment Annotation Extension
 * @author "Javier Molina <javier-molina at GH>"
 */
@Slf4j
class EnvironmentEndPointExtension extends AbstractAnnotationDrivenExtension<EnvironmentEndPoint> {

//    private static def config = new ConfigSlurper().parse(new File('src/test/resources/SpockConfig.groovy').toURL())

/**
 * env environment variable
 * <p>
 * Defaults to {@code LOCAL_END_POINT}
 */
//    private static final String envString = System.getProperties().getProperty("env", config.envHost);

    static {
//        log.info("Environment End Point [" + envString + "]")
    }

/**
 * {@inheritDoc}
 */
    @Override
    void visitFieldAnnotation(EnvironmentEndPoint annotation, FieldInfo field) {
        String envString = System.getProperties().getProperty(annotation.envVariable());
        if(!envString) {
            log.warn("Property ${annotation.envVariable()} not set while running test suite. Most likely tests will fail." )
        } else {
            log.info("Using ${annotation.envVariable()} property with value ${envString}." )
        }

        def interceptor = new EnvironmentInterceptor(field, envString)

        interceptor.install(field.parent.getTopSpec())
    }
}

/**
 *
 * Environment Intercepter
 *
 */
class EnvironmentInterceptor extends AbstractMethodInterceptor {
    private final FieldInfo field
    private final String envString

    EnvironmentInterceptor(FieldInfo field, String envString) {
        this.field = field
        this.envString = envString
    }

    private void injectEnvironmentHost(target) {
        field.writeValue(target, envString)
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) {
        injectEnvironmentHost(invocation.target)
        invocation.proceed()
    }


    void install(SpecInfo spec) {
        spec.setupMethods.each {it.addInterceptor this}
    }
}