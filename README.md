# HotfixDemo
一个简单的热修复demo，仅为学习热修复原理用

热修复

1.阿里系：DeXposed。andfix
	从底层C的二进制来入手的。

2.腾讯系：QQ空间超级补丁，微信的tinker
	Java类加载机制来入手的。
	
此demo也是根据类加载机制入手，仿照腾讯的热修复原理，仅仅是个demo
项目中使用还是要借助阿里和腾讯。
如何实现呢？实现的原理？
从Java的类加载机制来入手的。
classLoader

安卓是如何加载classes.dex文件，启动程序。

public class PathClassLoader extends BaseDexClassLoader {
用来加载应用程序的dex

public class DexClassLoader extends BaseDexClassLoader {
可以加载指定的某个dex文件。（限制：必须要在应用程序的目录下面）

修复方案：搞多个dex。
第一个版本：classes.dex
修复后的补丁包：classes2.dex（包涵了我们修复xxx.class）

这种实现方式也可以用于插件开发。

2.如果可以解决这个问题：把两个dex合并---将修复的class替换原来出bug的class.

通过BaseDexClassLoader调用findClass(className)
Class<?> findClass(String name)

将修复好的dex插入到dexElements的集合，位置：出现bug的xxx.class所在的dex的前面。

List of dex/resource (class path) elements.
Element[] dexElements;存储的是dex的集合

最本质的实现原理：类加载器去加载某个类的时候，是去dexElements里面从头往下查找的。
fixed.dex,classes1.dex,classes2.dex,classes3.dex


=================AS打包multidex(官方待验证)============================
1.
```
dependencies {
    compile 'com.android.support:multidex:1.0.1'
}
```
2.
```
defaultConfig {
        multiDexEnabled true
    }
```
3.
```
buildTypes {
    release {
        multiDexKeepFile file('dex.keep')
        def myFile = file('dex.keep')
        println("isFileExists:"+myFile.exists())
        println "dex keep"
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
    }
}
```
4.
```Java
public class MyApplication extends Application{

	@Override
	protected void attachBaseContext(Context base) {
		// TODO Auto-generated method stub
		MultiDex.install(base);
	}

}
```
```Java
class BaseDexClassLoader{
	DexPathList pathList;


}
```
```Java
class DexPathList{
	Element[] dexElements;
}
```
源码链接：
http://androidxref.com/4.4.2_r1/xref/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java#pathList
http://androidxref.com/4.4.2_r1/xref/libcore/dalvik/src/main/java/dalvik/system/DexPathList.java
提供参考源码解析文章阅读：
http://blog.csdn.net/ch15851302205/article/details/44671687

Element[] dexElements;原来的
Element[] dexElements2;合并以后的

1.找到MyTestClass.class
	HotFixDemo\app\build\intermediates\classes\debug\com\wzhy\hotfixdemo\test
	
2.配置dx.bat的环境变量
	Android\sdk\build-tools\23.0.3\dx.bat
	
3.命令
dx --dex --output=D:\Users\techfit\Desktop\dex\classes2.dex D:\Users\techfit\Desktop\dex

注意：要创建dex目录，将需要更新的class文件放在相应的包目录中（com\wzhy\hotfixdemo\test，要创建包结构）

命令解释：
	--output=D:\Users\techfit\Desktop\dex\classes2.dex   指定输出路径
	D:\Users\techfit\Desktop\dex    最后指定去打包哪个目录下面的class字节文件(注意要包括全路径的文件夹，也可以有多个class)
