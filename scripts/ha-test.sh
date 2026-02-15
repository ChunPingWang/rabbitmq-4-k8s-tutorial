#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="rabbitmq-poc"
APP_URL="${APP_URL:-http://localhost:8080}"

echo "============================================"
echo " RabbitMQ PoC — HA 故障測試"
echo "============================================"
echo ""
echo "  請先確認 port-forward 已啟動:"
echo "  kubectl port-forward svc/rabbitmq-poc-app 8080:8080 -n ${NAMESPACE}"
echo ""

# --- Step 1: Check cluster status ---
echo "=== Step 1: 確認叢集狀態 ==="
kubectl exec rabbitmq-poc-cluster-server-0 -n "${NAMESPACE}" -- rabbitmqctl cluster_status
echo ""

# --- Step 2: Send messages continuously in background ---
echo "=== Step 2: 持續發送訊息（背景執行 30 秒）==="
SENT=0
(
  for i in $(seq 1 60); do
    curl -s -X POST "${APP_URL}/api/demo/simple?msg=ha-test-${i}" > /dev/null 2>&1 && SENT=$((SENT+1))
    sleep 0.5
  done
  echo "[HA-Test] Messages sent: approximately 60"
) &
SENDER_PID=$!

sleep 3

# --- Step 3: Kill a node ---
echo "=== Step 3: 停掉一個節點 ==="
kubectl delete pod rabbitmq-poc-cluster-server-1 -n "${NAMESPACE}" --wait=false
echo "  Pod rabbitmq-poc-cluster-server-1 deleted"

# --- Step 4: Wait and observe ---
echo "=== Step 4: 等待 30 秒，觀察訊息是否持續送達 ==="
sleep 30

# --- Step 5: Check recovery ---
echo "=== Step 5: 檢查叢集恢復 ==="
echo "--- Pods ---"
kubectl get pods -n "${NAMESPACE}" -l app.kubernetes.io/name=rabbitmq-poc-cluster
echo ""
echo "--- Cluster Status ---"
kubectl exec rabbitmq-poc-cluster-server-0 -n "${NAMESPACE}" -- rabbitmqctl cluster_status 2>/dev/null || echo "(node-0 may still be recovering)"

# --- Step 6: Stop sender ---
echo ""
echo "=== Step 6: 停止發送 ==="
kill "${SENDER_PID}" 2>/dev/null || true
wait "${SENDER_PID}" 2>/dev/null || true

echo ""
echo "[DONE] HA 測試完成！"
echo "  如果訊息在節點故障期間仍持續送達，代表 Quorum Queue HA 正常運作。"
