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

# NOTE: Kind cluster has limited resources. We disable heavy components
# (alertmanager, node-exporter, kube-state-metrics, k8s component monitors)
# and use --no-hooks --skip-crds to reduce API server pressure during install.
helm upgrade --install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --skip-crds \
  --no-hooks \
  --set grafana.adminPassword=admin \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
  --set prometheus.prometheusSpec.ruleSelectorNilUsesHelmValues=false \
  --set prometheus.prometheusSpec.resources.requests.cpu=100m \
  --set prometheus.prometheusSpec.resources.requests.memory=256Mi \
  --set prometheus.prometheusSpec.resources.limits.memory=512Mi \
  --set grafana.resources.requests.cpu=50m \
  --set grafana.resources.requests.memory=128Mi \
  --set grafana.resources.limits.memory=256Mi \
  --set alertmanager.enabled=false \
  --set kubeStateMetrics.enabled=false \
  --set nodeExporter.enabled=false \
  --set kubeApiServer.enabled=false \
  --set kubelet.enabled=false \
  --set kubeControllerManager.enabled=false \
  --set kubeScheduler.enabled=false \
  --set kubeProxy.enabled=false \
  --set kubeEtcd.enabled=false \
  --set coreDns.enabled=false \
  --timeout 10m

echo "  Waiting for Prometheus Operator to create Prometheus instance..."
sleep 30

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
