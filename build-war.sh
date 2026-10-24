#!/bin/sh
# Assemble une structure Maven standard à partir des fichiers à plat du dépôt, puis compile le WAR.
# Vercel a besoin des fichiers HTML/JS/CSS à la racine du dépôt ; Maven a besoin de src/main/...
# Ce script fait le pont sans rien déplacer dans GitHub. Résultat : _build/target/livres-app.war
set -eu

ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/_build"
rm -rf "$OUT"
mkdir -p "$OUT/src/main/java" "$OUT/src/main/webapp/WEB-INF"

# 1. Classes Java : rangées dans le dossier correspondant à leur ligne "package ..."
for f in "$ROOT"/*.java; do
  name="$(basename "$f")"
  case "$name" in Test*.java) continue ;; esac   # classes de test manuelles, inutiles en production
  pkg="$(grep -m1 '^package ' "$f" | tr -d '\r' | sed 's/^package \(.*\);.*/\1/' | tr '.' '/')"
  mkdir -p "$OUT/src/main/java/$pkg"
  cp "$f" "$OUT/src/main/java/$pkg/$name"
done

# 2. Descripteur web et fichiers statiques (servis aussi par Tomcat, utile en local)
cp "$ROOT/web.xml" "$OUT/src/main/webapp/WEB-INF/web.xml"
for f in "$ROOT"/*.html "$ROOT"/*.js "$ROOT"/*.css; do
  [ -f "$f" ] && cp "$f" "$OUT/src/main/webapp/"
done

# 3. Compilation
cp "$ROOT/pom.xml" "$OUT/pom.xml"
cd "$OUT"
mvn -q -B -DskipTests clean package
ls -l "$OUT/target/livres-app.war"
