 A interceptor for Mybatis log printing, it will print our sql statement complete on the console, which can let us better see whether there is a problem with the sql we wrote.

 ![image](https://github.com/user-attachments/assets/f4dc17c6-46f4-4e45-a439-3ff53c5444f8)

 As can be seen from the figure, the white font is the Mybatis log configured by us, and the purple font is the log printed out by our interceptor. We can see that the purple is more complete.

 Rely on the maven central repository that has been published:

````xml
<dependency>
  <groupId>io.github.huazhaoyishiba</groupId>
  <artifactId>mybatis-sql-interceptor</artifactId>
  <version>1.1.3</version>
</dependency>
````

 一个关于Mybatis日志打印的拦截器,它会将我们的sql语句完整的打印在控制台上,可以让我们更好的查看自己写的sql是否存在问题
 
  ![image](https://github.com/user-attachments/assets/f4dc17c6-46f4-4e45-a439-3ff53c5444f8)
  
 从图上可以看到,白色字体的是我们配置的Mybatis的日志, 紫色字体的是我们的拦截器打印出来的日志, 我们能看出紫色的更加完整

 依赖已经发布maven中央仓库:

  ````xml
<dependency>
   <groupId>io.github.huazhaoyishiba</groupId>
   <artifactId>mybatis-sql-interceptor</artifactId>
   <version>1.1.3</version>
</dependency>
````

