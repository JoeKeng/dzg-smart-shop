export interface ShopCategory {
  categoryId?: string | number;
  categoryName?: string;
  sortOrder?: number;
  status?: string;
  remark?: string;
}

export interface ShopProduct {
  productId?: string | number;
  categoryId?: string | number;
  categoryName?: string;
  productName?: string;
  barcode?: string;
  unitName?: string;
  salePrice?: number;
  purchasePrice?: number;
  warningQty?: number;
  imageUrl?: string;
  imageOssId?: string;
  saleCount?: number;
  keyword?: string;
  sortBySales?: boolean;
  supplierId?: string | number;
  supplierIds?: Array<string | number>;
  supplierNames?: string;
  status?: string;
  remark?: string;
  stockQuantity?: number;
}

export interface ShopStock {
  stockId?: string | number;
  productId?: string | number;
  productName?: string;
  quantity?: number;
  warningQty?: number;
}

export interface ShopCustomer {
  customerId?: string | number;
  customerName?: string;
  phone?: string;
  creditLimit?: number;
  currentDebt?: number;
  status?: string;
  remark?: string;
}

export interface ShopOrderItem {
  productId?: string | number;
  quantity?: number;
}

export interface ShopOrderForm {
  customerId?: string | number;
  payType?: string;
  paidAmount?: number;
  remark?: string;
  items: ShopOrderItem[];
}

export interface ShopCreditRecord {
  creditId?: string | number;
  orderId?: string | number;
  customerId?: string | number;
  customerName?: string;
  creditAmount?: number;
  paidAmount?: number;
  unpaidAmount?: number;
  status?: string;
  remark?: string;
}

export interface ShopDashboard {
  todaySales: number;
  todayOrderCount: number;
  todayCredit: number;
  unpaidTotal: number;
  lowStockCount: number;
}

export interface ShopPaymentSummary {
  range: 'today' | 'week' | 'month' | 'all' | string;
  orderCount: number;
  paidAmount: number;
  creditAmount: number;
  repaidCreditAmount?: number;
  unpaidAmount?: number;
  collectedAmount?: number;
  totalAmount: number;
}

export interface ShopSupplier {
  supplierId?: string | number;
  supplierName?: string;
  contactName?: string;
  phone?: string;
  address?: string;
  productCount?: number;
  status?: string;
  remark?: string;
}

export interface ShopPurchaseItem {
  productId?: string | number;
  quantity?: number;
  purchasePrice?: number;
}

export interface ShopPurchaseForm {
  supplierId?: string | number;
  remark?: string;
  items: ShopPurchaseItem[];
}

export interface ShopPurchaseOrder {
  purchaseId?: string | number;
  purchaseNo?: string;
  supplierId?: string | number;
  totalAmount?: number;
  status?: string;
  purchaseTime?: string;
  remark?: string;
}

export interface ShopReport {
  salesTotal: number;
  orderCount: number;
  creditTotal: number;
  unpaidTotal: number;
  grossProfit: number;
  lowStockCount: number;
}

export interface ShopNotification {
  noticeId?: string | number;
  noticeType?: string;
  noticeTitle?: string;
  noticeContent?: string;
  bizType?: string;
  bizId?: string | number;
  status?: string;
}

export interface ShopAiInsight {
  type?: string;
  title?: string;
  content?: string;
  level?: 'success' | 'info' | 'warning' | 'danger' | string;
  actionText?: string;
  actionPath?: string;
}

export interface ShopAiAnalysis {
  summary?: string;
  riskLevel?: 'low' | 'medium' | 'high' | string;
  provider?: string;
  model?: string;
  generatedByAi?: boolean;
  fallbackReason?: string;
  analysisTime?: string;
  insights?: ShopAiInsight[];
}

export interface ShopAiBusinessMetric {
  label?: string;
  value?: string;
  hint?: string;
}

export interface ShopAiBusinessSection {
  title?: string;
  horizon?: string;
  level?: 'success' | 'info' | 'warning' | 'danger' | string;
  content?: string;
  actions?: string[];
}

export interface ShopAiBusinessAnalysis {
  summary?: string;
  riskLevel?: 'low' | 'medium' | 'high' | string;
  provider?: string;
  model?: string;
  generatedByAi?: boolean;
  fallbackReason?: string;
  analysisTime?: string;
  metrics?: ShopAiBusinessMetric[];
  sections?: ShopAiBusinessSection[];
  suggestions?: string[];
}

export interface ShopAiBusinessQuestionResult {
  answer?: string;
  generatedByAi?: boolean;
  fallbackReason?: string;
  suggestions?: string[];
}

export interface ShopAiConversation {
  conversationId?: string | number;
  title?: string;
  status?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ShopAiMessage {
  messageId?: string | number;
  conversationId?: string | number;
  role?: 'user' | 'assistant' | string;
  content?: string;
  createTime?: string;
}

export interface ShopAiChatResult {
  conversation?: ShopAiConversation;
  reply?: ShopAiMessage;
  generatedByAi?: boolean;
  fallbackReason?: string;
  suggestions?: string[];
}
