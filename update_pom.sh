#!/bin/bash
# Вставляем конфигурацию плагина в pom.xml
sed -i '/<build>/a\    <plugins>\n        <plugin>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-maven-plugin</artifactId>\n            <configuration>\n                <executable>true</executable>\n                <mainClass>com.diplomagenerator.DiplomaGeneratorApplication</mainClass>\n            </configuration>\n            <executions>\n                <execution>\n                    <goals>\n                        <goal>repackage</goal>\n                    </goals>\n                </execution>\n            </executions>\n        </plugin>\n    </plugins>' pom.xml
echo "✅ pom.xml обновлен"
