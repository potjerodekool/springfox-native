package org.platonos.springfoxnative.mavenplugin;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.types.ResolvedObjectType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.RequestHandler;
import springfox.documentation.RequestHandlerKey;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spring.web.WebMvcPatternsRequestConditionWrapper;
import springfox.documentation.spring.wrapper.NameValueExpression;
import springfox.documentation.spring.wrapper.PatternsRequestCondition;
import springfox.documentation.spring.wrapper.RequestMappingInfo;

import java.lang.annotation.Annotation;
import java.util.*;

public class TestRequestHandler implements RequestHandler {

    private final Class<?> requestHandlerClass = TestController.class;

    @Override
    public Class<?> declaringClass() {
        return requestHandlerClass;
    }

    @Override
    public boolean isAnnotatedWith(Class<? extends Annotation> annotation) {
        return requestHandlerClass.isAnnotationPresent(annotation);
    }

    @Override
    public PatternsRequestCondition getPatternsCondition() {
        return new WebMvcPatternsRequestConditionWrapper("/test", new org.springframework.web.servlet.mvc.condition.PatternsRequestCondition(""));
    }

    @Override
    public String groupName() {
        return "myGroup";
    }

    @Override
    public String getName() {
        return requestHandlerClass.getName();
    }

    @Override
    public Set<RequestMethod> supportedMethods() {
        return new HashSet<>();
    }

    private <E> Set<E> setOf(final E... values) {
        return new HashSet(Arrays.asList(values));
    }

    @Override
    public Set<MediaType> produces() {
        return setOf(MediaType.APPLICATION_JSON);
    }

    @Override
    public Set<MediaType> consumes() {
        return setOf(MediaType.APPLICATION_JSON);
    }

    @Override
    public Set<NameValueExpression<String>> headers() {
        return setOf();
    }

    @Override
    public Set<NameValueExpression<String>> params() {
        return setOf();
    }

    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotation) {
        return Optional.empty();
    }

    @Override
    public RequestHandlerKey key() {
        return null;
    }

    @Override
    public List<ResolvedMethodParameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public ResolvedType getReturnType() {
        return ResolvedObjectType.create(String.class, null, null, new ArrayList<>());
    }

    @Override
    public <T extends Annotation> Optional<T> findControllerAnnotation(Class<T> annotation) {
        return Optional.empty();
    }

    @Override
    public RequestMappingInfo<?> getRequestMapping() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HandlerMethod getHandlerMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestHandler combine(RequestHandler other) {
        throw new UnsupportedOperationException();
    }
}