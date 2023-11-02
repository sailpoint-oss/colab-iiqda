# Instrumental ID readme for IIQDA

To build, use JDK 8 or higher and Maven 3.8.6. (The Maven 3.9 series has [a bug](https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues/3014) that breaks the Eclipse "sonatype" packaging tool.)

Execute `mvn clean package`.

The generated Eclipse artifacts will be gathered in `./releng/sailpoint.IIQ_Deployment_Accelerator.update/target/repository`. This folder is an Eclipse update site structure. The local folder can be imported directly into Eclipse as an update site or it can be zipped up and passed around that way.
