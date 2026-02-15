#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CLUSTER_NAME="rabbitmq-poc"

echo "============================================"
echo " RabbitMQ PoC — Phase 1: 基礎設施部署"
echo "============================================"

# --- 1. Kind 叢集 ---
if kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
  echo "[INFO] Kind cluster '${CLUSTER_NAME}' already exists, skipping creation."
else
  echo "[STEP 1/6] Creating Kind cluster '${CLUSTER_NAME}' (1 control-plane + 3 workers)..."
  kind create cluster --config="${PROJECT_DIR}/kind/kind-cluster-config.yaml"
fi

kubectl cluster-info --context "kind-${CLUSTER_NAME}"

# --- 2. cert-manager ---
echo "[STEP 2/6] Installing cert-manager..."
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/latest/download/cert-manager.yaml
echo "  Waiting for cert-manager pods..."
kubectl wait --for=condition=Ready pods --all -n cert-manager --timeout=180s

# --- 3. Namespace ---
echo "[STEP 3/6] Creating namespace..."
kubectl apply -f "${PROJECT_DIR}/k8s/00-namespace.yaml"
kubectl config set-context --current --namespace=rabbitmq-poc

# --- 4. RabbitMQ Cluster Operator ---
echo "[STEP 4/6] Installing RabbitMQ Cluster Operator..."
kubectl apply -f https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml

# --- 5. Messaging Topology Operator ---
echo "[STEP 5/6] Installing Messaging Topology Operator..."
kubectl apply -f https://github.com/rabbitmq/messaging-topology-operator/releases/latest/download/messaging-topology-operator-with-certmanager.yaml

echo "  Waiting for rabbitmq-system pods..."
kubectl wait --for=condition=Ready pods --all -n rabbitmq-system --timeout=180s

# --- 6. NGINX Ingress Controller (for Kind) ---
echo "[STEP 6/6] Installing NGINX Ingress Controller..."
kubectl apply -f https://kind.sigs.k8s.io/examples/ingress/deploy-ingress-nginx.yaml
echo "  Waiting for ingress-nginx pods..."
kubectl wait --for=condition=Ready pods -l app.kubernetes.io/component=controller \
  -n ingress-nginx --timeout=180s

# --- Verify ---
echo ""
echo "============================================"
echo " 驗證結果"
echo "============================================"
echo ""
echo "--- CRDs ---"
kubectl get crd | grep rabbitmq || echo "(no rabbitmq CRDs found)"
echo ""
echo "--- Namespaces ---"
kubectl get ns rabbitmq-poc rabbitmq-system cert-manager ingress-nginx 2>/dev/null
echo ""
echo "[DONE] Phase 1 基礎設施部署完成！"
echo "  接下來請執行: ./scripts/02-deploy-topology.sh"
