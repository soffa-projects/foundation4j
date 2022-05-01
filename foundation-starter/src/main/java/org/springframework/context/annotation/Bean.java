/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    /**
     * Alias for {@link #name}.
     * <p>Intended to be used when no other attributes are needed, for example:
     * {@code @Bean("customBeanName")}.
     * @since 4.3.3
     * @see #name
     */
    @AliasFor("name")
    String[] value() default {};

    /**
     * The name of this bean, or if several names, a primary bean name plus aliases.
     * <p>If left unspecified, the name of the bean is the name of the annotated method.
     * If specified, the method name is ignored.
     * <p>The bean name and aliases may also be configured via the {@link #value}
     * attribute if no other attributes are declared.
     * @see #value
     */
    @AliasFor("value")
    String[] name() default {};

    /**
     * Are dependencies to be injected via convention-based autowiring by name or type?
     * <p>Note that this autowire mode is just about externally driven autowiring based
     * on bean property setter methods by convention, analogous to XML bean definitions.
     * <p>The default mode does allow for annotation-driven autowiring. "no" refers to
     * externally driven autowiring only, not affecting any autowiring demands that the
     * bean class itself expresses through annotations.
     * @see Autowire#BY_NAME
     * @see Autowire#BY_TYPE
     * @deprecated as of 5.1, since {@code @Bean} factory method argument resolution and
     * {@code @Autowired} processing supersede name/type-based bean property injection
     */
    @Deprecated
    Autowire autowire() default Autowire.NO;

    /**
     * Is this bean a candidate for getting autowired into some other bean?
     * <p>Default is {@code true}; set this to {@code false} for internal delegates
     * that are not meant to get in the way of beans of the same type in other places.
     * @since 5.1
     */
    boolean autowireCandidate() default true;

    /**
     * The optional name of a method to call on the bean instance during initialization.
     * Not commonly used, given that the method may be called programmatically directly
     * within the body of a Bean-annotated method.
     * <p>The default value is {@code ""}, indicating no init method to be called.
     * @see org.springframework.beans.factory.InitializingBean
     * @see org.springframework.context.ConfigurableApplicationContext#refresh()
     */
    String initMethod() default "";

    /**
     * The optional name of a method to call on the bean instance upon closing the
     * application context, for example a {@code close()} method on a JDBC
     * {@code DataSource} implementation, or a Hibernate {@code SessionFactory} object.
     * The method must have no arguments but may throw any exception.
     * <p>As a convenience to the user, the container will attempt to infer a destroy
     * method against an object returned from the {@code @Bean} method. For example, given
     * an {@code @Bean} method returning an Apache Commons DBCP {@code BasicDataSource},
     * the container will notice the {@code close()} method available on that object and
     * automatically register it as the {@code destroyMethod}. This 'destroy method
     * inference' is currently limited to detecting only public, no-arg methods named
     * 'close' or 'shutdown'. The method may be declared at any level of the inheritance
     * hierarchy and will be detected regardless of the return type of the {@code @Bean}
     * method (i.e., detection occurs reflectively against the bean instance itself at
     * creation time).
     * <p>To disable destroy method inference for a particular {@code @Bean}, specify an
     * empty string as the value, e.g. {@code @Bean(destroyMethod="")}. Note that the
     * {@link org.springframework.beans.factory.DisposableBean} callback interface will
     * nevertheless get detected and the corresponding destroy method invoked: In other
     * words, {@code destroyMethod=""} only affects custom close/shutdown methods and
     * {@link java.io.Closeable}/{@link java.lang.AutoCloseable} declared close methods.
     * <p>Note: Only invoked on beans whose lifecycle is under the full control of the
     * factory, which is always the case for singletons but not guaranteed for any
     * other scope.
     * @see org.springframework.beans.factory.DisposableBean
     * @see org.springframework.context.ConfigurableApplicationContext#close()
     */
    String destroyMethod() default AbstractBeanDefinition.INFER_METHOD;

}
