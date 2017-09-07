# GsonFormatKotlin

------

最新项目用到了Kotlin, 虽然Android Studio 3.0 Preview 比较方便的将Java代码可以转换为Kotlin代码, 但是在写model的时候还是比较坑,可能我优先在一个java文件用[**GsonFormat**](https://github.com/zzz40500/GsonFormat)将一个Json字符串转换为对象结构, 然后再转为Kotlin, 繁琐麻烦, 搜了下Android Studio在线的Plugins,有一个[**JsonToKotlinClass**](https://github.com/wuseal/JsonToKotlinClass) ，不过主要针对的是data的数据，并且必须先输入文件名，和我想要的有出入，于是造了个轮子

### 1. 选择打开
本地安装好后选择code menu, 然后选择GsonFormatKotlin

![tool-manager](http://ww1.sinaimg.cn/large/5dfcd11agy1fjb3pjoy78j20vi0wg7b6.jpg)

### 2. 输入Json字符串

![tool-manager](http://ww1.sinaimg.cn/large/5dfcd11agy1fjb3uh2xe4j21fc0mqq4u.jpg)

比如下面这段是一个孩子的json字符串
```java
       {
          "imgUrl": null,
          "times": "1",
          "gender": "0",
          "name": "江润之",
          "days": "6",
          "id": "d38e1d3dc1a9496985d4528ef70cf51c",
          "status": "1"
        }
```
- [ ] 不选择 "is data？"

```java
        class ChildrenEntity {
            var imgUrl: String? = null
    		var times: String? = null
    		var gender: String? = null
    		var name: String? = null
    		var days: String? = null
    		var id: String? = null
    		var status: String? = null
		}
```
- [x] 选择 "is data？"

```java
        data class ChildrenEntity (
           	val imgUrl: String,
		    val times: String,
		    val gender: String,
		    val name: String,
		    val days: String,
		    val id: String,
		    val status: String
		)
```

页面比较丑，本来想模仿[**GsonFormat**](https://github.com/zzz40500/GsonFormat)写一个获取当前class包名+名称的数据，但是看了源码，自己研究了下，发现在class文件没问题，但是koltin就会报错:
> org.jetbrains.kotlin.psi.KtFile cannot be cast to com.intellij.psi.impl.source.PsiJavaFileImpl

原因应该很明显，暂时也没发现有类似PsiKotlinFileImpl这样的类名，只能后面作罢

后面有机会还是完善一下功能，其实不算难，但是要做更加完善和优美的功能，还是要花时间去看[**api**](http://www.jetbrains.org/intellij/sdk/docs/reference_guide.htmlApi)和java swing的一些布局相关的知识

Thanks to [**GsonFormat**](https://github.com/zzz40500/GsonFormat) [**JsonToKotlinClass**](https://github.com/wuseal/JsonToKotlinClass)