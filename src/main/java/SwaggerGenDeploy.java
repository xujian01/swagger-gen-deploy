import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xujian03
 * @date 2020/3/17 09:42
 * @description
 */
@Slf4j
public class SwaggerGenDeploy {
    public static void main(String[] args) {
        //api-spec位置
        String yamlPath = args[0];
        Yaml yaml = new Yaml();
        Map<String, Object> properties = null;
        try {
            log.info("正在加载swagger文件...");
            properties = yaml.loadAs(new FileInputStream(new File(yamlPath)), HashMap.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String groupId = (String) properties.get("groupId");
        String artifactId = (String) properties.get("artifactId");
        String version = (String) properties.get("version");
        log.info("正在检查目录...");
        File tmpDir = new File("/tmp/swagger-tmp/"+artifactId);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        try {
            String command1 = "swagger-codegen generate -i "+yamlPath+" -l spring --library spring-cloud -o /tmp/swagger-tmp/"+artifactId+" --api-package "+groupId+".client  --invoker-package "+groupId+".client.invoker --model-package "+groupId+".client.model --group-id="+groupId+" --artifact-id="+artifactId+" --artifact-version="+version;
            log.info("正在生成代码...");
            Process process1 = Runtime.getRuntime().exec(command1);
            String s1;
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            while ((s1=reader1.readLine()) != null) {
                log.info(s1);
            }
            process1.waitFor();

            /*
            修改pom文件，plugin标签增加以下节点
            <configuration>
                    <includes>
                        <include>
                            <groupId>nothing</groupId>
                            <artifactId>nothing</artifactId>
                        </include>
                    </includes>
                    <skip>true</skip>
                </configuration>
             */
            File file = new File(tmpDir,"pom.xml");
            DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = newDocumentBuilder.parse(file);
            Element root = doc.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("plugin");
            Node plugin = nodeList.item(0);
            Element configuration = doc.createElement("configuration");
            Element includes = doc.createElement("includes");
            Element include = doc.createElement("include");
            Element groupIdTag = doc.createElement("groupId");
            groupIdTag.setTextContent("nothing");
            Element artifactIdTag = doc.createElement("artifactId");
            artifactIdTag.setTextContent("nothing");
            include.appendChild(groupIdTag);
            include.appendChild(artifactIdTag);
            includes.appendChild(include);
            configuration.appendChild(includes);
            Element skip = doc.createElement("skip");
            skip.setTextContent("true");
            configuration.appendChild(skip);
            plugin.appendChild(configuration);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(file);
            transformer.transform(source, result);

            log.info("正在发布代码...");
            //仓库地址
            String repoUrl = args[1];
            //仓库id
            String repoId = args[2];
            String command2 = "mvn deploy:deploy-file -DgroupId="+groupId+" -DartifactId="+artifactId+" -Dversion="+version+" -Dfile=/tmp/swagger-tmp/"+artifactId+"/pom.xml -Dpackaging=pom -Durl="+repoUrl+" -DrepositoryId="+repoId+" -DskipTests";
            Process process2 = Runtime.getRuntime().exec(command2);
            String s2;
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            while ((s2=reader2.readLine()) != null) {
                log.info(s2);
            }
            process2.waitFor();
            log.info("发布成功!");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("执行失败！",e);
        }
    }
}
