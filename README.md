# RabbitMQ 4.x Kubernetes 教學範例

本專案展示如何在 Kubernetes 環境中部署 RabbitMQ 4.x 叢集，並實作 12 種常見的 RabbitMQ 使用情境，適合初學者學習與參考。

## 目錄

- [專案架構](#專案架構)
- [前置要求](#前置要求)
- [快速開始](#快速開始)
- [12 種 RabbitMQ 使用情境](#12-種-rabbitmq-使用情境)
- [API 端點說明](#api-端點說明)
- [監控與警報](#監控與警報)
- [安全機制 (mTLS)](#安全機制-mtls)
- [清理環境](#清理環境)

---

## 專案架構

```
┌─────────────────────────────────────────────────────────────────┐
│                        Kubernetes Cluster                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    RabbitMQ Cluster (3 nodes)            │  │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐                    │  │
│  │  │ Node 1  │  │ Node 2  │  │ Node 3  │                    │  │
│  │  │ (Pod)   │  │ (Pod)   │  │ (Pod)   │                    │  │
│  │  └─────────┘  └─────────┘  └─────────┘                    │  │
│  │         ↖︎         ↗︎                                       │  │
│  │    Quorum Queue (HA)                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │  Java App (POC) │    │   Ingress NGINX │                     │
│  │   (Deployment)  │    │                 │                     │
│  └─────────────────┘    └─────────────────┘                     │
│                                                                  │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │ cert-manager    │    │ Monitoring Stack│                     │
│  │ (TLS Certificates)│  │ (Prometheus)   │                     │
│  └─────────────────┘    └─────────────────┘                     │
└─────────────────────────────────────────────────────────────────┘
```

### 元件說明

| 元件 | 版本 | 用途 |
|------|------|------|
| RabbitMQ | 4.0-management | 訊息代理伺服器 |
| RabbitMQ Cluster Operator | latest | K8s 上自動化管理 RabbitMQ 叢集 |
| Messaging Topology Operator | latest | K8s CRD 管理 RabbitMQ 資源 (Exchanges, Queues, etc.) |
| cert-manager | latest | TLS 憑證自動化管理 |
| NGINX Ingress | latest | 外部存取入口 |
| Spring Boot | 3.x | 範例應用程式 |
| Prometheus | latest | 監控指標收集 |

---

## 前置要求

- **Docker** (Kind 使用)
- **kubectl** (已設定好連線至 Kubernetes)
- **Kind** (本地開發用 Kubernetes 叢集)
- **Helm** (可選，用於監控部署)

---

## 快速開始

### 1. 部署基礎設施

```bash
./scripts/01-deploy-infra.sh
```

此腳本會：
- 建立 Kind 叢集 (1 control-plane + 3 workers)
- 安裝 cert-manager
- 安裝 RabbitMQ Cluster Operator
- 安裝 Messaging Topology Operator
- 安裝 NGINX Ingress Controller

### 2. 部署 RabbitMQ 叢集與拓撲

```bash
./scripts/02-deploy-topology.sh
```

此腳本會：
- 產生 TLS 憑證 (mTLS)
- 部署 3 節點 RabbitMQ 叢集
- 設定 NetworkPolicy
- 建立 Virtual Host 與使用者
- 建立 Exchanges、Queues、Bindings
- 套用 Policies

### 3. 建構與部署應用程式

```bash
./scripts/03-build-and-deploy-app.sh
```

此腳本會：
- 建構 Docker 映像
- 部署 Spring Boot 應用程式到 Kubernetes

### 4. 驗證部署

```bash
# 查看 RabbitMQ 叢集狀態
kubectl get rabbitmqcluster -n rabbitmq-poc

# 查看應用程式狀態
kubectl get pods -n rabbitmq-poc

# 取得 Ingress IP
kubectl get ingress -n rabbitmq-poc
```

---

## 12 種 RabbitMQ 使用情境

### 情境 1: Simple Queue (簡單佇列)

最基本的點對點訊息傳遞。生產者發送訊息到佇列，單一消費者取出處理。

```
Producer ──► [simple.queue] ──► Consumer
```

**使用場景**：單一工作者處理任務

---

### 情境 2: Work Queue (工作佇列)

多個消費者共享同一佇列，訊息會被輪詢分配給不同的消費者 (Round Robin)。

```
Producer ──► [work.queue] ──┬──► Consumer 1
                            └──► Consumer 2
```

**使用場景**：負載平衡的任務處理

---

### 情境 3: Pub/Sub (發布/訂閱)

使用 Fanout Exchange，將訊息廣播給所有訂閱的消費者。

```
Producer ──► [notifications.exchange (fanout)] ──┬──► [email.queue] ──► Email Consumer
                                                 └──► [sms.queue] ──► SMS Consumer
```

**使用場景**：系統通知、事件廣播

---

### 情境 4: Direct Routing (直接路由)

使用 Direct Exchange，根據 routing key 精確路由訊息到特定佇列。

```
Producer ──► [order.exchange (direct)] ──┬──► [order.created.queue] ──► Created Handler
                                         └──► [order.shipped.queue] ──► Shipped Handler
                    routing_key: order.created
```

**使用場景**：訂單狀態變更通知

---

### 情境 5: Topic Exchange (主題路由)

使用 Topic Exchange，支援萬用字元匹配多個 routing key。

```
Producer ──► [events.exchange (topic)] ──┬──► [log.info.queue]     (routing key: *.info)
                                          └──► [log.error.queue]    (routing key: *.error)
                 routing_key: user.created
```

**使用場景**：複雜的事件分類與路由

**萬用字元**：
- `*` 匹配一個單詞
- `#` 匹配零或多個詞

---

### 情境 6: Headers Exchange (標頭路由)

根據訊息標頭屬性進行路由，而非 routing key。

```
Producer ──► [reports.exchange (headers)] ──► [finance.report.queue] ──► Finance Consumer
              headers: department=finance, format=pdf
```

**使用場景**：根據多種屬性條件路由訊息

---

### 情境 7: RPC (遠端程序呼叫)

客戶端發送請求訊息並等待伺服器回應，同步通訊模式。

```
Client ──► [rpc.queue] ──► Server
          ◄── [reply.queue] ◄──
```

**使用場景**：需要同步回應的遠端服務呼叫

---

### 情境 8: Dead Letter Queue (死信佇列)

處理無法正常消費的訊息 (例如超過重試次數)。

```
[main.queue] ──(reject/expire)──► [dlx.exchange] ──► [dead.letter.queue]
```

**使用場景**：錯誤訊息處理、訊息重試機制

---

### 情境 9: Delayed Message (延遲訊息)

使用插件實現訊息延遲投遞。

```
Producer ──► [delayed.exchange] ──► [delayed.queue] ──► Consumer
            (x-delay: 30000)        (30秒後釋出)
```

**使用場景**：排程任務、延遲處理

---

### 情境 10: Priority Queue (優先權佇列)

訊息帶有優先權，高優先權訊息會先被消費。

```
Producer ──► [priority.queue] ──► Consumer
             priority: 10 (高)
             priority: 5  (低)
```

**使用場景**：VIP 用戶請求、重要訂單處理

---

### 情境 11: Quorum Queue (仲裁佇列)

RabbitMQ 3.8+ 引入的 HA 佇列，基於 Raft 共識協議實現高可用性。

**特色**：
- 預設 5 個副本
- 自動資料同步
- 支援資料持久化
- 無需 Policy 即有 HA

**使用場景**：關鍵業務訊息需要高可靠性

---

### 情境 12: Stream (串流)

RabbitMQ 3.9+ 引入的新功能，支援大量訊息持久化與回放。

**特色**：
- 追加寫入 (Append-only)
- 支援多消費者
- 訊息回放 (Replay)
- 消費者追蹤 (Offset)

**使用場景**：事件溯源、日誌收集、審計追蹤

---

## API 端點說明

部署完成後，可透過以下 API 測試各情境：

| 方法 | 端點 | 情境說明 |
|------|------|----------|
| POST | `/api/demo/simple?msg=Hello` | Simple Queue |
| POST | `/api/demo/work-queue?count=10` | Work Queue |
| POST | `/api/demo/notification?title=Alert&recipient=user1` | Pub/Sub |
| POST | `/api/demo/order/{status}?orderId=ORD-001` | Direct Routing |
| POST | `/api/demo/event?routingKey=user.created&data=test` | Topic Exchange |
| POST | `/api/demo/report?format=pdf&dept=finance` | Headers Exchange |
| GET | `/api/demo/credit-score/{customerId}` | RPC |
| POST | `/api/demo/poison` | Dead Letter Queue |
| POST | `/api/demo/delayed?msg=Hello&delayMs=30000` | Delayed Message |
| POST | `/api/demo/priority?msg=VIP&priority=10` | Priority Queue |
| POST | `/api/demo/ha-test` | Quorum Queue HA |
| POST | `/api/demo/stream?msg=Hello&count=5` | Stream |

### 取得 Ingress IP

```bash
# 查看 Ingress
kubectl get ingress -n rabbitmq-poc

# 測試 API
curl http://<INGRESS-IP>/api/demo/simple?msg=Hello
```

---

## 監控與警報

### 部署監控堆疊

```bash
./scripts/04-deploy-monitoring.sh
```

### 監控面板

- **RabbitMQ Management UI**: `https://rabbitmq-poc-cluster.rabbitmq-poc.svc.cluster.local:15671/`
- **Prometheus Metrics**: 透過 Service Monitor 自動收集
- **Grafana Dashboard**: 匯入 [RabbitMQ Grafana Dashboard](https://grafana.com/grafana/dashboards/10991-rabbitmq-overview/)

### 警報規則

已部署的警報規則 (參考 `k8s/13-alert-rules.yaml`)：
- `RabbitMQNodeDown`: 節點離線
- `RabbitMQHighMemoryUsage`: 記憶體使用過高 (>70%)
- `RabbitMQHighDiskUsage`: 磁碟空間不足
- `RabbitMQQueueGrowth`: 佇列訊息快速累積

---

## 安全機制 (mTLS)

本專案採用 **Mutual TLS (雙向認證)**：

### 憑證架構

```
┌─────────────┐     ┌─────────────┐
│   Root CA   │────▶│ Server Cert │
└─────────────┘     └─────────────┘
       │
       └───────────▶│ Client Cert │
                    └─────────────┘
```

### 設定的元件

- **Server Certificate**: RabbitMQ Server 使用
- **Client Certificate**: Spring Boot 應用程式使用
- **Management TLS**: RabbitMQ Management UI 使用

### 驗證 mTLS

```bash
# 查看憑證
kubectl get certificate -n rabbitmq-poc

# 查看 Secret
kubectl get secret rabbitmq-server-tls -n rabbitmq-poc
```

---

## 清理環境

### 清理 Kubernetes 資源

```bash
./scripts/cleanup.sh
```

此腳本會刪除：
- 命名空間 `rabbitmq-poc` (包含所有資源)
- 監控相關資源
- Kind 叢集

### 個別清理

```bash
# 刪除應用程式
kubectl delete -f k8s/11-app-deployment.yaml

# 刪除 RabbitMQ 叢集
kubectl delete -f k8s/03-rabbitmq-cluster.yaml

# 刪除整個 Kind 叢集
kind delete cluster --name rabbitmq-poc
```

---

## 學習路徑建議

1. **初階**：從情境 1 (Simple Queue) 開始，理解基本的生產者/消費者模式
2. **進階**：學習情境 3-6，了解不同的 Exchange 類型與路由機制
3. **高階**：研究情境 7-12，包含 RPC、DLX、Stream 等進階功能
4. **生產部署**：研究本專案的 Kubernetes 部署設定，包括安全、監控、高可用性

---

## 相關資源

- [RabbitMQ 官方文檔](https://www.rabbitmq.com/docs)
- [RabbitMQ Kubernetes Operator](https://www.rabbitmq.com/kubernetes/operator/operator-overview)
- [Messaging Topology Operator](https://www.rabbitmq.com/kubernetes/operator/topology-operator)
- [Spring AMQP 文檔](https://docs.spring.io/spring-amqp/reference/html/)

---

## 授權

MIT License
