# swagger-gen-deploy

一键执行：swagger代码自动生成+发布到maven私有仓库

## 注意：对于引入了maven依赖的maven项目，想要用java -jar的形式来执行，则需在pom文件配置如下：
```xml
<build>
        <plugins>
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <configuration>
                    <appendAssemblyId>false</appendAssemblyId>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <manifest>
                            <!-- 此处指定main方法入口的class -->
                            <mainClass>SwaggerGenDeploy</mainClass>
                        </manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>assembly</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```
否则会报错找不到对应的包（NoClassDefinitionException）
