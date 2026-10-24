# ---------- Étape 1 : compilation du WAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
RUN sed -i 's/\r$//' build-war.sh && sh build-war.sh

# ---------- Étape 2 : exécution sous Tomcat 9 (javax.servlet) ----------
FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/_build/target/livres-app.war /usr/local/tomcat/webapps/ROOT.war
# Mémoire limitée sur l'offre gratuite de Render (512 Mo)
ENV JAVA_OPTS="-Xms64m -Xmx256m"
EXPOSE 8080
CMD ["catalina.sh", "run"]
