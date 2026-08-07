# Handyman — Configure, Run & View SonarQube Coverage

Configure JaCoCo coverage scoping in `pom.xml`, run the build to generate and upload the
report, and view the result in SonarQube.

> **Disclaimer:** This guide assumes you already have a configured local Docker SonarQube
> setup (server running on `http://localhost:9000` with a valid analysis token). For details
> on the SonarQube setup itself, please refer to the **deployment_scripts** repo.

> All tokens and secrets in this document are shown as placeholders (e.g. `$SONAR_TOKEN`).
> Never paste real secrets into this file or commit them.

---

## 1. `pom.xml` change — scope coverage with `<includes>`

Configure the JaCoCo plugin so the **agent stays unfiltered** (it must record execution)
and the **`<includes>` lives only in the `report` execution** (this scopes the rendered
report to the class(es) you want covered).

Replace the `jacoco-maven-plugin` block in `<build><plugins>` with:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>

        <!-- AGENT: no <includes> here. It records execution for the test JVM. -->
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>

        <!-- REPORT: <includes> goes HERE. This is where you list the
             class files to scope coverage to. Add one <include> line
             per class you want in the report. -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
            <configuration>
                <includes>
                    <!-- One <include> per class to scope coverage to.
                         Use the class-file path glob for your own class(es),
                         e.g. **/configuration/GlobalControllerExceptionHandler.class -->
                    <include>**/path/to/YourClass.class</include>
                </includes>
            </configuration>
        </execution>

    </executions>
</plugin>
```

Ensure this property is present in `<properties>` so the scanner can find the report:

```xml
<properties>
    <sonar.coverage.jacoco.xmlReportPaths>target/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
</properties>
```

> Include patterns here use compiled **`.class`** path globs (bytecode), not `.java`.

---

## 2. Build and push to SonarQube

Run from the directory containing `pom.xml`:

```bash
mvn clean verify sonar:sonar \
  -Dtest=YourClassTest \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=$SONAR_TOKEN
```

| Fragment | Purpose |
|---|---|
| `clean verify` | Fresh build; runs tests, records coverage, generates `jacoco.xml` |
| `sonar:sonar` | Analyses the code and uploads results + coverage to the server |
| `-Dtest=...` | Runs only the target test class |
| `-Dsonar.host.url` | Points at your local SonarQube |
| `-Dsonar.token` | Authentication (dynamic env var) |

On success the log ends with:

```
ANALYSIS SUCCESSFUL, you can find the results at: http://localhost:9000/dashboard?id=in.handyman%3Araven
BUILD SUCCESS
```

---

## 3. View the result in SonarQube

1. Open <http://localhost:9000> and select the **raven** project (the Maven artifact name for handyman).
2. On the **Overview**, switch to the **Overall Code** tab (the default *New Code* tab is often empty).
3. Go to **Code** (left sidebar) and drill down to your class's source file
   (e.g. `src → main/java → … → YourClass.java`).
4. The header shows the file-level **Coverage %**. In the code view, the left gutter shows
   **green** for covered lines and **red** for uncovered; condition lines show a
   branch-coverage indicator you can hover to see which branches were exercised.
