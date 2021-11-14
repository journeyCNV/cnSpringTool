# cnSpringTool
My spring frame

核心代码在spring文件夹下，cnn文件夹是用于测试功能的

***

不用看这个文档了，后面计划把开发思想写到注释里，这样写文档太累了~
这个文档已经严重落后于开发进度。

***

开发文档 持续更新 

PS：开发文档记录速度比实际开发进度要慢展示的也不完全是实际代码，此文档不代表实际开发思路，主要是回头复盘。。。bug是一直都在修

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

这一块的实现还是比较复杂的，又创建了三个类来支持这个功能的实现。

![](https://img-blog.csdnimg.cn/2021062202035813.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)


核心部分是这个方法：
```java
public BeanSupport createBeanInstance(String beanName,Class<?> clazz) {
        Constructor<?>[] cons = clazz.getDeclaredConstructors();

        int consLength = cons.length;

        ArrayList<Object> paramList = new ArrayList<>();
        ArrayList<String> paramBeanNames = new ArrayList<>();
        boolean mayHasBean = false; //是否可能有依赖的Bean存在
        
        if(consLength==1){
            Parameter[] params = cons[0].getParameters(); //Java8的Parameter类
            if(params==null){ //如果唯一的构造函数是无参构造，直接使用无参
                return new BeanSupport(cons[0].newInstance(),null,false,null);
            }
            for (Parameter param : params) {
                if(param.getType().isPrimitive()) { //如果是基本类型
                    if (param.getType() == Boolean.class) {
                        paramList.add(false);
                    } else {
                        paramList.add(0);
                    }
                }else {
                    paramList.add(null);
                    paramBeanNames.add(param.getName());
                    mayHasBean = true;
                }
            }
            if(!mayHasBean)
                return new BeanSupport(cons[0].newInstance(paramList.toArray()),null,mayHasBean,null);
            String[] beanNames = new String[paramBeanNames.size()];
            return new BeanSupport(cons[0].newInstance(paramList.toArray()),paramBeanNames.toArray(beanNames),mayHasBean,null);
        }else {
            int countAutowired = 0; //记录有多少个注解
            Constructor<?> noParamCon = null;
            Constructor<?> recCon = null;
            int recAutowiredTrue = 0;
            int recAutowiredFalse = 0;
            ArrayList<Constructor<?>> AutowiredFalseCons = new ArrayList<>();//存储可能要被选举的实例化方法
            for (Constructor<?> con : cons) {
                if(con.isAnnotationPresent(Autowired.class)) {
                    if(con.getDeclaredAnnotation(Autowired.class).required()){
                        recAutowiredTrue++;
                        recCon = con;
                    }else {
                        recAutowiredFalse++;
                        AutowiredFalseCons.add(con);
                    }
                    countAutowired++;
                }
                if(con.getParameters().length==0) {
                    noParamCon = con;
                }
            }
            //有多个构造并且没有注解，有无参构造
            if(countAutowired==0&&noParamCon!=null){
                return new BeanSupport(noParamCon.newInstance(),null,false,null);
            }
            //有且仅有一个true的Autowired
            if(recAutowiredTrue==1&&recAutowiredFalse==0){
                //只有一个Autowired注解是true,并且没有其他
                //处理recCon,解析参数
                Parameter[] parameters = recCon.getParameters();
                if(parameters==null){
                    return new BeanSupport(recCon.newInstance(),null,false,null);
                }
                for (Parameter parameter : parameters) {
                    if(parameter.getType().isPrimitive()) { //如果是基本类型
                        paramList.add(parameter.getType()==Boolean.class?false:0);
                    }else {
                        paramList.add(null);
                        paramBeanNames.add(parameter.getName());
                        mayHasBean = true;
                    }
                }
                String[] beanNames = new String[paramBeanNames.size()];
                return new BeanSupport(recCon.newInstance(paramList.toArray()),paramBeanNames.toArray(beanNames),mayHasBean,null);
            }
            if(recAutowiredTrue==0&&recAutowiredFalse>=0){
                //要在一堆false中进行选举，选能加载Bean更多的那个
                BeanSupportCandidate beanSupportCandidate = new BeanSupportCandidate(AutowiredFalseCons,AutowiredFalseCons.size(),noParamCon);
                return new BeanSupport(null,null,true,beanSupportCandidate);
            }
        }
        throw  new BeansException("无法确定使用 "+beanName+" Bean的哪个构造方法");
    }
```

## 依赖注入-构造注入
核心代码：
**CNApplicationContext.java** 的 createBean方法
```java
            if (beanSupport.getInstance()!=null) { //不用选举，已经确定
                instance = beanSupport.getInstance();
                if (beanSupport.isMayHasBean()) {
                    for (String name : beanSupport.getBeanNames()) {
                        if (containsBean(name)) {
                            Object beanNeed = getBean(name);
                            Field field = clazz.getDeclaredField(name);
                            field.setAccessible(true);
                            field.set(instance,beanNeed);
                        }
                    }
                }
            }else {
                Constructor<?> hasMaxNumBeanCon=null;
                int currentMax=0;
                for (Constructor<?> candidate : beanSupport.getCandidate().getCandidates()) {
                    int count = 0;
                    for (Parameter parameter : candidate.getParameters()) {
                        count += containsBean(parameter.getName())?1:0;
                    }
                    if(count>currentMax){
                        currentMax = count;
                        hasMaxNumBeanCon = candidate;
                    }
                }
                Parameter[] params = hasMaxNumBeanCon.getParameters();
                ArrayList<Object> paramList = new ArrayList<>();
                for (Parameter param : params) {
                    if(param.getType().isPrimitive()) { //如果是基本类型
                        if (param.getType() == Boolean.class) {
                            paramList.add(false);
                        } else {
                            paramList.add(0);
                        }
                    }else {
                        if (containsBean(param.getName())) {
                            paramList.add(getBean(param.getName()));
                        }else{
                            paramList.add(null);
                        }
                    }
                }
                instance = hasMaxNumBeanCon.newInstance(paramList.toArray());
            }
```

到此 IoC已经基本实现结束。
***
# AOP实现
***
## InitializingBean
这并不是AOP实现的一部分，但是和后面的BeanPostProcessor有关系，就放在这里了。

```java
/**
 * 模仿Spring
 * InitializingBean接口为bean提供了属性初始化后的处理方法，
 * 它只包括afterPropertiesSet方法，凡是继承该接口的类，在bean的属性初始化后都会执行该方法。
 */
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
```
InitialingBean接口只有一个方法，在bean实例化后会进行初始化。
![](https://img-blog.csdnimg.cn/20210624141743714.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)
```java
			//初始化
            if(instance instanceof InitializingBean){
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
```
依赖注入完成后，bean实例化完毕后，会进行Bean的初始化，调用InitializinngBean的afterPropertiesSet()方法。

***
## BeanPostProcessor
为什么要在这里写BeanPostProcessor，其实BeanPostProcessor为我们实现AOP提供了基础，在Spring源码里关于AOP的部分追踪到最后发现关系到BeanPostProcessor。

```java
/**
 * BeanPostProcessor 是一个回调机制的扩展点，
 * 它的核心工作点是在 bean 的初始化前后做一些额外的处理
 * （预初始化 bean 的属性值、注入特定的依赖，甚至扩展生成代理对象等）
 */
public interface BeanPostProcessor {

    /**
     * 初始化前
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 初始化后
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessAfterInitialization(Object bean, String beanName)  {
        return bean;
    }

}
```
在createBean方法里体现了这个初始化前后的操作：
![](https://img-blog.csdnimg.cn/20210624144231609.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)
我们在ApplicationContext里还设置了一个List。
```java
private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
```
这个List获取的时间比创建Bean更早。

```java
			//当前class对象是否实现了BeanPostProcessor接口
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor instanceBpp = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instanceBpp);
                            }
```
在bean扫描的时候就去进行判断了。

***
## 设计AOP的思路
AOP基于代理模式，对原来的业务进行增强，当我们想在不修改源码的情况下增强原来的功能，那么就可以对原来的业务类生成一个代理的对象，在代理对象中实现方法对原来的业务增强。

这里我们实现框架，显然会使用动态代理完成需求。
Java中通常有两种代理方式，一个是jdk自带的，一个是cglib实现的。

这里先来写一个测试：

```java
@Component("beanProcessor")
public class NNBeanProcessor implements BeanPostProcessor {

    @Override  //Bean初始化前方法
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if(beanName.equals("hhhh")){
            ((Hhhh)bean).setBeanName("ohhh");
        }
        return bean;
    }

    @Override //Bean初始化后方法
    public Object postProcessAfterInitialization(Object bean, String beanName)  {
        /**
         * 实现AOP
         * 在Spring中开启AOP：@EnableAspectJAutoProxy
         * 在Spring源码中,开启AOP就是通过上面的注解最终向容器里注册一个BeanPostProcessor的Bean对象
         */
        if (beanName.equals("food")) {
            Object proxyInstance = Proxy.newProxyInstance(NNBeanProcessor.class.getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        System.out.println("food用到的代理逻辑");
                        /**
                         * 可以找切点，然后执行切点的方法 ……
                         */
                        return method.invoke(bean,args);//这里会调用被代理对象的业务方法
                    });

            return proxyInstance;
        }
        return bean;
    }
}
```

动态代理利用Java的反射技术在运行时创建一个实现某些给定接口的新类(也称"动态代理类")及其实例(对象)。
代理的是接口，不是类。

动态代理用于解决一个接口的实现在编译时无法知道，需要在运行时才能实现。

>`Proxy.newProxyInstance()`方法有3个参数：
>* 类加载器
>* 返回的对象需要实现哪些接口
>* 调用处理器

调用处理器就是**InvocationHandler**类，用于激发动态代理类的方法。

按照上面的写法会有下面这样的效果：当调用实现类的方法时，都会带上代理的逻辑。

>![](https://img-blog.csdnimg.cn/2021062617483820.png)
> FoodImpl是Food的实现子类，打印的“food用到的代理逻辑”是动态代理里设置的，method.invoke会被替换成具体的方法，从food继承来的test和eat方法。代理过的对象的业务方法就像被自动统一加上一段代码一样。
>![](https://img-blog.csdnimg.cn/20210626174820113.png)
现在有AOP的感觉了吧

***
## 实现AOP
上面的测试使用的是JDK自带的代理，也提到了还有cglib。
Spring对这两种代理方式都支持，在默认的情况下，如果bean实现了一个接口，使用JDK代理，如果没有那就用cglib代理。

>**JDK动态代理和CGLIB字节码生成的区别**
>* JDK动态代理只能对实现了接口的类生成代理，不能针对类
>* CGLIB是针对类实现代理，主要是针对指定的类生成一个子类，覆盖其中的方法，并覆盖其中方法实现增强，因为采用的是继承，所以该类或方法最好不要声明为final。
>\
>PS: 随着JDK版本升级，jdk代理效率都在提升，甚至已经高于cglib

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    /**
     * 标记在实现代理功能的类上
     */
    public String value() default "";

}
```
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Before {

}
```
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface After {
}
```

```java
public class ProxyCreator implements BeanPostProcessor {

    private final String aspectBeanName; //要增强的目标bean的名字 比如说food

    private final Class clazz; //被@Aspect注解的Class

    public ProxyCreator(String aspectBeanName,Class clazz){
        this.aspectBeanName = aspectBeanName;
        this.clazz = clazz;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if(beanName.equals(aspectBeanName)) {
            Object proxyInstance = Proxy.newProxyInstance(ProxyCreator.class.getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        Method[] classMethods = clazz.getDeclaredMethods();
                        //前置方法
                        //如果带了@before注解
                        //在这里调用
                        for (Method classMethod : classMethods) {
                            if (classMethod.isAnnotationPresent(Before.class))
                                classMethod.invoke(clazz.newInstance());
                        }
                        Object obj = method.invoke(bean, args); //会根据被继承的接口的方法来
                        //后置方法
                        //如果带了@after注解
                        //在这里调用
                        for (Method classMethod : classMethods) {
                            if (classMethod.isAnnotationPresent(After.class))
                                classMethod.invoke(clazz.newInstance());
                        }
                        return obj;
                    });
            return proxyInstance;
        }
        return null;
    }

}
```
![](https://img-blog.csdnimg.cn/20210627025237849.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTc4MTM4,size_16,color_FFFFFF,t_70)
Method 类的 invoke() 方法在指定的对象上，以指定的参数调用此 Method 对象表示的底层方法。单个参数自动匹配原始形式参数。基本参数和引用参数都根据需要进行方法调用转换。










