# Instrumental ID readme for IIQDA

To build, use JDK 21 or higher and Maven 3.9.6. The build *will fail* with other versions of Java or Maven. The latest versions of the Eclipse Tycho build tool requires the latest JDKs.

The generated artifacts will still be Java 8 compatible because of the compiler settings used.

Execute `mvn clean package`.

The generated Eclipse artifacts will be gathered in `./releng/sailpoint.IIQ_Deployment_Accelerator.update/target/repository`. This folder is an Eclipse update site structure. The local folder can be imported directly into Eclipse as an update site or it can be zipped up and passed around that way.
