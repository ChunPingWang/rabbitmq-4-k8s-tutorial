#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
K8S_DIR="${PROJECT_DIR}/k8s"

echo "============================================"
echo " RabbitMQ PoC — Phase 7: 監控與告警"
echo "============================================"

# --- 1. Install kube-prometheus-stack ---
echo "[STEP 1/4] Installing kube-prometheus-stack (Prometheus + Grafana)..."
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts 2>/dev/null || true
helm repo update

helm upgrade --install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.adminPassword=admin \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
  --set prometheus.prometheusSpec.ruleSelectorNilUsesHelmValues=false \
  --wait --timeout 5m

# --- 2. Label monitoring namespace ---
echo "[STEP 2/4] Labeling monitoring namespace for NetworkPolicy..."
kubectl label namespace monitoring kubernetes.io/metadata.name=monitoring --overwrite

# --- 3. ServiceMonitor ---
echo "[STEP 3/4] Applying ServiceMonitor and AlertRules..."
kubectl apply -f "${K8S_DIR}/12-service-monitor.yaml"
kubectl apply -f "${K8S_DIR}/13-alert-rules.yaml"

# --- 4. Ingress ---
echo "[STEP 4/4] Applying Ingress rules..."
kubectl apply -f "${K8S_DIR}/14-ingress.yaml"

# --- Import Grafana Dashboard ---
echo ""
echo "============================================"
echo " 監控部署完成"
echo "============================================"
echo ""
echo "  Grafana: http://grafana.rabbitmq.local (admin/admin)"
echo "  或 port-forward: kubectl port-forward svc/kube-prometheus-stack-grafana 3000:80 -n monitoring"
echo ""
echo "  RabbitMQ Dashboard ID: 10991 (匯入到 Grafana)"
echo ""
echo "  /etc/hosts 設定:"
echo "    127.0.0.1  app.rabbitmq.local mgmt.rabbitmq.local grafana.rabbitmq.local"
