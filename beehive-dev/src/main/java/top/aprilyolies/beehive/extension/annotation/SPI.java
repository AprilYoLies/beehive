package top.aprilyolies.beehive.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */

/**
 * 该注解可用于接口上，用于表示该接口是符合 SPI 拓展机制的
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SPI {
    /**
     * 默认拓展类的名字，ExtensionLoader 的 getDefaultExtensionName() 方法将会返回此默认值
     *
     * @return
     */
    String value() default "";
}
