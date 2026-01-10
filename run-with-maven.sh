#!/bin/bash
set -e

echo "🏗️  Building and publishing Maven mixins..."
echo ""

echo "📦 Building Redis mixin..."
(cd redis-spring-boot-maven-mixin && ./mvnw clean install -q)
echo "✅ Redis mixin installed locally"

echo "📦 Building Observability mixin..."
(cd observability-spring-boot-maven-mixin && ./mvnw clean install -q)
echo "✅ Observability mixin installed locally"

cd arconia-composition-demo-app

echo ""
echo "🧪 Running tests..."
./mvnw test
echo "✅ Tests passed"

echo ""
echo "🚀 Starting application with Dev Services..."
echo "   - Redis will be started automatically"
echo "   - LGTM stack (Grafana, Loki, Tempo, Mimir) will be started automatically"
echo "   - Watch the console for the Grafana URL!"
echo ""

./mvnw spring-boot:run
