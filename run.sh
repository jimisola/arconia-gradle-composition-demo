#!/bin/bash
set -e

echo "🏗️  Building and publishing convention plugins..."
echo ""

echo "📦 Building Redis convention plugin..."
(cd redis-spring-boot-gradle-convention && ./gradlew clean publishToMavenLocal -q)
echo "✅ Redis plugin published locally"

echo "📦 Building Observability convention plugin..."
(cd observability-spring-boot-gradle-convention && ./gradlew clean publishToMavenLocal -q)
echo "✅ Observability plugin published locally"

cd arconia-gradle-composition-demo-app

echo ""
echo "🧪 Running unit tests..."
./gradlew test
echo "✅ Unit tests passed"


echo ""
echo "🧪 Running integration tests..."
./gradlew integrationTest
echo "✅ Integration tests passed"

echo ""
echo "🚀 Starting application with Dev Services..."
echo "   - Redis will be started automatically"
echo "   - LGTM stack (Grafana, Loki, Tempo, Mimir) will be started automatically"
echo "   - Watch the console for the Grafana URL!"
echo ""

./gradlew bootRun
