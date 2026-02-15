#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
APP_DIR="${PROJECT_DIR}/app"
K8S_DIR="${PROJECT_DIR}/k8s"
IMAGE_NAME="rabbitmq-poc-app:latest"
CLUSTER_NAME="rabbitmq-poc"

echo "============================================"
echo " RabbitMQ PoC — Phase 6: Build & Deploy App"
echo "============================================"

# --- 1. Build ---
echo "[STEP 1/3] Building Spring Boot application..."
cd "${APP_DIR}"
./gradlew clean bootJar -x test
cd "${PROJECT_DIR}"

# --- 2. Docker build + load into Kind ---
echo "[STEP 2/3] Building Docker image and loading into Kind..."
docker build -t "${IMAGE_NAME}" "${APP_DIR}"
kind load docker-image "${IMAGE_NAME}" --name "${CLUSTER_NAME}"

# --- 3. Deploy ---
echo "[STEP 3/3] Deploying app to Kubernetes..."
kubectl apply -f "${K8S_DIR}/11-app-deployment.yaml"
echo "  Waiting for app pods to be ready..."
kubectl rollout status deployment/rabbitmq-poc-app -n rabbitmq-poc --timeout=120s

# --- Verify ---
echo ""
echo "============================================"
echo " 驗證結果"
echo "============================================"
echo ""
kubectl get pods -l app=rabbitmq-poc-app -n rabbitmq-poc
echo ""
echo "[DONE] App deployed!"
echo ""
echo "  測試方式:"
echo "  1. Port-forward: kubectl port-forward svc/rabbitmq-poc-app 8080:8080 -n rabbitmq-poc"
echo "  2. Ingress:      curl -X POST 'http://app.rabbitmq.local/api/demo/simple?msg=hello'"
echo "  3. 或使用: ./scripts/04-deploy-monitoring.sh 部署監控"
