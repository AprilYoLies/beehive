package top.aprilyolies.beehive.compiler;

import top.aprilyolies.beehive.extension.annotation.SPI;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */

/**
 * 该接口的实现类可以将符合代码规范的字符串编译成为 class 对象
 */
@SPI("javassist")
public interface Compiler {
    /**
     * 实现类通过该方法将源代码编译成为 class
     *
     * @param code        源代码
     * @param classLoader 类加载器
     * @return 编译生成的 class
     */
    Class<?> compile(String code, ClassLoader classLoader);
}
