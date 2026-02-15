#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME="rabbitmq-poc"

echo "============================================"
echo " RabbitMQ PoC — 清理資源"
echo "============================================"

read -p "確定要刪除 Kind 叢集 '${CLUSTER_NAME}' 及所有相關資源嗎？(y/N) " confirm
if [[ "${confirm}" != "y" && "${confirm}" != "Y" ]]; then
  echo "已取消"
  exit 0
fi

echo "[STEP 1/2] Deleting Kind cluster..."
kind delete cluster --name "${CLUSTER_NAME}"

echo "[STEP 2/2] Cleaning up Docker images..."
docker rmi rabbitmq-poc-app:latest 2>/dev/null || true

echo ""
echo "[DONE] 清理完成！"
echo ""
echo "  如需重新建立，請依序執行:"
echo "    ./scripts/01-deploy-infra.sh"
echo "    ./scripts/02-deploy-topology.sh"
echo "    ./scripts/03-build-and-deploy-app.sh"
echo "    ./scripts/04-deploy-monitoring.sh"
