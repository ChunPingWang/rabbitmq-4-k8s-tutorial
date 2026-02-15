#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
K8S_DIR="${PROJECT_DIR}/k8s"
NAMESPACE="rabbitmq-poc"

echo "============================================"
echo " RabbitMQ PoC — Phase 2-4: 憑證 + Cluster + Topology"
echo "============================================"

# --- 1. Certificates ---
echo "[STEP 1/6] Applying CA Issuer and certificates..."
kubectl apply -f "${K8S_DIR}/01-ca-issuer.yaml"
echo "  Waiting for CA certificate to be ready..."
kubectl wait --for=condition=Ready certificate/rabbitmq-ca -n "${NAMESPACE}" --timeout=60s

echo "  Applying server certificate..."
kubectl apply -f "${K8S_DIR}/02-server-certificate.yaml"
kubectl wait --for=condition=Ready certificate/rabbitmq-server-cert -n "${NAMESPACE}" --timeout=60s

echo "  Applying client certificate..."
kubectl apply -f "${K8S_DIR}/04-client-certificate.yaml"
kubectl wait --for=condition=Ready certificate/rabbitmq-client-cert -n "${NAMESPACE}" --timeout=60s

# --- 2. RabbitMQ Cluster ---
echo "[STEP 2/6] Deploying RabbitMQ Cluster (3 replicas, mTLS)..."
kubectl apply -f "${K8S_DIR}/03-rabbitmq-cluster.yaml"
echo "  Waiting for RabbitMQ cluster to be ready (this may take 3-5 minutes)..."
kubectl wait --for=condition=AllReplicasReady rabbitmqcluster/rabbitmq-poc-cluster \
  -n "${NAMESPACE}" --timeout=600s

# --- 3. NetworkPolicy (must be applied BEFORE topology to allow operator access) ---
echo "[STEP 3/7] Applying NetworkPolicies..."
kubectl apply -f "${K8S_DIR}/05-network-policies.yaml"

# --- 4. Configure Topology Operator for mTLS CA ---
echo "[STEP 4/7] Configuring Topology Operator to trust self-signed CA..."
# Copy CA cert to rabbitmq-system namespace
CA_CERT=$(kubectl -n "${NAMESPACE}" get secret rabbitmq-ca-secret -o jsonpath='{.data.ca\.crt}' | base64 -d)
kubectl -n rabbitmq-system create secret generic rabbitmq-ca \
  --from-literal=ca.crt="${CA_CERT}" --dry-run=client -o yaml | kubectl apply -f -

# Patch topology operator to mount CA cert (idempotent check)
EXISTING_VOLUMES=$(kubectl -n rabbitmq-system get deployment messaging-topology-operator \
  -o jsonpath='{.spec.template.spec.volumes[*].name}' 2>/dev/null)
if [[ ! "$EXISTING_VOLUMES" =~ "rabbitmq-ca" ]]; then
  kubectl -n rabbitmq-system patch deployment messaging-topology-operator --type='json' -p='[
    {
      "op": "add",
      "path": "/spec/template/spec/volumes/-",
      "value": {"name":"rabbitmq-ca","secret":{"secretName":"rabbitmq-ca","defaultMode":420}}
    },
    {
      "op": "add",
      "path": "/spec/template/spec/containers/0/volumeMounts/-",
      "value": {"mountPath":"/etc/ssl/certs/rabbitmq-ca.crt","name":"rabbitmq-ca","subPath":"ca.crt"}
    }
  ]'
  echo "  Waiting for topology operator to restart..."
  kubectl -n rabbitmq-system rollout status deployment/messaging-topology-operator --timeout=90s
else
  echo "  CA already mounted in topology operator, skipping patch."
fi

# --- 5. Vhost + User ---
echo "[STEP 5/7] Creating Vhost, User, Permission..."
kubectl apply -f "${K8S_DIR}/06-vhost-user.yaml"
sleep 15  # Wait for topology operator to reconcile

# --- 6. Exchanges + Queues + Bindings ---
echo "[STEP 6/7] Creating Exchanges, Queues, Bindings..."
kubectl apply -f "${K8S_DIR}/07-exchanges.yaml"
kubectl apply -f "${K8S_DIR}/08-queues.yaml"
sleep 5
kubectl apply -f "${K8S_DIR}/09-bindings.yaml"

# --- 7. Policies ---
echo "[STEP 7/7] Applying Policies..."
kubectl apply -f "${K8S_DIR}/10-policies.yaml"

# --- Verify ---
echo ""
echo "============================================"
echo " 驗證結果"
echo "============================================"
echo ""
echo "--- RabbitMQ Cluster ---"
kubectl get rabbitmqcluster -n "${NAMESPACE}"
echo ""
echo "--- Pods ---"
kubectl get pods -n "${NAMESPACE}"
echo ""
echo "--- Topology Resources ---"
echo "Vhosts:    $(kubectl get vhosts -n "${NAMESPACE}" --no-headers 2>/dev/null | wc -l | tr -d ' ')"
echo "Users:     $(kubectl get users -n "${NAMESPACE}" --no-headers 2>/dev/null | wc -l | tr -d ' ')"
echo "Exchanges: $(kubectl get exchanges -n "${NAMESPACE}" --no-headers 2>/dev/null | wc -l | tr -d ' ')"
echo "Queues:    $(kubectl get queues -n "${NAMESPACE}" --no-headers 2>/dev/null | wc -l | tr -d ' ')"
echo "Bindings:  $(kubectl get bindings -n "${NAMESPACE}" --no-headers 2>/dev/null | wc -l | tr -d ' ')"
echo "Policies:  $(kubectl get policies -n "${NAMESPACE}" --no-headers 2>/dev/null | wc -l | tr -d ' ')"
echo ""
echo "[DONE] Phase 2-4 完成！RabbitMQ Cluster + Topology 已就緒"
echo "  接下來請執行: ./scripts/03-build-and-deploy-app.sh"
