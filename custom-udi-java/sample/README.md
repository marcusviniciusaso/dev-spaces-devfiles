# Sample Java multi versão — Dev Spaces

Projeto Java mínimo para validar, dentro de um workspace OpenShift Dev Spaces, a troca de versão
do Java da imagem `custom-udi-java` (8, 11, 17, 21 e 25).

O código é compilado para bytecode Java 8 e não tem dependência de runtime, então o mesmo projeto
compila e roda nos cinco JDKs, com Maven ou com Gradle. A aplicação imprime a versão do Java que a
está executando.

## Execução

```bash
cd /projects/dev-spaces-devfiles/custom-udi-java/sample

# Maven
mvn -q clean package
java -jar target/java-multi-version-sample-1.0.0.jar

# Gradle
gradle -q run
gradle test
```

## Troca de versão

```bash
use-java 17                  # troca a versão ativa (fica salva para os próximos terminais)
mvn -q clean package && java -jar target/java-multi-version-sample-1.0.0.jar

with-java 8 gradle -q run    # só este comando roda em Java 8
java -version                # o terminal continua em Java 17
```

| Java | Gradle usado | Spring Boot CLI |
| --- | --- | --- |
| 8 e 11 | 7.6.6 | 2.7.18 |
| 17 e 21 | 8.14.4 | 4.0.3 |
| 25 (default) | 9.3.1 | 4.0.3 |

## Estrutura

```
.vscode/
└── extensions.json                 # Extensões recomendadas (Open VSX)
pom.xml                             # source/target 8, JUnit 5
build.gradle                        # plugin application, sourceCompatibility 1.8
settings.gradle
src/
├── main/java/com/example/App.java      # imprime a versão do Java em execução
└── test/java/com/example/AppTest.java
```

## Sobre a versão do Java

O `pom.xml` usa `maven.compiler.source`/`target` em vez de `release` porque o `javac` do Java 8
não aceita `--release`. Num projeto que só compila em Java 9 ou superior, prefira
`<maven.compiler.release>`.

No Java 21 e no 25 o `javac` avisa que o nível 8 está obsoleto. É só um aviso: a compilação
termina normalmente.
