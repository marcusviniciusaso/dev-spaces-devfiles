# Roteiro de Validação — Java multi versão no Dev Spaces

Este documento descreve o passo a passo para validar os cinco JDKs, a troca de versão com
`use-java` e `with-java`, o pareamento com Gradle e Spring Boot CLI, a persistência da escolha e o
`devspaces-setup` dentro de um workspace OpenShift Dev Spaces 3.28 usando a imagem
`custom-udi-java`.

---

## 1. Ferramentas

Num terminal novo, antes de qualquer `use-java`:

```bash
devspaces-environment
java -version                       # 25.0.4.1 (default)
echo $JAVA_HOME                     # /usr/lib/jvm/java-25-openjdk
mvn -version | head -n 1            # Apache Maven 3.9.12
gradle --version | grep '^Gradle'   # Gradle 9.3.1
spring --version                    # Spring CLI v4.0.3
```

---

## 2. use-java

```bash
for v in 8 11 17 21 25; do
  echo "-- Java $v --"
  use-java $v
  echo "$JAVA_HOME"
  mvn -version | grep 'Java version'
  gradle --version | grep '^Gradle'
  spring --version
done
```

| `use-java` | `java -version` | `JAVA_HOME` | Gradle | Spring Boot CLI |
| --- | --- | --- | --- | --- |
| 8 | 1.8.0_492 | `/usr/lib/jvm/java-1.8.0-openjdk` | 7.6.6 | 2.7.18 |
| 11 | 11.0.25 | `/usr/lib/jvm/java-11-openjdk` | 7.6.6 | 2.7.18 |
| 17 | 17.0.19 | `/usr/lib/jvm/java-17-openjdk` | 8.14.4 | 4.0.3 |
| 21 | 21.0.11 | `/usr/lib/jvm/java-21-openjdk` | 8.14.4 | 4.0.3 |
| 25 | 25.0.4.1 | `/usr/lib/jvm/java-25-openjdk` | 9.3.1 | 4.0.3 |

O `Java version` do Maven acompanha a versão ativa em todas as linhas.

```bash
use-java 9                          # uso: use-java 8|11|17|21|25
```

---

## 3. with-java

```bash
use-java 25
with-java 8 java -version           # 1.8.0_492
with-java 11 gradle --version | grep -E '^Gradle|JVM'   # Gradle 7.6.6, JVM 11
java -version                       # 25: o terminal não mudou
cat /home/user/persistent/.java-version                 # 25: nada foi gravado
```

---

## 4. Persistência

```bash
use-java 17
cat /home/user/persistent/.java-version                 # 17
```

Abra um **novo terminal**:

```bash
java -version                       # 17.0.19
gradle --version | grep '^Gradle'   # Gradle 8.14.4
devspaces-environment               # "Novos terminais: 17 (/home/user/persistent/.java-version)"
```

Pause e retome o workspace e repita o bloco acima: continua em 17. Terminais que já estavam
abertos antes do `use-java` mantêm a versão que tinham.

Volte ao default:

```bash
use-java 25
```

---

## 5. Sample

```bash
cd /projects/dev-spaces-devfiles/custom-udi-java/sample
for v in 8 11 17 21 25; do
  echo "-- Java $v --"
  with-java $v mvn -q clean package
  with-java $v java -jar target/java-multi-version-sample-1.0.0.jar
  with-java $v gradle -q run
done
```

Cada versão imprime duas vezes a linha `Java <versão> (...) em <java.home>`, uma pelo jar do Maven
e outra pelo Gradle. O aviso do `javac` de que o nível 8 está obsoleto é esperado no Java 21 e
no 25.

---

## 6. devspaces-setup

Com a URL do devfile ainda como placeholder:

```bash
devspaces-setup                     # aborta sem pedir credenciais
```

Com um repositório Maven público no lugar do corporativo (qualquer usuário e senha servem):

```bash
ARTIFACTORY_MAVEN_BASEURL=https://repo.maven.apache.org/maven2/ devspaces-setup
ls -l /home/user/persistent/.m2/settings.xml            # modo 600
```

---

## 7. Extensão Java

Instale as extensões sugeridas em `sample/.vscode/extensions.json` e abra `App.java`. Em
**Java: Configure Java Runtime** (Command Palette) devem aparecer os cinco JDKs de `/usr/lib/jvm`,
detectados sem nenhum `settings.json`. Na view **Testing**, o `AppTest` deve executar com check
verde.
