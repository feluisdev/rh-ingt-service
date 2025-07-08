package cv.igrp.RH_Service.shared.infrastructure.spring;

import cv.igrp.framework.core.domain.Query;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.framework.core.domain.QueryHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Component
public class SpringQueryBus implements QueryBus {

  private final Map<Class<? extends Query>, QueryHandler<?, ?>> handlers = new HashMap<>();

  public SpringQueryBus(List<QueryHandler<?, ?>> handlerList) {
    for (QueryHandler<?, ?> handler : handlerList) {
      Class<?> targetClass = resolveTargetClass(handler); // use the method above
      Type[] interfaces = targetClass.getGenericInterfaces();

      for (Type iface : interfaces) {
        if (iface instanceof ParameterizedType) {
          ParameterizedType pt = (ParameterizedType) iface;
          Type actualType = pt.getActualTypeArguments()[0];
          if (actualType instanceof Class<?>) {
          Class<?> queryType = (Class<?>) actualType;
          handlers.put((Class<? extends Query>) queryType, handler);
        }
      }
    }
  }
}
   
  @Override
  public <Q extends Query, R> R handle(Q query) {
    QueryHandler<Q, R> handler = (QueryHandler<Q, R>) handlers.get(query.getClass());
    if (handler == null) {
      throw new IllegalArgumentException("No handler found for query type: " + query.getClass());
    }
    return handler.handle(query);
  }


  private Class<?> resolveTargetClass(Object bean) {
    Class<?> clazz = bean.getClass();

    // Check for CGLIB-generated class name
    while (clazz.getName().contains("$$")) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
 }

}