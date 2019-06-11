package top.aprilyolies.javassist;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */
public class ClassGenerate {
    public static void main(String[] args) {
        String code = "package top.aprilyolies.javassist;\n" +
                "\n" +
                "public class ServiceImp implements top.aprilyolies.javassist.Service {\n" +
                "    public String sayHello() {\n" +
                "        return \"Hello\";\n" +
                "    }\n" +
                "}\n";
        ClassLoader classLoader = ClassUtils.getClassLoader(ClassGenerate.class);
        JavassistCompiler compiler = new JavassistCompiler();
        Class<?>[] interfaces1 = compiler.compile(code, classLoader).getInterfaces();
        System.out.println("finish..");
    }
}
