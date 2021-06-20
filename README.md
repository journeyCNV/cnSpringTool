# cnSpringTool
My spring frame

开发文档

***
# 前言
通过模仿Spring实现自己的框架来深入理解Spring框架的设计思想，开发目标是实现四个主要功能：
* Spring上下文，Bean容器
* IOC
* AOP
* MVC

本博客不是真实的Spring源码！不要当作源码读！
***
# 开始搭建
***
创建一个什么都没有的Maven项目
![](https://img-blog.csdnimg.cn/20210617225648784.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)

***
## ApplicationContext
先创建一个CNApplicationContext类作为Spring上下文。

>**【Spring】**
>\
>Spring 框架的主要特性之一是 IoC（控制反转）容器。在Spring IoC容器负责管理应用程序的对象。它使用依赖注入来实现控制反转。
>\
>`BeanFactory` 和`ApplicationContext` 接口代表 Spring IoC 容器。
>
>* BeanFactory接口是访问Spring容器的根接口，采用的是**延迟加载**
>* ApplicationContext用于向应用程序提供配置信息是一个中央接口，实现了BeanFactory，采用**预先加载**


这里我们忽略BeanFactory，只实现一个ApplicationContext。

ApplicationContext需要做的工作有：
* 解析配置文件
* 扫描目标路径
* 提供Bean容器
* 创建Bean
* 依赖注入
* 提供应用获取Bean的方法


**CNApplicationContext.java**
```java
public class CNApplicationContext {
	private Class configClass;//配置类
	public Object getBean(String beanName){}
}
```

***
## @ComponentScan
Spring是使用配置文件，为了简化开发，我们使用配置类代替配置文件。

我们要给配置类加上一个定义Spring容器扫描路径的注解，下面创建这个注解：

**ComponentScan.java**
```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //写在类上
public @interface ComponentScan{

    //定义一个 指定扫描路径的属性
    String value() default "";

}
```
`@Retention`是一个元注解，`RetentionPolicy.RUNTIME`：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在。
`@Target`也是一个元注解，`ElementType.TYPE`表示我们的注解是用在类上的。
value是这个注解的参数。

***
## @Component
标注类为Spring容器的组件。

**Component.java**
```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //写在类上
public @interface Component {
    String value() default "";
}
```
***
# Spring上下文初步搭建
***
## 实现ApplicationContext
先将根据注解扫描组件的功能给实现：

**CNApplicationContext.java**
```java
private void scanGetDefine(Class configClass) {
	//解析配置类
    ComponentScan componentScan = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
    String path = componentScan.value(); //得到了扫描路径
    path = path.replace(".","/");		
```
根据配置类获得配置类上的注解，然后通过注解的属性获得扫描路径。
```java
    //类加载 Application ClassLoader
    ClassLoader classLoader = CNApplicationContext.class.getClassLoader();
    URL resource = classLoader.getResource(path);
    File file = new File(resource.getFile());//得到目录
```
`classLoader.getResource()`加载当前类加载器以及父类加载器所在路径的资源文件，将遇到的第一个资源文件直接返回。

```java
	if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                String filename = f.getAbsolutePath();
                if(filename.endsWith(".class")) {
                    String className = filename.substring(filename.indexOf("com"), filename.indexOf(".class"));
                    className = className.replace("\\", ".");
```
解析扫描目录下的文件，留下类名。
```java
		Class<?> clazz = classLoader.loadClass(className);
    	if (clazz.isAnnotationPresent(Component.class)){
```
这里通过`classLoader.loadClass`加载类，这里为什么不用Class.forName原因如下：
>**Class.forName**和**ClassLoader.loadClass**都会执行加载过程。不同的是，在类加载后：
>\
>Class.forName：默认执行初始化，但可以指定；
>ClassLoader.loadClass：默认不做任何事(不连接，不初始化)，但可以指定是否连接。

```java
			Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
            String beanName = componentAnnotation.value();

            //解析出来的Bean的定义
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setClazz(clazz);
            if(clazz.isAnnotationPresent(Scope.class)) {
            	Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                beanDefinition.setScope(scopeAnnotation.value());
            }else {
                 beanDefinition.setScope("singleton");
            }
                           
    		beanDefinitionMap.put(beanName,beanDefinition);
    }
}
```
如果扫描目录下的类使用了@Component注解将继续这些逻辑。
通过@Componet的属性获取组件名BeanName。
这里后面将解析出来的Bean定义使用BeanDefinition存储起来, 并且对Bean的作用域进行了判断。

***
## BeanDefinition
BeanDefinition描述Bean的定义信息，bean 的定义就是包含了这个 bean 应该有的所有重要信息。

> **【Spring】**
>bean 的定义信息可以包含许多配置信息，包括构造函数参数，属性值和特定于容器的信息，例如初始化方法，静态工厂方法名称等。
>\
>**BeanDefinition 描述了一个 bean 的实例**，该实例具有属性值，构造函数参数值以及具体实现所提供的更多信息。 这只是一个最小的接口，它的主要目的是允许 BeanFactoryPostProcessor（例如 PropertyPlaceholderConfigurer ）内省和修改属性值和其他 bean 的元数据。
> **在Spring里BeanDefinition是一个接口。**
> 

我们这里实现的BeanDefinition更像是Spring的AbstractBeanDefinition。

**BeanDefinition.java**

```java
public class BeanDefinition {
    private Class clazz;
    private String scope;
    public BeanDefinition(){}
    public BeanDefinition(Class clazz,String scope){
        this.clazz = clazz;
        this.scope = scope;
    }

    public Class getClazz(){
        return clazz;
    }

    public void setClazz(Class clazz){
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
```
这里Bean的定义只包含了两个属性：Bean的对象类型，Bean的作用域。

***
## @Scope
使用@Scope注解标注Bean的作用域。
我们在后续的代码里只设置两种作用域：
singleton和prototype。

**Scope.java**
```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    String value() ;
}
```
***
## 解析Bean的定义
**CNApplicationContext.java**
```java
//解析出来的Bean的定义
BeanDefinition beanDefinition = new BeanDefinition();
beanDefinition.setClazz(clazz);
if(clazz.isAnnotationPresent(Scope.class)) {
	Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
    beanDefinition.setScope(scopeAnnotation.value());
}else {
    beanDefinition.setScope("singleton")
}                   
```
扫描组件的时候，要为每一个使用了@Component注解的类创建一个BeanDefinition对象。
将Bean的类型和作用域都保存在BeanDefinition对象中。
同样作用域也是通过判断是否有@Scope注解，如果有Scope注解通过getDeclaredAnnotation方法获取Scope对象从而获得指定的作用域。
如果没有指定，将Bean的作用域设置为默认的singleton作用域。

最终将Bean的定义存放在CNApplicationContext的私有属性beanDefinitionMap中：
```java
//存Bean的定义
private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
```
```java
beanDefinitionMap.put(beanName,beanDefinition);
```
***
## 小结
到现在为止，已经完成了这样的工作：
给配置文件提供了@ComponentScan注解用于标注扫描路径，
给Bean组件提供了@Component注解和@Scope注解。
定义了BeanDefinition来存储Bean的定义信息。
实现了CNApplicationContext来解析配置文件，通过配置文件获得扫描路径，再去解析指定目录下的类的注解，将Bean的定义存放到BeanDefinition中。

***
# IoC实现
***
## 获取Bean
设置一个单例池：
**CNApplicationContext.java**
```java
//单例池
private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
```
getBean方法用于获取Bean，逻辑是这样的，如果存在于beanDefinitionMap中，说明是个Bean。如果他的作用域是singleton则从单例池中获取，如果作用域是prototype，那需要创建一个新的Bean对象。

```java
	public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                //如果是单例bean，从单例池返回
                Object o = singletonObjects.get(beanName);
                return o;
            }else {
                //要New一个bean 原型bean
                Object bean = createBean(beanName,beanDefinition);
                return bean;
            }

        }else {
            throw new NullPointerException();
        }
    }
```

这里我们还没有实现创建Bean的方法，而作用域为singleton的Bean在创建ApplicationContext对象时就放入单例池了。

***
## ApplicationContext构造方法
**CNApplicationContext.java**
```java
	public CNApplicationContext(Class configClass){
        this.configClass = configClass;

        scanGetDefine(configClass);

        for(String beanName : beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName,beanDefinition); //单例Bean
                singletonObjects.put(beanName,bean); //放入单例池
            }
        }

    }
```
构造方法里的扫描路径函数的功能在上面已经实现了。
将扫描出来的Bean都存储到Bean Definition之后还要进行一个操作：
将作用域为singleton的Bean创建出来并且放入单例池，这样配合getBean方法可以确保singleton作用域的Bean对象永远只有一个。

***
## 创建Bean
先写一个简单的createBean方法：

**CNApplicationContext.java**
```java
public Object createBean(String beanName,BeanDefinition beanDefinition){
	Class clazz = beanDefinition.getClazz();
    Object instance = null;
    //通过反射获得Bean
    instance = clazz.getDeclaredConstructor().newInstance();
    return instance;
}
```
>java.lang.Class.getDeclaredConstructor（）方法返回一个构造对象，它反映此Class对象所表示的类或接口的指定构造。该parameterTypes参数是标识构造方法的形参类型，在声明的顺序Class对象的数组。

这里就是典型的通过Class.getDeclaredConstructor().newInstance()反射来获取对象。

这里有问题，如果我的构造函数是带参数的呢，并且没有无参构造函数。
涉及到无参构造函数还有另外一个问题，这个有参构造函数引用的是其他Bean，这里会有一个构造函数的依赖注入。
这些问题后面会解决。

***
## @Autowired
 >**【Spring】**
 >\
 >Spring提供了@Autowired注释来自动发现 bean 并将协作 bean（其他关联的依赖 bean）注入我们的 bean
 >
 
再添加@Autowired注解：

**Autowired.java**
```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD}) //可以加在属性、方法上使用
public @interface Autowired {
    //String value() ;
}
```
***
## 依赖注入-属性注入
如果要创建的 Bean 需要一些预设的属性，就涉及到 IOC 的依赖注入。延续 IOC 的思想，如果需要属性依赖，交给 IOC 容器找，并且赋上值。

**CNApplicationContext.java**
```java
public Object createBean(String beanName,BeanDefinition beanDefinition){
	Class clazz = beanDefinition.getClazz();
    Object instance = null;
    try {
    	//通过反射获得Bean
        instance = clazz.getDeclaredConstructor().newInstance();
        //依赖注入 反射
        for (Field declaredField : clazz.getDeclaredFields()) {
        	//如果属性上加了Autowired注解，那我就进行属性赋值
            if(declaredField.isAnnotationPresent(Autowired.class)){
            	Object bean = getBean(declaredField.getName());
                if(bean == null){
                 	throw new RuntimeException("无法注入对应的类，目标类型：" + declaredField.getType().getName());
                 }
                 declaredField.setAccessible(true);
                 declaredField.set(instance,bean);
            }
		}
```
现在可以在createBean方法里实现依赖注入了。
clazz.getDeclaredFields()可以获取自己声明的各种字段，包括public，protected，private。
如果属性上加了Autowired注解，就进行属性赋值。
使用getBean方法获取该Bean，将bean赋值给属性，完成依赖注入。

 field.setAccessible(true)方式是用来设置获取权限的。
如果 accessible 标志被设置为true，那么反射对象在使用的时候，不会去检查Java语言权限控制（private之类的）


但是这里暂时还是存在问题的。
Bean的加载有顺序，如果加载的这个Bean依赖的Bean还没有被加载怎么办。我们这里暂时选择抛出异常来提醒程序员。

***
## 再写getBean
上面写的getBean方法其实是存在问题的。
捋一下思路：
createBean方法通过反射可以实例化Bean，如果该Bean依赖了其他Bean会调用getBean方法。
之前写的getBean如果是单例的Bean直接从单例池获得就有问题了。如果这个Bean还没有被加载，单例池是获取不到的，依赖的Bean会变成null。
所以getBean方法在单例Bean的情况下要反射创建Bean，并且采用双检锁来保证并发情况下getBean单例模式的安全。

```java
	public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                //双检锁单例模式
                if(!singletonObjects.containsKey(beanName)){
                    synchronized (CNApplicationContext.class){
                        if(!singletonObjects.containsKey(beanName)){
                            Class beanClazz = beanDefinition.getClazz();
                            try {
                                Object bean = beanClazz.getDeclaredConstructor().newInstance();
                                singletonObjects.put(beanName,bean);
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //这里记得从单例池拿，而不是上面直接返回
                    return singletonObjects.get(beanName);
                }else {
                    //如果单例池里有从单例池返回
                    Object bean = singletonObjects.get(beanName);
                    return bean;
                }
            }else {
                //要New一个bean 原型bean
                Object bean = createBean(beanName,beanDefinition);
                return bean;
            }
        }else {
            throw new NullPointerException();
        }
    }
```
***
## 依赖注入-回调注入
>【Spring】
>\
>在Spring中回调注入的核心是一个叫 Aware 的接口，它来自 SpringFramework 3.1。它有一系列的子接口。
>

如果当前的 bean 需要依赖它本身的 name ，使用 @Autowired 就不好使了，这个时候就得使用 BeanNameAware 接口来辅助注入当前 bean 的 name 了
```java
/**
 * 模仿spring的
 * BeanNameAware使对象知道容器中定义的 bean名称
 */
public interface BeanNameAware {
    void setBeanName(String name);
}
```
写了一个测试类演示一下用法：
```java
import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;
import com.spring.Scope;

@Component("hhhh")
@Scope("prototype")
public class Hhhh implements BeanNameAware {

    @Autowired
    private TestService testService;

    @Autowired
    private Food food;

    private String beanName;

    @Override
    public void setBeanName(String name){
        beanName = name;
    }

    public void test(){
        //希望拿对象的时候已经赋好值了
        System.out.println("hhhh依赖的Bean1："+testService);
        System.out.println("hhhh依赖的Bean2："+food);
        System.out.println("hhhh的Bean名字是："+beanName); //通过回调得到beanName
    }
}
```
我们希望在拿到Bean的时候就有beanName了。

**CNApplicationContext.java** 的createBean()方法：加上对Aware回调的处理：
```java
//Aware回调
if(instance instanceof BeanNameAware){
	((BeanNameAware) instance).setBeanName(beanName);
}
```
***
## IOC小结
到目前为止IOC已经基本功能实现完毕了。
![](https://img-blog.csdnimg.cn/20210620031103799.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)
未使用框架前获得Hhhh对象需要new一个对象，在编译期就要保证Hhhh存在，属于强耦合。
我们使用getBean方法到运行期反射过程才知道Hhhh到底存不存在，等于将获取对象的控制权交给了Spring容器，也就是我们俗称的 控制反转（ Inverse of Control , IoC ）。

ApplicationContext 根据指定的 beanName 去获取和创建对象的过程称作：依赖查找（ Dependency Lookup , DL ）。
***
## AutowireCapableBeanFactory
前面还遗留了一些问题没有解决。

当我们反射时需要的构造函数是带参的构造函数，或者构造函数上有@Autowired注解的情况是没有处理的。

>在Spring源码里是这样处理的：
![](https://img-blog.csdnimg.cn/20210621004211722.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)
>AbstractAutowireCapableBeanFactory类里有一个createBeanInstance方法用于选择到底用哪个构造方法。
>\
>核心还是调用了ConstructorResolver类的autowireConstructor方法：
>如果只存在一个显式定义的构造函数，则使用这个构造函数；否则先基于构造函数的参数个数对所有构造函数进行降序排序，然后遍历检查这些构造函数。选中最合适的构造函数后，则进行构造函数的属性对象的注入。![](https://img-blog.csdnimg.cn/20210621004634187.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)

这个关系还是比较复杂的。

在构造函数没有被@Autowired注解的情况下，具体情况大致分为：
* 只有一个无参构造函数。使用无参初始化
* 只有一个含参构造函数。使用唯一的这个初始化
* 有若干个构造函数，但是包含一个无参构造函数。使用无参
* 有若干个构造函数，但是不包括一个无参构造函数。抛出异常，无法确定用哪个

在构造函数被@Autowired注解的情况下，@Autowired可以通过true、false来设置强依赖或者弱依赖，具体情况大致分为：
* 只有一个@Autowired修饰的构造函数。用这个构造函数
* 有多个一个@Autowired或修饰的构造函数。如果都是false，那选举，如果有一个true，抛异常。

大概可以描述为：选择能够成功注入最多bean对象的使用了@Autowired注解的构造函数，即基于贪婪的策略。










