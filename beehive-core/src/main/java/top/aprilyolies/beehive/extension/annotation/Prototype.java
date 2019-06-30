package top.aprilyolies.beehive.extension.annotation;

/**
 * @Author EvaJohnson
 * @Date 2019-06-30
 * @Email g863821569@gmail.com
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于表示从 beehive spi 中获取的实例为多例的（非单例）
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Prototype {
}
